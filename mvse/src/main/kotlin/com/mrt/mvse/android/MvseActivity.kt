package com.mrt.mvse.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.mrt.mvse.core.*

/**
 * Created by jaehochoe on 2020-01-03.
 */
abstract class MvseActivity<S : MvseState, E : MvseEvent, SE : MvseSideEffect> : AppCompatActivity(), MvseView<S, E> {

    private val rendererList: List<MvseRenderer<S, E>> by lazy {
        val list = (extraRenderer() ?: mutableListOf())
        renderer?.let {
            list.add(0, it)
            list
        } ?: list
    }
    abstract val renderer: MvseRenderer<S, E>?

    abstract val viewInitializer: MvseViewInitializer<S, E>?

    abstract val vm: MvseVm<S, E, SE>?

    abstract fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM)

    override val binding: ViewDataBinding? by lazy {
        if (layout > 0) DataBindingUtil.setContentView<ViewDataBinding>(this, layout) else null
    }

    open fun preOnCreate(savedInstanceState: Bundle?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preOnCreate(savedInstanceState)
        vm?.let {
            binding?.lifecycleOwner = this
            bindingVm(binding, it)
            it.bind(this@MvseActivity)
        }
        viewInitializer?.initializeView(this, vm)
    }

    override fun render(state: S) {
        for (renderer in rendererList) {
            renderer.render(this, state, vm)
        }
    }

    override fun intent(event: E) {
        vm?.intent(event)
    }

    @Suppress("UNUSED")
    fun extraRenderer(): MutableList<MvseRenderer<S, E>>? {
        return null
    }

    override fun activity(): AppCompatActivity {
        return this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vm?.onActivityResult(this, requestCode, resultCode, data)
    }
}