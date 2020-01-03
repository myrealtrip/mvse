package com.mrt.mvse.core

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding


/**
 * Created by jaehochoe on 2020-01-01.
 */
interface MvseView<S: MvseState, E: MvseEvent> {
    val binding: ViewDataBinding?
    val layout: Int

    open fun render(state: S)
    open fun intends(event: E)
    open fun activity(): AppCompatActivity

    fun <B : ViewDataBinding> binding(): B {
        return binding.be()
    }
}