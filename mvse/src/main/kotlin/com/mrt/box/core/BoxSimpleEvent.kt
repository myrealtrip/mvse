package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-05.
 */
interface ToWork<SE : BoxWork> {
    fun toWork(): SE
}