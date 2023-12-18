package com.xiaojinzi.aspectj.app1.module.main.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.xiaojinzi.aspectj.base.theme.CommonTheme
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseAct
import com.xiaojinzi.support.compose.StateBar
import com.xiaojinzi.support.ktx.initOnceUseViewModel
import com.xiaojinzi.support.ktx.translateStatusBar
import kotlinx.coroutines.InternalCoroutinesApi

@ViewLayer
class MainAct : BaseAct<MainViewModel>() {

    override fun getViewModelClass(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    @OptIn(
        ExperimentalFoundationApi::class,
        InternalCoroutinesApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalPagerApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.translateStatusBar()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        initOnceUseViewModel {
        }

        setContent {
            CommonTheme {
                StateBar {
                    MainViewWrap()
                }
            }
        }

    }

}