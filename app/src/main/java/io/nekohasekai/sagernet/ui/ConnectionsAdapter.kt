package io.nekohasekai.sagernet.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.utils.PackageCache

class ConnectionsAdapter(
    private val onClose: (String) -> Unit,
) : RecyclerView.Adapter<ConnectionsAdapter.Holder>() {

    private var items: List<ConnectionDisplay> = emptyList()
    private val icons = HashMap<String, Drawable>()

    @Suppress("NotifyDataSetChanged")
    fun submitList(value: List<ConnectionDisplay>) {
        items = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.layout_connection_item, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])

    inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val card = view as MaterialCardView
        private val icon: ImageView = view.findViewById(R.id.connection_app_icon)
        private val appName: TextView = view.findViewById(R.id.connection_app_name)
        private val packageName: TextView = view.findViewById(R.id.connection_package_name)
        private val protocol: TextView = view.findViewById(R.id.connection_protocol)
        private val target: TextView = view.findViewById(R.id.connection_target)
        private val traffic: TextView = view.findViewById(R.id.connection_traffic)
        private val chain: TextView = view.findViewById(R.id.connection_chain)
        private val closed: TextView = view.findViewById(R.id.connection_closed)
        private val closeButton: ImageButton = view.findViewById(R.id.connection_close)

        fun bind(display: ConnectionDisplay) {
            // Core JSON can contain null lists despite the non-null Kotlin model.
            val safeDisplay = display.sanitized()
            val record = safeDisplay.record
            val context = itemView.context
            val packageSnapshot = PackageCache.snapshot()
            val packages = record.packages.orEmpty() + packageSnapshot.packagesOf(record.ownerUid).orEmpty()
            val ownerPackage = packages.firstOrNull { packageSnapshot.installedApps.containsKey(it) }
            val application = ownerPackage?.let { packageSnapshot.installedApps[it] }
            val path = record.processPaths.orEmpty().firstOrNull().orEmpty()
            appName.text = application?.let { packageSnapshot.loadLabel(it.packageName) }
                ?: path.substringAfterLast('/').ifBlank { context.getString(R.string.connection_unknown_app) }
            packageName.text = ownerPackage ?: path
            packageName.isVisible = packageName.text.isNotBlank()
            val fallback = ContextCompat.getDrawable(context, R.drawable.ic_navigation_apps)
            icon.setImageDrawable(
                ownerPackage?.let { name ->
                    icons.getOrPut(name) {
                        runCatching { application?.loadIcon(context.packageManager) }.getOrNull() ?: fallback!!
                    }
                } ?: fallback,
            )
            icon.contentDescription = appName.text

            protocol.text = listOf(record.network, record.protocol)
                .filter(String::isNotBlank)
                .distinct()
                .joinToString(" · ")
                .ifBlank { "TCP" }

            val domain = record.domain.trim()
            val destination = record.destination.trim()
            target.text = when {
                domain.isBlank() -> destination
                destination.isBlank() || destination.contains(domain, ignoreCase = true) -> destination.ifBlank { domain }
                else -> "$domain\n$destination"
            }.ifBlank { context.getString(R.string.connection_unknown_target) }

            traffic.text = context.getString(
                R.string.connection_transfer,
                formatRate(context, safeDisplay.uploadRate),
                formatRate(context, safeDisplay.downloadRate),
                formatBytes(context, record.upload),
                formatBytes(context, record.download),
            )

            val route = record.chain.orEmpty().filter(String::isNotBlank).ifEmpty {
                listOf(record.outbound.ifBlank { context.getString(R.string.connection_direct) })
            }
            chain.text = context.getString(R.string.connection_chain, route.joinToString(" → "))
            chain.isVisible = route.isNotEmpty()

            closed.isVisible = !safeDisplay.active
            if (!safeDisplay.active) {
                val relative = DateUtils.getRelativeTimeSpanString(
                    record.closedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                )
                closed.text = context.getString(R.string.connection_closed_at, relative)
            }

            closeButton.isVisible = safeDisplay.active
            closeButton.setOnClickListener { onClose(record.id) }
            closeButton.contentDescription = context.getString(R.string.connection_close)
            card.contentDescription = context.getString(R.string.connection_card_description, appName.text, target.text)
        }
    }

    private fun formatBytes(context: Context, value: Long): String = Formatter.formatShortFileSize(context, value.coerceAtLeast(0L))

    private fun formatRate(context: Context, value: Long): String = "${formatBytes(context, value)}/s"
}
