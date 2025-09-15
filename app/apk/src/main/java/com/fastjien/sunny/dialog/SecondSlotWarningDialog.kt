package com.fastjien.sunny.dialog

import com.fastjien.sunny.core.R
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.view.SunnyDialog

class SecondSlotWarningDialog : DialogBuilder {

    override fun build(dialog: SunnyDialog) {
        dialog.apply {
            setTitle(android.R.string.dialog_alert_title)
            setMessage(R.string.install_inactive_slot_msg)
            setButton(SunnyDialog.ButtonType.POSITIVE) {
                text = android.R.string.ok
            }
            setCancelable(true)
        }
    }
}
