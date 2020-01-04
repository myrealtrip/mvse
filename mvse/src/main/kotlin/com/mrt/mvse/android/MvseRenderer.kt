package com.mrt.mvse.android

import com.mrt.mvse.core.MvseEvent
import com.mrt.mvse.core.Vm
import com.mrt.mvse.core.MvseState
import com.mrt.mvse.core.MvseView

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface MvseRenderer {
    fun <S : MvseState, E : MvseEvent> render(
        view: MvseView<S, E>,
        state: MvseState,
        handler: Vm?
    )
}