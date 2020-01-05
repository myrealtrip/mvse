package com.mrt.mvse.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.mrt.mvse.core.*


/**
 * Created by jaehochoe on 2020-01-03.
 */
abstract class MvseFragment<S : MvseState, E : MvseEvent, SE : MvseSideEffect> : Fragment(),
    MvseView<S, E> {

    abstract val isNeedLazyLoading: Boolean
    private var isBound = false

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
                    it?.bind(this@MvseFragment)
                    isBound = true
                }
            }
            viewInitializer?.initializeView(this)
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
    fun extraRenderer(): MutableList<MvseRenderer>? {
        return null
    }

    override fun activity(): AppCompatActivity {
        if(activity !is AppCompatActivity)
            error("AppCompatActivity is required")

        return activity as AppCompatActivity
    }
}