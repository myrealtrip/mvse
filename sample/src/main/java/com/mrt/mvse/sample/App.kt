package com.mrt.mvse.sample

import android.app.Application
import android.util.Log
import com.mrt.mvse.core.Mvse

/**
 * Created by jaehochoe on 2020-01-03.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Mvse.enableLog {
            Log.e("MVCO", it)
        }
    }
}