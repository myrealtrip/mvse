package com.mrt.box.core

sealed class BoxTransition<out STATE : BoxState, out EVENT : BoxEvent, out WORK : BoxWork> {
    abstract val from: STATE
    abstract val event: EVENT

    data class Valid<out STATE : BoxState, out EVENT : BoxEvent, out WORK : BoxWork>(
            override val from: STATE,
            override val event: EVENT,
            val to: STATE,
            val work: WORK
    ) : BoxTransition<STATE, EVENT, WORK>()

    data class Invalid<out STATE : BoxState, out EVENT : BoxEvent, out WORK : BoxWork>(
            override val from: STATE,
            override val event: EVENT
    ) : BoxTransition<STATE, EVENT, WORK>()
}