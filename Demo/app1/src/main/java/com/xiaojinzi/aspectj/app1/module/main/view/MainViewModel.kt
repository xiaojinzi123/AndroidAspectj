package com.xiaojinzi.aspectj.app1.module.main.view

import com.xiaojinzi.aspectj.app1.module.main.domain.MainUseCase
import com.xiaojinzi.aspectj.app1.module.main.domain.MainUseCaseImpl
import com.xiaojinzi.support.annotation.ViewLayer
import com.xiaojinzi.support.architecture.mvvm1.BaseViewModel

@ViewLayer
class MainViewModel(
    private val useCase: MainUseCase = MainUseCaseImpl(),
) : BaseViewModel(),
    MainUseCase by useCase {

    init {
        println("123123")
    }

}