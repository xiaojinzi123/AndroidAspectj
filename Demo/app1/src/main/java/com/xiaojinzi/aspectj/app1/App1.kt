package com.xiaojinzi.aspectj.app1

import android.app.Application
import com.xiaojinzi.support.init.AppInstance

class App1 : Application() {

    override fun onCreate() {
        super.onCreate()

        AppInstance.app = this

    }

}