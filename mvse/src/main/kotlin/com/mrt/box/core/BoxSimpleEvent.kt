package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-05.
 */
interface ToSideEffect<SE : BoxWork> {
    fun toSideEffect(): SE
}