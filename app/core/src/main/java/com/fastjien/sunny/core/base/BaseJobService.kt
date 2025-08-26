package com.fastjien.sunny.core.base

import android.app.job.JobService
import android.content.Context
import com.fastjien.sunny.core.patch

abstract class BaseJobService : JobService() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.patch())
    }
}
