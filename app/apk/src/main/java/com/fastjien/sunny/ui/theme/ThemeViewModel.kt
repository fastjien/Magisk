package com.fastjien.sunny.ui.theme

import com.fastjien.sunny.arch.BaseViewModel
import com.fastjien.sunny.core.Config
import com.fastjien.sunny.dialog.DarkThemeDialog
import com.fastjien.sunny.events.RecreateEvent
import com.fastjien.sunny.view.TappableHeadlineItem

class ThemeViewModel : BaseViewModel(), TappableHeadlineItem.Listener {

    val themeHeadline = TappableHeadlineItem.ThemeMode

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        is TappableHeadlineItem.ThemeMode -> DarkThemeDialog().show()
    }

    fun saveTheme(theme: Theme) {
        if (!theme.isSelected) {
            Config.themeOrdinal = theme.ordinal
            RecreateEvent().publish()
        }
    }
}
