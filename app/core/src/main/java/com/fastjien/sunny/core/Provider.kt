package com.fastjien.sunny.core

import android.os.Bundle
import com.fastjien.sunny.core.base.BaseProvider
import com.fastjien.sunny.core.su.SuCallbackHandler

class Provider : BaseProvider() {

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return when (method) {
            SuCallbackHandler.LOG, SuCallbackHandler.NOTIFY -> {
                SuCallbackHandler.run(context!!, method, extras)
                Bundle.EMPTY
            }

            else -> Bundle.EMPTY
        }
    }
}
