package com.fastjien.sunny.dialog

import com.fastjien.sunny.core.AppContext
import com.fastjien.sunny.core.Info
import com.fastjien.sunny.core.R
import com.fastjien.sunny.core.download.DownloadEngine
import com.fastjien.sunny.core.download.Subject
import com.fastjien.sunny.view.SunnyDialog
import java.io.File

class ManagerInstallDialog : MarkDownDialog() {

    override suspend fun getMarkdownText(): String {
        val text = Info.update.note
        // Cache the changelog
        File(AppContext.cacheDir, "${Info.update.versionCode}.md").writeText(text)
        return text
    }

    override fun build(dialog: SunnyDialog) {
        super.build(dialog)
        dialog.apply {
            setCancelable(true)
            setButton(SunnyDialog.ButtonType.POSITIVE) {
                text = R.string.install
                onClick { DownloadEngine.startWithActivity(activity, Subject.App()) }
            }
            setButton(SunnyDialog.ButtonType.NEGATIVE) {
                text = android.R.string.cancel
            }
        }
    }

}
