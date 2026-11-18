package io.nekohasekai.sagernet.ui

import android.os.RemoteException
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.databinding.LayoutConnectionsBinding
import io.nekohasekai.sagernet.ktx.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ConnectionsFragment : ToolbarFragment(R.layout.layout_connections), Toolbar.OnMenuItemClickListener {

    private enum class Filter { ACTIVE, CLOSED, ALL }
    private data class LastTraffic(val sampledAt: Long, val upload: Long, val download: Long)

    private lateinit var binding: LayoutConnectionsBinding
    private lateinit var adapter: ConnectionsAdapter
    private val gson = Gson()
    private var filter = Filter.ACTIVE
    private var query = ""
    private var snapshot = ConnectionSnapshot()
    private var displayItems: List<ConnectionDisplay> = emptyList()
    private var lastTraffic = HashMap<String, LastTraffic>()

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutConnectionsBinding.bind(view)
        toolbar?.setTitle(R.string.connections_title)
        toolbar?.inflateMenu(R.menu.connections_menu)
        toolbar?.setOnMenuItemClickListener(this)

        adapter = ConnectionsAdapter(::closeConnection)
        binding.connectionList.layoutManager = LinearLayoutManager(requireContext())
        binding.connectionList.adapter = adapter
        binding.connectionFilters.check(R.id.connection_filter_active)
        binding.connectionFilters.addOnButtonCheckedListener { _, checkedId, checked ->
            if (!checked) return@addOnButtonCheckedListener
            filter = when (checkedId) {
                R.id.connection_filter_closed -> Filter.CLOSED
                R.id.connection_filter_all -> Filter.ALL
                else -> Filter.ACTIVE
            }
            render()
        }
        binding.connectionSearch.doOnTextChanged { text, _, _, _ ->
            query = text?.toString().orEmpty().trim()
            render()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    refresh()
                    delay(1000L)
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId != R.id.action_connections_refresh) return false
        viewLifecycleOwner.lifecycleScope.launch { refresh() }
        return true
    }

    private suspend fun refresh() {
        val service = (activity as? MainActivity)?.connection?.service ?: return
        val json = withContext(Dispatchers.IO) {
            try {
                service.queryConnections()
            } catch (e: RemoteException) {
                Logs.w(e)
                null
            } catch (e: Exception) {
                Logs.w(e)
                null
            }
        } ?: return
        val fresh = try {
            gson.fromJson(json, ConnectionSnapshot::class.java)?.sanitized() ?: ConnectionSnapshot()
        } catch (e: Exception) {
            Logs.w(e)
            ConnectionSnapshot()
        }
        snapshot = fresh
        updateDisplayItems()
        render()
    }

    private fun updateDisplayItems() {
        val now = SystemClock.elapsedRealtime()
        val nextTraffic = HashMap<String, LastTraffic>()
        val activeItems = snapshot.active.map { record ->
            val previous = lastTraffic[record.id]
            val elapsed = (previous?.let { now - it.sampledAt } ?: 0L).coerceAtLeast(1L)
            val uploadRate = previous?.let { ((record.upload - it.upload).coerceAtLeast(0L) * 1000L) / elapsed } ?: 0L
            val downloadRate = previous?.let { ((record.download - it.download).coerceAtLeast(0L) * 1000L) / elapsed } ?: 0L
            nextTraffic[record.id] = LastTraffic(now, record.upload, record.download)
            ConnectionDisplay(record, uploadRate, downloadRate, active = true)
        }
        val closedItems = snapshot.closed.map { ConnectionDisplay(it, 0L, 0L, active = false) }
        lastTraffic = nextTraffic
        displayItems = activeItems + closedItems
    }

    private fun render() {
        if (!::binding.isInitialized || !::adapter.isInitialized) return
        val activeCount = snapshot.active.size
        val closedCount = snapshot.closed.size
        binding.connectionFilterActive.text = getString(R.string.connection_filter_active, activeCount)
        binding.connectionFilterClosed.text = getString(R.string.connection_filter_closed, closedCount)
        binding.connectionFilterAll.text = getString(R.string.connection_filter_all)
        binding.connectionSummary.text = getString(
            R.string.connections_summary,
            activeCount,
            closedCount,
            formatTotal(snapshot.active.sumOf { it.upload } + snapshot.closed.sumOf { it.upload }),
            formatTotal(snapshot.active.sumOf { it.download } + snapshot.closed.sumOf { it.download }),
        )

        val needle = query.lowercase(Locale.ROOT)
        val filtered = displayItems.filter { display ->
            val record = display.record
            val matchesFilter = when (filter) {
                Filter.ACTIVE -> display.active
                Filter.CLOSED -> !display.active
                Filter.ALL -> true
            }
            if (!matchesFilter || needle.isEmpty()) return@filter matchesFilter
            val searchable = sequenceOf(
                record.domain,
                record.destination,
                record.source,
                record.network,
                record.protocol,
                record.outbound,
                record.outboundType,
            ).plus(record.chain.asSequence()).plus(record.packages.asSequence()).plus(record.processPaths.asSequence())
                .joinToString(" ")
                .lowercase(Locale.ROOT)
            searchable.contains(needle)
        }
        adapter.submitList(filtered)
        binding.connectionEmpty.isVisible = filtered.isEmpty()
        binding.connectionEmpty.setText(
            when {
                query.isNotEmpty() -> R.string.connections_empty_search
                filter == Filter.CLOSED -> R.string.connections_empty_closed
                else -> R.string.connections_empty_active
            },
        )
    }

    private fun formatTotal(value: Long): String = android.text.format.Formatter.formatShortFileSize(requireContext(), value.coerceAtLeast(0L))

    private fun closeConnection(id: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val service = (activity as? MainActivity)?.connection?.service ?: return@launch
            withContext(Dispatchers.IO) {
                try {
                    service.closeConnection(id)
                } catch (e: Exception) {
                    Logs.w(e)
                }
            }
            refresh()
        }
    }
}
