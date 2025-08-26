package com.fastjien.sunny.core.di

import android.annotation.SuppressLint
import android.content.Context
import android.text.method.LinkMovementMethod
import androidx.room.Room
import com.fastjien.sunny.core.AppContext
import com.fastjien.sunny.core.Const
import com.fastjien.sunny.core.data.SuLogDatabase
import com.fastjien.sunny.core.data.magiskdb.PolicyDao
import com.fastjien.sunny.core.data.magiskdb.SettingsDao
import com.fastjien.sunny.core.data.magiskdb.StringDao
import com.fastjien.sunny.core.ktx.deviceProtectedContext
import com.fastjien.sunny.core.repository.LogRepository
import com.fastjien.sunny.core.repository.NetworkService
import io.noties.markwon.Markwon
import io.noties.markwon.utils.NoCopySpannableFactory

@SuppressLint("StaticFieldLeak")
object ServiceLocator {

    val deContext by lazy { AppContext.deviceProtectedContext }
    val timeoutPrefs by lazy { deContext.getSharedPreferences("su_timeout", 0) }

    // Database
    val policyDB = PolicyDao()
    val settingsDB = SettingsDao()
    val stringDB = StringDao()
    val sulogDB by lazy { createSuLogDatabase(deContext).suLogDao() }
    val logRepo by lazy { LogRepository(sulogDB) }

    // Networking
    val okhttp by lazy { createOkHttpClient(AppContext) }
    val retrofit by lazy { createRetrofit(okhttp) }
    val markwon by lazy { createMarkwon(AppContext) }
    val networkService by lazy {
        NetworkService(
            createApiService(retrofit, Const.Url.INVALID_URL),
            createApiService(retrofit, Const.Url.GITHUB_API_URL),
        )
    }
}

private fun createSuLogDatabase(context: Context) =
    Room.databaseBuilder(context, SuLogDatabase::class.java, "sulogs.db")
        .addMigrations(SuLogDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()

private fun createMarkwon(context: Context) =
    Markwon.builder(context).textSetter { textView, spanned, bufferType, onComplete ->
        textView.apply {
            movementMethod = LinkMovementMethod.getInstance()
            setSpannableFactory(NoCopySpannableFactory.getInstance())
            setText(spanned, bufferType)
            onComplete.run()
        }
    }.build()
