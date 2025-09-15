package com.fastjien.sunny.dialog

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import com.fastjien.sunny.R
import com.fastjien.sunny.arch.UIActivity
import com.fastjien.sunny.core.Config
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.view.SunnyDialog
import com.fastjien.sunny.core.R as CoreR

class DarkThemeDialog : DialogBuilder {

    override fun build(dialog: SunnyDialog) {
        val activity = dialog.ownerActivity!!
        dialog.apply {
            setTitle(CoreR.string.settings_dark_mode_title)
            setMessage(CoreR.string.settings_dark_mode_message)
            setButton(SunnyDialog.ButtonType.POSITIVE) {
                text = CoreR.string.settings_dark_mode_light
                icon = R.drawable.ic_day
                onClick { selectTheme(AppCompatDelegate.MODE_NIGHT_NO, activity) }
            }
            setButton(SunnyDialog.ButtonType.NEUTRAL) {
                text = CoreR.string.settings_dark_mode_system
                icon = R.drawable.ic_day_night
                onClick { selectTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, activity) }
            }
            setButton(SunnyDialog.ButtonType.NEGATIVE) {
                text = CoreR.string.settings_dark_mode_dark
                icon = R.drawable.ic_night
                onClick { selectTheme(AppCompatDelegate.MODE_NIGHT_YES, activity) }
            }
        }
    }

    private fun selectTheme(mode: Int, activity: Activity) {
        Config.darkTheme = mode
        (activity as UIActivity<*>).delegate.localNightMode = mode
    }
}
