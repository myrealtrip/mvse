package com.mrt.mvse.android

import com.mrt.mvse.core.MvseEvent
import com.mrt.mvse.core.MvseState
import com.mrt.mvse.core.MvseView
import com.mrt.mvse.core.Vm

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface MvseViewInitializer<S : MvseState, E : MvseEvent> {
    fun initializeView(v: MvseView<S, E>, vm: Vm?)
}