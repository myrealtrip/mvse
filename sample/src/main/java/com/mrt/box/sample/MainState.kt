package com.mrt.box.sample

import com.mrt.box.core.BoxState

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainState : BoxState {
    data class Count(var count: Int = 0) : MainState()
    object Clean : MainState()
}