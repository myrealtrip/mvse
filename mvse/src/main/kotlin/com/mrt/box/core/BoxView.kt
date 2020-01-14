package com.mrt.box.core

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding


/**
 * Created by jaehochoe on 2020-01-01.
 */
interface BoxView<S: BoxState, E: BoxEvent> {
    val binding: ViewDataBinding?
    val layout: Int

    open fun render(state: S)
    open fun intent(event: E)
    open fun activity(): AppCompatActivity

    fun <B : ViewDataBinding> binding(): B {
        return binding.be()
    }
}