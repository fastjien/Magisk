package com.fastjien.sunny.dialog

import android.app.ProgressDialog
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.fastjien.sunny.arch.NavigationActivity
import com.fastjien.sunny.arch.UIActivity
import com.fastjien.sunny.core.R
import com.fastjien.sunny.core.ktx.toast
import com.fastjien.sunny.core.tasks.SunnyInstaller
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.ui.flash.FlashFragment
import com.fastjien.sunny.view.SunnyDialog
import kotlinx.coroutines.launch

class UninstallDialog : DialogBuilder {

    override fun build(dialog: SunnyDialog) {
        dialog.apply {
            setTitle(R.string.uninstall_sunny_title)
            setMessage(R.string.uninstall_sunny_msg)
            setButton(SunnyDialog.ButtonType.POSITIVE) {
                text = R.string.restore_img
                onClick { restore(dialog.activity) }
            }
            setButton(SunnyDialog.ButtonType.NEGATIVE) {
                text = R.string.complete_uninstall
                onClick { completeUninstall(dialog) }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun restore(activity: UIActivity<*>) {
        val dialog = ProgressDialog(activity).apply {
            setMessage(activity.getString(R.string.restore_img_msg))
            show()
        }

        activity.lifecycleScope.launch {
            SunnyInstaller.Restore().exec { success ->
                dialog.dismiss()
                if (success) {
                    activity.toast(R.string.restore_done, Toast.LENGTH_SHORT)
                } else {
                    activity.toast(R.string.restore_fail, Toast.LENGTH_LONG)
                }
            }
        }
    }

    private fun completeUninstall(dialog: SunnyDialog) {
        (dialog.ownerActivity as NavigationActivity<*>)
            .navigation.navigate(FlashFragment.uninstall())
    }

}
