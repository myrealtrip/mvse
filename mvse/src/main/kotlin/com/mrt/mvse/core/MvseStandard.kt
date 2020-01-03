package com.mrt.mvse.core

import androidx.databinding.ViewDataBinding
import com.mrt.mvse.android.MvseVm

/**
 * Created by jaehochoe on 2020-01-03.
 */
fun <B : ViewDataBinding> ViewDataBinding?.be(): B {
    return this as B
}

fun <S : MvseState, E : MvseEvent, SE : MvseSideEffect> MvseVm<S, E, SE>.typeCheck(event: Any) : Boolean {
    if(event !is MvseEvent)
        return false

    try {
        event as E
    } catch (e: Exception) {
        Mvse.log(e)
        return false
    }

    return true
}