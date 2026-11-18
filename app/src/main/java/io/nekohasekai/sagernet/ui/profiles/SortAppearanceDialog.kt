package io.nekohasekai.sagernet.ui.profiles

import android.content.Context
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.GroupSortMethod
import io.nekohasekai.sagernet.database.ProxyGroup
import io.nekohasekai.sagernet.database.TestBy
import io.nekohasekai.sagernet.ui.ConfigurationFragment
import io.nekohasekai.sagernet.ktx.getColorAttr

internal object SortAppearanceDialog {

    private data class Choice(val id: Int, val title: Int, val select: () -> Unit)

    private const val ORIGINAL = 101
    private const val BY_NAME = 102
    private const val BY_LATENCY = 103
    private const val DETAILED = 401
    private const val CONCISE = 402

    fun show(host: ConfigurationFragment) {
        val context = host.requireContext()
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(8), context.dp(16), context.dp(8))
        }
        val scroll = ScrollView(context).apply {
            isFillViewport = false
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        val currentGroup = host.currentGroup()
        val sortSelection = currentGroup?.let(::selectedSort) ?: ORIGINAL
        addSection(
            context,
            content,
            R.string.appearance_sort,
            sortSelection,
            listOf(
                Choice(ORIGINAL, R.string.profiles_sort_original) {
                    host.currentGroup()?.let { GroupActions.stopSorting(host, it.id) }
                },
                Choice(BY_NAME, R.string.profiles_sort_name) {
                    host.currentGroup()?.let { GroupActions.sort(host, it.id, GroupSortMethod.BY_NAME, false) }
                },
                Choice(BY_LATENCY, R.string.profiles_sort_latency) {
                    host.currentGroup()?.let { group ->
                        GroupActions.sort(host, group.id, GroupSortMethod.BY_TEST_RESULT, false) {
                            it.testSortBy = TestBy.LATENCY.value
                        }
                    }
                },
            ),
        ).isEnabled = currentGroup != null

        addSection(
            context,
            content,
            R.string.profile_display_mode,
            if (host.compactRows) CONCISE else DETAILED,
            listOf(
                Choice(DETAILED, R.string.profile_display_detailed) { host.setCompactRows(false) },
                Choice(CONCISE, R.string.profile_display_concise) { host.setCompactRows(true) },
            ),
        )

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.profiles_sort_appearance)
            .setView(scroll)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun selectedSort(group: ProxyGroup): Int = when {
        group.sort.method == GroupSortMethod.BY_NAME -> BY_NAME
        (group.sort.method == GroupSortMethod.BY_TEST_RESULT || group.sort.method == GroupSortMethod.BY_LATENCY) &&
            TestBy.of(group.testSortBy) == TestBy.LATENCY -> BY_LATENCY
        else -> ORIGINAL
    }

    private fun addSection(
        context: Context,
        parent: LinearLayout,
        title: Int,
        checkedId: Int,
        choices: List<Choice>,
    ): RadioGroup {
        parent.addView(TextView(context).apply {
            text = context.getString(title)
            setTextAppearance(android.R.style.TextAppearance_Medium)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(context.getColorAttr(R.attr.colorPrimary))
            setPadding(0, context.dp(8), 0, context.dp(2))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        return RadioGroup(context).apply {
            orientation = RadioGroup.VERTICAL
            for (choice in choices) {
                addView(MaterialRadioButton(context).apply {
                    id = choice.id
                    text = context.getString(choice.title)
                    setPadding(context.dp(8), context.dp(4), context.dp(8), context.dp(4))
                    layoutParams = RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })
            }
            check(checkedId)
            setOnCheckedChangeListener { _, id -> choices.firstOrNull { it.id == id }?.select?.invoke() }
            parent.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
