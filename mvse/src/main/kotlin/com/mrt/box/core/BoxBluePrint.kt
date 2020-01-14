package com.mrt.box.core

import com.mrt.box.android.BoxVm
import kotlinx.coroutines.Deferred

/**
 * Created by jaehochoe on 2020-01-01.
 */
class BoxBlueprint<STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork> (
    private val graph: Graph<STATE, EVENT, WORK>
) {

    val initialState = graph.initialState

    fun reduce(state: STATE, event: EVENT): BoxTransition<STATE, EVENT, WORK> {
        return synchronized(this) {
            state.getTransition(event)
        }
    }

    fun findBackgroundWork(work: WORK): (suspend (BoxTransition.Valid<STATE, EVENT, WORK>) -> Deferred<Any?>?)? {
        return synchronized(this) {
            work.getBackgroundWork()
        }
    }

    fun findForegroundWork(work: WORK): ((BoxTransition.Valid<STATE, EVENT, WORK>) -> Any?)? {
        return synchronized(this) {
            work.getForegroundWork()
        }
    }

    private fun STATE.getTransition(event: EVENT): BoxTransition<STATE, EVENT, WORK> {
        for ((eventMatcher, createTransitionTo) in getDefinition().transitions) {
            if (eventMatcher.matches(event)) {
                val (toState, work) = createTransitionTo(this, event)
                return BoxTransition.Valid(this, event, toState, work)
            }
        }
        return BoxTransition.Invalid(this, event)
    }

    private fun STATE.getDefinition() = graph.stateDefinitions
        .filter { it.key.matches(this) }
        .map { it.value }
        .firstOrNull() ?: error("Missing definition for state ${this.javaClass.simpleName}!")

    private fun WORK.getBackgroundWork() = graph.doInBackground
        .filter { it.key.matches(this) }
        .map { it.value }
        .firstOrNull()

    private fun WORK.getForegroundWork() = graph.doInForeground
        .filter { it.key.matches(this) }
        .map { it.value }
        .firstOrNull()

    data class Graph<STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork>(
            val initialState: STATE,
            val stateDefinitions: Map<Matcher<STATE, STATE>, State<STATE, EVENT, WORK>>,
            val doInBackground: Map<Matcher<WORK, WORK>, suspend (BoxTransition.Valid<STATE, EVENT, WORK>) -> Deferred<Any?>?>,
            val doInForeground: Map<Matcher<WORK, WORK>, (BoxTransition.Valid<STATE, EVENT, WORK>) -> Any?>
    ) {

        class State<STATE : Any, EVENT : Any, WORK : Any> internal constructor() {
            val transitions =
                linkedMapOf<Matcher<EVENT, EVENT>, (STATE, EVENT) -> TransitionTo<STATE, WORK>>()

            data class TransitionTo<out STATE : Any, out WORK : Any> internal constructor(
                val toState: STATE,
                val work: WORK
            )
        }
    }

    class GraphBuilder<STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork>(graph: Graph<STATE, EVENT, WORK>? = null) {
        private var initialState = graph?.initialState
        private val stateDefinitions = LinkedHashMap(graph?.stateDefinitions ?: emptyMap())
        private val doInBackground = LinkedHashMap(graph?.doInBackground ?: emptyMap())
        private val doInForeground = LinkedHashMap(graph?.doInForeground ?: emptyMap())

        fun initialState(initialState: STATE) {
            this.initialState = initialState
        }

        fun <S : STATE> state(
            stateMatcher: Matcher<STATE, S>,
            init: StateDefinitionBuilder<S>.() -> Unit
        ) {
            stateDefinitions[stateMatcher] = StateDefinitionBuilder<S>().apply(init).build()
        }

        inline fun <reified S : STATE> state(noinline init: StateDefinitionBuilder<S>.() -> Unit) {
            state(Matcher.any(), init)
        }

        inline fun <reified S : STATE> state(
            state: S,
            noinline init: StateDefinitionBuilder<S>.() -> Unit
        ) {
            state(Matcher.eq<STATE, S>(state), init)
        }

        fun <W : WORK> workInBackground(
            matcher: Matcher<WORK, W>,
            init: suspend (BoxTransition.Valid<STATE, EVENT, W>) -> Deferred<Any?>?
        ) {
            doInBackground[matcher] = init as (suspend (BoxTransition.Valid<STATE, EVENT, WORK>) -> Deferred<Any?>)
        }

        inline fun <reified W : WORK> workInBackground(
            noinline init: suspend (BoxTransition.Valid<STATE, EVENT, W>) -> Deferred<Any?>?
        ) {
            workInBackground(Matcher.any(), init)
        }

        fun <W : WORK> work(
            matcher: Matcher<WORK, W>,
            init: (BoxTransition.Valid<STATE, EVENT, W>) -> Any?
        ) {
            doInForeground[matcher] = init as (BoxTransition.Valid<STATE, EVENT, WORK>) -> Any?
        }

        inline fun <reified W : WORK> work(
            noinline init: (BoxTransition.Valid<STATE, EVENT, W>) -> Any?
        ) {
            work(Matcher.any(), init)
        }

        fun build(): Graph<STATE, EVENT, WORK> {
            return Graph(
                requireNotNull(initialState),
                stateDefinitions.toMap(),
                doInBackground.toMap(),
                doInForeground.toMap()
            )
        }

        inner class StateDefinitionBuilder<S : STATE> {

            private val stateDefinition = Graph.State<STATE, EVENT, WORK>()

            inline fun <reified E : EVENT> any(): Matcher<EVENT, E> = Matcher.any()

            inline fun <reified R : EVENT> eq(value: R): Matcher<EVENT, R> = Matcher.eq(value)

            fun <E : EVENT> event(
                eventMatcher: Matcher<EVENT, E>,
                createTransitionTo: S.(E) -> Graph.State.TransitionTo<STATE, WORK>
            ) {
                stateDefinition.transitions[eventMatcher] = { state, event ->
                    @Suppress("UNCHECKED_CAST")
                    createTransitionTo((state as S), event as E)
                }
            }

            inline fun <reified E : EVENT> event(
                noinline createTransitionTo: S.(E) -> Graph.State.TransitionTo<STATE, WORK>
            ) {
                return event(any(), createTransitionTo)
            }

            inline fun <reified E : EVENT> event(
                event: E,
                noinline createTransitionTo: S.(E) -> Graph.State.TransitionTo<STATE, WORK>
            ) {
                return event(eq(event), createTransitionTo)
            }

            fun build() = stateDefinition

            @Suppress("UNUSED") // The unused warning is probably a compiler bug.
            fun S.toBe(state: STATE, work: WORK? = null) =
                Graph.State.TransitionTo(state, work ?: BoxVoidWork as WORK)

            @Suppress("UNUSED") // The unused warning is probably a compiler bug.
            fun S.toBeNothing(work: WORK? = null) = toBe(this, work)
        }
    }
}

fun <STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork> BoxVm<STATE, EVENT, WORK>.bluePrint(
    init: BoxBlueprint.GraphBuilder<STATE, EVENT, WORK>.() -> Unit
): BoxBlueprint<STATE, EVENT, WORK> {
    return bluePrint(null, init)
}

fun <STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork> BoxVm<STATE, EVENT, WORK>.bluePrint(
        graph: BoxBlueprint.Graph<STATE, EVENT, WORK>?,
        init: BoxBlueprint.GraphBuilder<STATE, EVENT, WORK>.() -> Unit
): BoxBlueprint<STATE, EVENT, WORK> {
    return BoxBlueprint(BoxBlueprint.GraphBuilder(graph).apply(init).build())
}