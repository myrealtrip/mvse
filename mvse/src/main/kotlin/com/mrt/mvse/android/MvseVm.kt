package com.mrt.mvse.android

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.mrt.mvse.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * Created by jaehochoe on 2020-01-01.
 */
abstract class MvseVm<S : MvseState, E : MvseEvent, SE : MvseSideEffect> : ViewModel(),
        CoroutineScope, Vm {

    abstract val bluePrint: MvcoBluePrint<S, E, SE>

    private var isInitialized = false
    protected var state: S = bluePrint.initialState
    private val stateLiveData = MutableLiveData<MvseState>()
    private val identifier = Job()
    private val jobs = mutableListOf<Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    private fun model(event: E) {
        val transition: MvcoBluePrint.Transition<S, E, SE> = bluePrint.reduce(state, event)
        Mvse.log("Intent was $transition")
        if (transition is MvcoBluePrint.Transition.Valid) {
            state = transition.toState
            view(state)
            transition.sideEffect?.let { sideEffect ->
                Mvse.log("Transition has side effect $sideEffect")
                var result: Any? = null
                var doInBackground = false
                var toDo: Any? = bluePrint.findForegroundSideEffect(sideEffect)
                if (toDo == null) {
                    toDo = bluePrint.findBackgroundSideEffect(sideEffect)

                    if (toDo == null)
                        return@model
                    else
                        doInBackground = true
                }
                when (doInBackground) {
                    true -> {
                        val toDo = bluePrint.findBackgroundSideEffect(sideEffect) ?: return@model
                        Mvse.log("Do in Background: $sideEffect")
                        workThread {
                            result = toDo(transition)?.await()
                            Mvse.log("Result is $result for $sideEffect")
                            handleResult(result)
                        }
                    }
                    else -> {
                        val toDo = bluePrint.findForegroundSideEffect((sideEffect)) ?: return@model
                        Mvse.log("Do in Foreground: $sideEffect")
                        result = toDo(transition)
                        Mvse.log("Result is $result for $sideEffect")
                        handleResult(result)
                    }
                }
            }
        }
    }

    private fun handleResult(result: Any?) {
        when (result) {
            is MvseState -> {
                this.state = result as S
                mainThread { view(this.state) }
            }
            is MvseEvent -> {
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
            Mvse.log("Intent was not MvseEvent")
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

    fun <V : MvseView<S, E>> bind(view: V) {
        if (view is LifecycleOwner) {
            stateLiveData.observe(view, Observer {
                Mvse.log("View will view by $it")
                view.render(it as S)
            })
            if (isInitialized.not()) {
                Mvse.log("Vm has initial state as ${bluePrint.initialState}")
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

    open fun onActivityResult(activity: MvseActivity<S, E, SE>, requestCode: Int, resultCode: Int, data: Intent?) {
    }
}