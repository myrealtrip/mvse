package com.mrt.box.android

import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxState
import com.mrt.box.core.BoxView
import com.mrt.box.core.Vm

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface BoxViewInitializer<S : BoxState, E : BoxEvent> {
    fun initializeView(v: BoxView<S, E>, vm: Vm?)
}