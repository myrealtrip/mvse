package com.mrt.mvse.core

/**
 * Created by jaehochoe on 2020-01-05.
 */
interface ToSideEffect<SE : MvseSideEffect> {
    fun toSideEffect(): SE
}