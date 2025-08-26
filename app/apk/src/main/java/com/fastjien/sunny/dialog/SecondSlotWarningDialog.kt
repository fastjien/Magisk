package com.fastjien.sunny.dialog

import com.fastjien.sunny.core.R
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.view.MagiskDialog

class SecondSlotWarningDialog : DialogBuilder {

    override fun build(dialog: MagiskDialog) {
        dialog.apply {
            setTitle(android.R.string.dialog_alert_title)
            setMessage(R.string.install_inactive_slot_msg)
            setButton(MagiskDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
            }
            setCancelable(true)
        }
    }
}
