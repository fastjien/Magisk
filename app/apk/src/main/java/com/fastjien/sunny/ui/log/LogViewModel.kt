package com.fastjien.sunny.ui.log

import android.system.Os
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.fastjien.sunny.BR
import com.fastjien.sunny.arch.AsyncLoadViewModel
import com.fastjien.sunny.core.BuildConfig
import com.fastjien.sunny.core.R
import com.fastjien.sunny.core.Info
import com.fastjien.sunny.core.ktx.timeFormatStandard
import com.fastjien.sunny.core.ktx.toTime
import com.fastjien.sunny.core.repository.LogRepository
import com.fastjien.sunny.core.utils.MediaStoreUtils
import com.fastjien.sunny.core.utils.MediaStoreUtils.outputStream
import com.fastjien.sunny.databinding.bindExtra
import com.fastjien.sunny.databinding.diffList
import com.fastjien.sunny.databinding.set
import com.fastjien.sunny.events.SnackbarEvent
import com.fastjien.sunny.view.TextItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

class LogViewModel(
    private val repo: LogRepository
) : AsyncLoadViewModel() {
    @get:Bindable
    var loading = true
        private set(value) = set(value, field, { field = it }, BR.loading)

    // --- empty view

    val itemEmpty = TextItem(R.string.log_data_none)
    val itemMagiskEmpty = TextItem(R.string.log_data_sunny_none)

    // --- su log

    val items = diffList<SuLogRvItem>()
    val extraBindings = bindExtra {
        it.put(BR.viewModel, this)
    }

    // --- sunny log
    val logs = diffList<LogRvItem>()
    var sunnyLogRaw = " "

    override suspend fun doLoadWork() {
        loading = true

        val (suLogs, suDiff) = withContext(Dispatchers.Default) {
            sunnyLogRaw = repo.fetchMagiskLogs()
            val newLogs = sunnyLogRaw.split('\n').map { LogRvItem(it) }
            logs.update(newLogs)
            val suLogs = repo.fetchSuLogs().map { SuLogRvItem(it) }
            suLogs to items.calculateDiff(suLogs)
        }

        items.firstOrNull()?.isTop = false
        items.lastOrNull()?.isBottom = false
        items.update(suLogs, suDiff)
        items.firstOrNull()?.isTop = true
        items.lastOrNull()?.isBottom = true
        loading = false
    }

    fun saveMagiskLog() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val filename = "sunny_log_%s.log".format(
                System.currentTimeMillis().toTime(timeFormatStandard)
            )
            val logFile = MediaStoreUtils.getFile(filename)
            logFile.uri.outputStream().bufferedWriter().use { file ->
                file.write("---Detected Device Info---\n\n")
                file.write("isAB=${Info.isAB}\n")
                file.write("isSAR=${Info.isSAR}\n")
                file.write("ramdisk=${Info.ramdisk}\n")
                val uname = Os.uname()
                file.write("kernel=${uname.sysname} ${uname.machine} ${uname.release} ${uname.version}\n")

                file.write("\n\n---System Properties---\n\n")
                ProcessBuilder("getprop").start()
                    .inputStream.reader().use { it.copyTo(file) }

                file.write("\n\n---Environment Variables---\n\n")
                System.getenv().forEach { (key, value) -> file.write("${key}=${value}\n") }

                file.write("\n\n---System MountInfo---\n\n")
                FileInputStream("/proc/self/mountinfo").reader().use { it.copyTo(file) }

                file.write("\n---Magisk Logs---\n")
                file.write("${Info.env.versionString} (${Info.env.versionCode})\n\n")
                if (Info.env.isActive) file.write(sunnyLogRaw)

                file.write("\n---Manager Logs---\n")
                file.write("${BuildConfig.APP_VERSION_NAME} (${BuildConfig.APP_VERSION_CODE})\n\n")
                ProcessBuilder("logcat", "-d").start()
                    .inputStream.reader().use { it.copyTo(file) }
            }
            SnackbarEvent(logFile.toString()).publish()
        }
    }

    fun clearMagiskLog() = repo.clearMagiskLogs {
        SnackbarEvent(R.string.logs_cleared).publish()
        startLoading()
    }

    fun clearLog() = viewModelScope.launch {
        repo.clearLogs()
        SnackbarEvent(R.string.logs_cleared).publish()
        startLoading()
    }
}
