package com.mrt.box.android

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.mrt.box.android.event.InAppEvent
import com.mrt.box.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * Created by jaehochoe on 2020-01-01.
 */
abstract class BoxVm<S : BoxState, E : BoxEvent, SE : BoxWork> : ViewModel(),
        CoroutineScope, Vm {

    abstract val bluePrint: BoxBlueprint<S, E, SE>

    private var isInitialized = false
    private var stateInternal: S = bluePrint.initialState
    protected val state: S
        get() = stateInternal

    private val stateLiveData = MutableLiveData<BoxState>()
    private val identifier = Job()
    private val jobs = mutableListOf<Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    private fun model(event: E) {
        val transition: BoxTransition<S, E, SE> = bluePrint.reduce(stateInternal, event)
        Box.log("Intent was $transition")
        if (transition is BoxTransition.Valid) {
            stateInternal = transition.to
            view(stateInternal)
            transition.work?.let { sideEffect ->
                Box.log("Transition has side effect $sideEffect")
                var result: Any? = null
                var doInBackground = false
                var toDo: Any? = bluePrint.findForegroundWork(sideEffect)
                if (toDo == null) {
                    toDo = bluePrint.findBackgroundWork(sideEffect)

                    if (toDo == null)
                        return@model
                    else
                        doInBackground = true
                }
                when (doInBackground) {
                    true -> {
                        val toDo = bluePrint.findBackgroundWork(sideEffect) ?: return@model
                        Box.log("Do in Background: $sideEffect")
                        workThread {
                            result = toDo(transition)?.await()
                            Box.log("Result is $result for $sideEffect")
                            handleResult(result)
                        }
                    }
                    else -> {
                        val toDo = bluePrint.findForegroundWork((sideEffect)) ?: return@model
                        Box.log("Do in Foreground: $sideEffect")
                        result = toDo(transition)
                        Box.log("Result is $result for $sideEffect")
                        handleResult(result)
                    }
                }
            }
        }
    }

    private fun handleResult(result: Any?) {
        when (result) {
            is BoxState -> {
                this.stateInternal = result as S
                mainThread { view(this.stateInternal) }
            }
            is BoxEvent -> {
                mainThread { intent(result as E) }
            }
        }
    }

    private fun view(state: S) {
        this.stateLiveData.value = state
    }

    override fun intent(event: Any) {
        if (isValidEvent(event)) {
            model(event as E)
        } else {
            Box.log("Intent was not BoxEvent")
        }

        linkedVms()?.let {
            for (vm in it) {
                if(vm.isValidEvent(event))
                    vm.intent(event)
            }
        }
    }

    protected fun mainThread(block: suspend () -> Unit) {
        val job = launch {
            block()
        }
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        jobs.add(job)
    }

    protected fun workThread(block: suspend () -> Unit) {
        val job = launch(Dispatchers.Main) {
            block()
        }
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        jobs.add(job)
    }

    fun <V : BoxView<S, E>> bind(view: V) {
        if (view is LifecycleOwner) {
            stateLiveData.observe(view, Observer {
                Box.log("View will view by $it")
                view.render(it as S)
            })
            if (isInitialized.not()) {
                Box.log("Vm has initial state as ${bluePrint.initialState}")
                view(bluePrint.initialState)
                isInitialized = true
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        identifier.cancel()
        jobs.forEach { job ->
            job.cancel()
        }
    }

    open fun onActivityResult(activity: BoxActivity<S, E, SE>, requestCode: Int, resultCode: Int, data: Intent?) {
    }

    open fun subjects(): Array<Int>? {
        return null
    }

    open fun onSubscribe(inAppEvent: InAppEvent) {
    }

    open fun linkedVms(): Array<BoxVm<BoxState, BoxEvent, BoxWork>>? {
        return null
    }
}