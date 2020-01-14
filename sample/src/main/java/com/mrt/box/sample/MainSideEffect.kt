package com.mrt.box.sample

import com.mrt.box.core.BoxWork

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainSideEffect : BoxWork {
    data class AutoCountUp(val count: Int) : MainSideEffect()
    data class Finish(val action: () -> Unit) : MainSideEffect()
}