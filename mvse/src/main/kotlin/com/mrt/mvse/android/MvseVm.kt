package com.mrt.mvse.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.mrt.mvse.core.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


/**
 * Created by jaehochoe on 2020-01-01.
 */
abstract class MvseVm<S : MvseState, E : MvseEvent, SE : MvseSideEffect> : ViewModel(),
    CoroutineScope, Vm {

    abstract val bluePrint: MvcoBluePrint<S, E, SE>

    private var isInitialized = false
    private var state: S = bluePrint.initialState
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
                val toDo = bluePrint.findSideEffect(sideEffect) ?: return@model
                when (sideEffect) {
                    is DoInWorkThread -> {
                        Mvse.log("Do in Background: $sideEffect")
                        workThread {
                            val result = toDo(transition).await()
                            Mvse.log("Result is $result for $sideEffect")
                            when (result) {
                                is MvseState -> {
                                    mainThread { view(result as S) }
                                }
                                is MvseEvent -> {
                                    mainThread { intent(result as E) }
                                }
                            }
                        }
                    }
                    else -> {
                        mainThread { toDo(transition) }
                    }
                }
            }
        }
    }

    private fun view(state: S) {
        this.stateLiveData.value = state
    }

    override fun intent(event: Any) {
        if (typeCheck(event)) {
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

    private fun workThread(block: suspend () -> Unit) {
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
}