package com.mrt.mvse.core

import com.mrt.mvse.android.MvseVm
import kotlinx.coroutines.Deferred

/**
 * Created by jaehochoe on 2020-01-01.
 * Inspired by StateMachine@Tinder
 */
class MvcoBluePrint<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> constructor(
    private val graph: Graph<STATE, EVENT, SIDE_EFFECT>
) {

    val initialState = graph.initialState

    fun reduce(state: STATE, event: EVENT): Transition<STATE, EVENT, SIDE_EFFECT> {
        return synchronized(this) {
            state.getTransition(event)
        }
    }

    fun findSideEffect(sideEffect: SIDE_EFFECT): (suspend (Transition.Valid<STATE, EVENT, SIDE_EFFECT>) -> Deferred<Any>)? {
        return synchronized(this) {
            sideEffect.getSideEffect()
        }
    }

    private fun STATE.getTransition(event: EVENT): Transition<STATE, EVENT, SIDE_EFFECT> {
        for ((eventMatcher, createTransitionTo) in getDefinition().transitions) {
            if (eventMatcher.matches(event)) {
                val (toState, sideEffect) = createTransitionTo(this, event)
                return Transition.Valid(this, event, toState, sideEffect)
            }
        }
        return Transition.Invalid(this, event)
    }

    private fun STATE.getDefinition() = graph.stateDefinitions
        .filter { it.key.matches(this) }
        .map { it.value }
        .firstOrNull() ?: error("Missing definition for state ${this.javaClass.simpleName}!")

    private fun SIDE_EFFECT.getSideEffect() = graph.sideEffectDefinitions
        .filter { it.key.matches(this) }
        .map { it.value }
        .firstOrNull()

    @Suppress("UNUSED")
    sealed class Transition<out STATE : Any, out EVENT : Any, out SIDE_EFFECT : Any> {
        abstract val fromState: STATE
        abstract val event: EVENT

        data class Valid<out STATE : Any, out EVENT : Any, out SIDE_EFFECT : Any> internal constructor(
            override val fromState: STATE,
            override val event: EVENT,
            val toState: STATE,
            val sideEffect: SIDE_EFFECT?
        ) : Transition<STATE, EVENT, SIDE_EFFECT>()

        data class Invalid<out STATE : Any, out EVENT : Any, out SIDE_EFFECT : Any> internal constructor(
            override val fromState: STATE,
            override val event: EVENT
        ) : Transition<STATE, EVENT, SIDE_EFFECT>()
    }

    data class Graph<STATE : Any, EVENT : Any, SIDE_EFFECT : Any>(
        val initialState: STATE,
        val stateDefinitions: Map<Matcher<STATE, STATE>, State<STATE, EVENT, SIDE_EFFECT>>,
        val sideEffectDefinitions: Map<Matcher<SIDE_EFFECT, SIDE_EFFECT>, suspend (Transition.Valid<STATE, EVENT, SIDE_EFFECT>) -> Deferred<Any>>
    ) {

        class State<STATE : Any, EVENT : Any, SIDE_EFFECT : Any> internal constructor() {
            val transitions =
                linkedMapOf<Matcher<EVENT, EVENT>, (STATE, EVENT) -> TransitionTo<STATE, SIDE_EFFECT>>()

            data class TransitionTo<out STATE : Any, out SIDE_EFFECT : Any> internal constructor(
                val toState: STATE,
                val sideEffect: SIDE_EFFECT?
            )
        }
    }

    class Matcher<T : Any, out R : T> private constructor(private val clazz: Class<R>) {

        private val predicates = mutableListOf<(T) -> Boolean>({ clazz.isInstance(it) })

        fun where(predicate: R.() -> Boolean): Matcher<T, R> = apply {
            predicates.add {
                @Suppress("UNCHECKED_CAST")
                (it as R).predicate()
            }
        }

        fun matches(value: T) = predicates.all { it(value) }

        companion object {
            fun <T : Any, R : T> any(clazz: Class<R>): Matcher<T, R> = Matcher(clazz)

            inline fun <T : Any, reified R : T> any(): Matcher<T, R> = any(R::class.java)

            inline fun <T : Any, reified R : T> eq(value: R): Matcher<T, R> =
                any<T, R>().where { this == value }
        }
    }

    class GraphBuilder<STATE : Any, EVENT : Any, SIDE_EFFECT : Any>(
        graph: Graph<STATE, EVENT, SIDE_EFFECT>? = null
    ) {
        private var initialState = graph?.initialState
        private val stateDefinitions = LinkedHashMap(graph?.stateDefinitions ?: emptyMap())
        private val sideEffectDefinitions = LinkedHashMap(graph?.sideEffectDefinitions ?: emptyMap())

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

        fun <SE : SIDE_EFFECT> sideEffect(
            matcher: Matcher<SIDE_EFFECT, SE>,
            init: suspend (Transition.Valid<STATE, EVENT, SE>) -> Any
        ) {
            sideEffectDefinitions[matcher] = init as (suspend (Transition.Valid<STATE, EVENT, SIDE_EFFECT>) -> Deferred<Any>)
        }

        inline fun <reified SE : SIDE_EFFECT> sideEffect(
            noinline init: suspend (Transition.Valid<STATE, EVENT, SE>) -> Any
        ) {
            sideEffect(Matcher.any(), init)
        }

        fun build(): Graph<STATE, EVENT, SIDE_EFFECT> {
            return Graph(
                requireNotNull(initialState),
                stateDefinitions.toMap(),
                sideEffectDefinitions.toMap()
            )
        }

        inner class StateDefinitionBuilder<S : STATE> {

            private val stateDefinition = Graph.State<STATE, EVENT, SIDE_EFFECT>()

            inline fun <reified E : EVENT> any(): Matcher<EVENT, E> = Matcher.any()

            inline fun <reified R : EVENT> eq(value: R): Matcher<EVENT, R> = Matcher.eq(value)

            fun <E : EVENT> event(
                eventMatcher: Matcher<EVENT, E>,
                createTransitionTo: S.(E) -> Graph.State.TransitionTo<STATE, SIDE_EFFECT>
            ) {
                stateDefinition.transitions[eventMatcher] = { state, event ->
                    @Suppress("UNCHECKED_CAST")
                    createTransitionTo((state as S), event as E)
                }
            }

            inline fun <reified E : EVENT> event(
                noinline createTransitionTo: S.(E) -> Graph.State.TransitionTo<STATE, SIDE_EFFECT>
            ) {
                return event(any(), createTransitionTo)
            }

            inline fun <reified E : EVENT> event(
                event: E,
                noinline createTransitionTo: S.(E) -> Graph.State.TransitionTo<STATE, SIDE_EFFECT>
            ) {
                return event(eq(event), createTransitionTo)
            }

            fun build() = stateDefinition

            @Suppress("UNUSED") // The unused warning is probably a compiler bug.
            fun S.toBe(state: STATE, sideEffect: SIDE_EFFECT? = null) =
                Graph.State.TransitionTo(state, sideEffect)

            @Suppress("UNUSED") // The unused warning is probably a compiler bug.
            fun S.toBeNothing(sideEffect: SIDE_EFFECT? = null) = toBe(this, sideEffect)
        }
    }
}

fun <STATE : MvseState, EVENT : MvseEvent, SIDE_EFFECT : MvseSideEffect> MvseVm<STATE, EVENT, SIDE_EFFECT>.bluePrint(
    init: MvcoBluePrint.GraphBuilder<STATE, EVENT, SIDE_EFFECT>.() -> Unit
): MvcoBluePrint<STATE, EVENT, SIDE_EFFECT> {
    return bluePrint(null, init)
}

fun <STATE : MvseState, EVENT : MvseEvent, SIDE_EFFECT : MvseSideEffect> MvseVm<STATE, EVENT, SIDE_EFFECT>.bluePrint(
    graph: MvcoBluePrint.Graph<STATE, EVENT, SIDE_EFFECT>?,
    init: MvcoBluePrint.GraphBuilder<STATE, EVENT, SIDE_EFFECT>.() -> Unit
): MvcoBluePrint<STATE, EVENT, SIDE_EFFECT> {
    return MvcoBluePrint(MvcoBluePrint.GraphBuilder(graph).apply(init).build())
}