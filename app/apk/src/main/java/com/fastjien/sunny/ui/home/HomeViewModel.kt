package com.fastjien.sunny.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.Bindable
import com.fastjien.sunny.BR
import com.fastjien.sunny.R
import com.fastjien.sunny.arch.ActivityExecutor
import com.fastjien.sunny.arch.AsyncLoadViewModel
import com.fastjien.sunny.arch.ContextExecutor
import com.fastjien.sunny.arch.UIActivity
import com.fastjien.sunny.arch.ViewEvent
import com.fastjien.sunny.core.BuildConfig
import com.fastjien.sunny.core.Config
import com.fastjien.sunny.core.Info
import com.fastjien.sunny.core.download.Subject
import com.fastjien.sunny.core.download.Subject.App
import com.fastjien.sunny.core.ktx.await
import com.fastjien.sunny.core.ktx.toast
import com.fastjien.sunny.core.repository.NetworkService
import com.fastjien.sunny.databinding.bindExtra
import com.fastjien.sunny.databinding.set
import com.fastjien.sunny.dialog.EnvFixDialog
import com.fastjien.sunny.dialog.ManagerInstallDialog
import com.fastjien.sunny.dialog.UninstallDialog
import com.fastjien.sunny.events.SnackbarEvent
import com.fastjien.sunny.ui.surequest.SuRequestActivity
import com.fastjien.sunny.utils.asText
import com.topjohnwu.superuser.Shell
import kotlin.math.roundToInt
import com.fastjien.sunny.core.R as CoreR

class HomeViewModel(
    private val svc: NetworkService
) : AsyncLoadViewModel() {

    enum class State {
        LOADING, INVALID, OUTDATED, UP_TO_DATE
    }

    val magiskTitleBarrierIds =
        intArrayOf(R.id.home_magisk_icon, R.id.home_magisk_title, R.id.home_magisk_button)
    val appTitleBarrierIds =
        intArrayOf(R.id.home_manager_icon, R.id.home_manager_title, R.id.home_manager_button)

    @get:Bindable
    var isNoticeVisible = Config.safetyNotice
        set(value) = set(value, field, { field = it }, BR.noticeVisible)

    val magiskState
        get() = when {
            Info.isRooted && Info.env.isUnsupported -> State.OUTDATED
            !Info.env.isActive -> State.INVALID
            Info.env.versionCode < BuildConfig.APP_VERSION_CODE -> State.OUTDATED
            else -> State.UP_TO_DATE
        }

    @get:Bindable
    var appState = State.LOADING
        set(value) = set(value, field, { field = it }, BR.appState)

    val magiskInstalledVersion
        get() = Info.env.run {
            if (isActive)
                ("$versionString ($versionCode)" + if (isDebug) " (D)" else "").asText()
            else
                CoreR.string.not_available.asText()
        }

    @get:Bindable
    var managerRemoteVersion = CoreR.string.loading.asText()
        set(value) = set(value, field, { field = it }, BR.managerRemoteVersion)

    val managerInstalledVersion
        get() = "${BuildConfig.APP_VERSION_NAME} (${BuildConfig.APP_VERSION_CODE})" +
                if (BuildConfig.DEBUG) " (D)" else ""

    @get:Bindable
    var stateManagerProgress = 0
        set(value) = set(value, field, { field = it }, BR.stateManagerProgress)

    val extraBindings = bindExtra {
        it.put(BR.viewModel, this)
    }

    companion object {
        private var checkedEnv = false
    }

    override suspend fun doLoadWork() {
        appState = State.LOADING
        Info.fetchUpdate(svc)?.apply {
            appState = when {
                BuildConfig.APP_VERSION_CODE < versionCode -> State.OUTDATED
                else -> State.UP_TO_DATE
            }

            val isDebug = Config.updateChannel == Config.Value.DEBUG_CHANNEL
            managerRemoteVersion =
                ("$version (${versionCode})" + if (isDebug) " (D)" else "").asText()
        } ?: run {
            appState = State.INVALID
            managerRemoteVersion = CoreR.string.not_available.asText()
        }
        ensureEnv()
    }

    override fun onNetworkChanged(network: Boolean) = startLoading()

    fun onProgressUpdate(progress: Float, subject: Subject) {
        if (subject is App)
            stateManagerProgress = progress.times(100f).roundToInt()
    }

    fun onLinkPressed(link: String) = object : ViewEvent(), ContextExecutor {
        override fun invoke(context: Context) {
            val intent = Intent(context, SuRequestActivity::class.java).apply {
                data = link.toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
//            val intent = Intent(Intent.ACTION_VIEW, link.toUri())
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                context.toast(CoreR.string.open_link_failed_toast, Toast.LENGTH_SHORT)
            }
        }
    }.publish()

    fun onDeletePressed() = UninstallDialog().show()

    fun onManagerPressed() = when (appState) {
        State.LOADING -> SnackbarEvent(CoreR.string.loading).publish()
        State.INVALID -> SnackbarEvent(CoreR.string.no_connection).publish()
        else -> withExternalRW {
            withInstallPermission {
                ManagerInstallDialog().show()
            }
        }
    }

    fun onMagiskPressed() = withExternalRW {
        HomeFragmentDirections.actionHomeFragmentToInstallFragment().navigate()
    }

    fun hideNotice() {
        Config.safetyNotice = false
        isNoticeVisible = false
    }

    private suspend fun ensureEnv() {
        if (magiskState == State.INVALID || checkedEnv) return
        val cmd = "env_check ${Info.env.versionString} ${Info.env.versionCode}"
        val code = Shell.cmd(cmd).await().code
        if (code != 0) {
            EnvFixDialog(this, code).show()
        }
        checkedEnv = true
    }

    val showTest = false
    fun onTestPressed() = object : ViewEvent(), ActivityExecutor {
        override fun invoke(activity: UIActivity<*>) {
            /* Entry point to trigger test events within the app */
        }
    }.publish()
}
