package com.mrt.mvse.android

import com.mrt.mvse.core.MvseEvent
import com.mrt.mvse.core.MvseState
import com.mrt.mvse.core.MvseView

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface MvseViewInitializer<S : MvseState, E : MvseEvent> {
    fun initializeView(view: MvseView<S, E>)
}