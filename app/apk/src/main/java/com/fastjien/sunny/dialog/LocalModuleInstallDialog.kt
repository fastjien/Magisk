package com.fastjien.sunny.dialog

import android.net.Uri
import com.fastjien.sunny.MainDirections
import com.fastjien.sunny.core.Const
import com.fastjien.sunny.core.R
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.ui.module.ModuleViewModel
import com.fastjien.sunny.view.SunnyDialog

class LocalModuleInstallDialog(
    private val viewModel: ModuleViewModel,
    private val uri: Uri,
    private val displayName: String
) : DialogBuilder {
    override fun build(dialog: SunnyDialog) {
        dialog.apply {
            setTitle(R.string.confirm_install_title)
            setMessage(context.getString(R.string.confirm_install, displayName))
            setButton(SunnyDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick {
                    viewModel.apply {
                        MainDirections.actionFlashFragment(Const.Value.FLASH_ZIP, uri).navigate()
                    }
                }
            }
            setButton(SunnyDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }
    }
}
