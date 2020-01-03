package com.mrt.mvse.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.mrt.mvse.core.*

/**
 * Created by jaehochoe on 2020-01-03.
 */
abstract class MvseActivity<S : MvseState, E : MvseEvent, SE : MvseSideEffect> : AppCompatActivity(), MvseView<S, E> {

    private val rendererList: List<MvseRenderer> by lazy {
        val list = (extraRenderer() ?: mutableListOf())
        renderer?.let {
            list.add(0, it)
            list
        } ?: list
    }
    abstract val renderer: MvseRenderer?
    abstract val viewInitializer: MvseViewInitializer<S, E>?
    abstract val vm: MvseVm<S, E, SE>?
    abstract fun <B : ViewDataBinding, VM : MvseEventHandler> bindingVm(b: B?, vm: VM)

    override val binding: ViewDataBinding? by lazy {
        if (layout > 0) DataBindingUtil.setContentView<ViewDataBinding>(this, layout) else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm?.let {
            binding?.lifecycleOwner = this
            bindingVm(binding, it)
            it.bind(this@MvseActivity)
        }
        viewInitializer?.initializeView(this)
    }

    override fun render(state: S) {
        for (renderer in rendererList) {
            renderer.render(this, state, vm)
        }
    }

    override fun intends(event: E) {
        vm?.intends(event)
    }

    @Suppress("UNUSED")
    fun extraRenderer(): MutableList<MvseRenderer>? {
        return null
    }

    override fun activity(): AppCompatActivity {
        return this
    }
}