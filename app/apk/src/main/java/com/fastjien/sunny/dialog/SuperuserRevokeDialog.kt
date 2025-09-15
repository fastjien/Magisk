package com.fastjien.sunny.dialog

import com.fastjien.sunny.core.R
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.view.SunnyDialog

class SuperuserRevokeDialog(
    private val appName: String,
    private val onSuccess: () -> Unit
) : DialogBuilder {

    override fun build(dialog: SunnyDialog) {
        dialog.apply {
            setTitle(R.string.su_revoke_title)
            setMessage(R.string.su_revoke_msg, appName)
            setButton(SunnyDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
                onClick { onSuccess() }
            }
            setButton(SunnyDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }
    }
}
