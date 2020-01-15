package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-05.
 */
interface ToWork<W : BoxWork> {
    fun toWork(): W
}