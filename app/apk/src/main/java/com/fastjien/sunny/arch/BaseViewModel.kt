package com.fastjien.sunny.arch

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.fastjien.sunny.core.R
import com.fastjien.sunny.databinding.ObservableHost
import com.fastjien.sunny.events.BackPressEvent
import com.fastjien.sunny.events.DialogBuilder
import com.fastjien.sunny.events.DialogEvent
import com.fastjien.sunny.events.NavigationEvent
import com.fastjien.sunny.events.PermissionEvent
import com.fastjien.sunny.events.SnackbarEvent

abstract class BaseViewModel : ViewModel(), ObservableHost {

    override var callbacks: PropertyChangeRegistry? = null

    private val _viewEvents = MutableLiveData<ViewEvent>()
    val viewEvents: LiveData<ViewEvent> get() = _viewEvents

    open fun onSaveState(state: Bundle) {}
    open fun onRestoreState(state: Bundle) {}
    open fun onNetworkChanged(network: Boolean) {}

    fun withPermission(permission: String, callback: (Boolean) -> Unit) {
        PermissionEvent(permission, callback).publish()
    }

    inline fun withExternalRW(crossinline callback: () -> Unit) {
        withPermission(WRITE_EXTERNAL_STORAGE) {
            if (!it) {
                SnackbarEvent(R.string.external_rw_permission_denied).publish()
            } else {
                callback()
            }
        }
    }

    @SuppressLint("InlinedApi")
    inline fun withInstallPermission(crossinline callback: () -> Unit) {
        withPermission(REQUEST_INSTALL_PACKAGES) {
            if (!it) {
                SnackbarEvent(R.string.install_unknown_denied).publish()
            } else {
                callback()
            }
        }
    }

    @SuppressLint("InlinedApi")
    inline fun withPostNotificationPermission(crossinline callback: () -> Unit) {
        withPermission(POST_NOTIFICATIONS) {
            if (!it) {
                SnackbarEvent(R.string.post_notifications_denied).publish()
            } else {
                callback()
            }
        }
    }

    fun back() = BackPressEvent().publish()

    fun ViewEvent.publish() {
        _viewEvents.postValue(this)
    }

    fun DialogBuilder.show() {
        DialogEvent(this).publish()
    }

    fun NavDirections.navigate(pop: Boolean = false) {
        _viewEvents.postValue(NavigationEvent(this, pop))
    }

}
