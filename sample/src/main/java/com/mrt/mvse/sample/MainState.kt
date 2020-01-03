package com.mrt.mvse.sample

import com.mrt.mvse.core.MvseState

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainState : MvseState {
    data class Count(var count: Int = 0) : MainState()
    object Clean : MainState()
}