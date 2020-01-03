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
abstract class MvseVm<S : MvseState, E : MvseEvent, SE : MvseSideEffect> : ViewModel(), CoroutineScope, MvseEventHandler {

    abstract val bluePrint: MvcoBluePrint<S, E, SE>

    private var isInitialized = false
    private var state: S = bluePrint.initialState
    private val stateLiveData = MutableLiveData<MvseState>()
    private val identifier = Job()
    private val jobs = mutableListOf<Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    override fun onCleared() {
        super.onCleared()
        identifier.cancel()
        jobs.forEach { job ->
            job.cancel()
        }
    }

    override fun intends(event: Any) {
        if(typeCheck(event).not()) {
            Mvse.log("Intent was not MvseEvent")
            return
        }

        val transition: MvcoBluePrint.Transition<S, E, SE> = bluePrint.reduce(state, event as E)
        Mvse.log("Intent was $transition")
        if (transition is MvcoBluePrint.Transition.Valid) {
            state = transition.toState
            render(state)
            transition.sideEffect?.let { sideEffect ->
                Mvse.log("Transition has side effect $sideEffect")
                workThread {
                    val toDo = bluePrint.findSideEffect(sideEffect) ?: return@workThread
                    val result = toDo(transition).await()
                    Mvse.log("Result is $result for $sideEffect")
                    when (result) {
                        is MvseState -> {
                            mainThread { render(result as S) }
                        }
                        is MvseEvent -> {
                            mainThread { intends(result as E) }
                        }
                    }
                }
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

    private fun workThread(block: suspend () -> Unit) {
        val job = launch(Dispatchers.Main) {
            block()
        }
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        jobs.add(job)
    }


    private fun render(state: S) {
        this.stateLiveData.value = state
    }

    fun <V : MvseView<S, E>> bind(view: V) {
        if (view is LifecycleOwner) {
            stateLiveData.observe(view, Observer {
                Mvse.log("View will render by $it")
                view.render(it as S)
            })
            if(isInitialized.not()) {
                Mvse.log("Vm has initial state as ${bluePrint.initialState}")
                render(bluePrint.initialState)
                isInitialized = true
            }
        }
    }
}