package com.mrt.box.core

import androidx.databinding.ViewDataBinding
import com.mrt.box.android.BoxVm

/**
 * Created by jaehochoe on 2020-01-03.
 */
fun <B : ViewDataBinding> ViewDataBinding?.be(): B {
    return this as B
}

fun <S : BoxState, E : BoxEvent, SE : BoxWork> BoxVm<S, E, SE>.isValidEvent(event: Any) : Boolean {
    if(event !is BoxEvent)
        return false

    try {
        event as E
    } catch (e: Exception) {
        Box.log(e)
        return false
    }

    return true
}