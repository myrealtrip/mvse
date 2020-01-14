package com.mrt.box.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.mrt.box.core.*


/**
 * Created by jaehochoe on 2020-01-03.
 */
abstract class BoxFragment<S : BoxState, E : BoxEvent, SE : BoxWork> : Fragment(),
    BoxView<S, E> {

    abstract val isNeedLazyLoading: Boolean
    private var isBound = false

    private val rendererList: List<BoxRenderer<S, E>> by lazy {
        val list = (extraRenderer() ?: mutableListOf())
        renderer?.let {
            list.add(0, it)
            list
        } ?: list
    }

    abstract val renderer: BoxRenderer<S, E>?

    abstract val viewInitializer: BoxViewInitializer<S, E>?

    abstract val vm: BoxVm<S, E, SE>?

    abstract fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM)

    private lateinit var bindingTemp: ViewDataBinding

    override val binding: ViewDataBinding? by lazyOf(bindingTemp)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (layout > 0) {
            bindingTemp = DataBindingUtil.inflate(inflater, layout, container, false)
            binding?.lifecycleOwner = this
            vm?.let {
                bindingVm(binding, it)
                if(isNeedLazyLoading.not()) {
                    it?.bind(this@BoxFragment)
                    isBound = true
                }
            }
            viewInitializer?.initializeView(this, vm)
            binding?.root
        } else
            super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible && isNeedLazyLoading && !isBound) {
            vm?.bind(this)
            isBound = true
        }
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
    fun extraRenderer(): MutableList<BoxRenderer<S, E>>? {
        return null
    }

    override fun activity(): AppCompatActivity {
        if(activity !is AppCompatActivity)
            error("AppCompatActivity is required")

        return activity as AppCompatActivity
    }
}