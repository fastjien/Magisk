package com.fastjien.sunny.dialog

import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.lifecycle.lifecycleScope
import com.fastjien.sunny.R
import com.fastjien.sunny.core.di.ServiceLocator
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.view.SunnyDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import com.fastjien.sunny.core.R as CoreR

abstract class MarkDownDialog : DialogBuilder {

    abstract suspend fun getMarkdownText(): String

    @CallSuper
    override fun build(dialog: SunnyDialog) {
        with(dialog) {
            val view = LayoutInflater.from(context).inflate(R.layout.markdown_window_md2, null)
            setView(view)
            val tv = view.findViewById<TextView>(R.id.md_txt)
            activity.lifecycleScope.launch {
                try {
                    val text = withContext(Dispatchers.IO) { getMarkdownText() }
                    ServiceLocator.markwon.setMarkdown(tv, text)
                } catch (e: IOException) {
                    Timber.e(e)
                    tv.setText(CoreR.string.download_file_error)
                }
            }
        }
    }
}
