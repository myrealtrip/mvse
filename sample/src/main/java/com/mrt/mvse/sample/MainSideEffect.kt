package com.mrt.mvse.sample

import com.mrt.mvse.core.MvseSideEffect

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainSideEffect : MvseSideEffect {
    data class AutoCountUp(val count: Int) : MainSideEffect()
    data class Finish(val action: () -> Unit) : MainSideEffect()
}