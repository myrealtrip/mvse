package com.mrt.box.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.mrt.box.android.event.InAppEvent
import com.mrt.box.android.event.event.BoxInAppEvent.asChannel
import com.mrt.box.core.*
import kotlinx.coroutines.launch

/**
 * Created by jaehochoe on 2020-01-03.
 */
abstract class BoxActivity<S : BoxState, E : BoxEvent, SE : BoxWork> : AppCompatActivity(), BoxView<S, E> {

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
            it.bind(this@BoxActivity)
            viewInitializer?.bindingVm(binding, it)
        }
        viewInitializer?.initializeView(this, vm)

        vm?.launch {
            val channel = asChannel<InAppEvent>()
            var isNeedSkipFirstEvent = channel.isEmpty.not()
            for (inAppEvent in channel) {
                if(isNeedSkipFirstEvent.not()) {
                    Box.log("InAppEvent = $inAppEvent in ${this@BoxActivity}")
                    onSubscribe(inAppEvent)
                } else
                    isNeedSkipFirstEvent = false
            }
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
        return this
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        vm?.onNewIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vm?.onActivityResult(this, requestCode, resultCode, data)
    }

    private fun subjects(): Array<Int>? {
        return vm?.subjects()
    }

    private fun onSubscribe(inAppEvent: InAppEvent) {
        vm?.onSubscribe(inAppEvent)
    }
}