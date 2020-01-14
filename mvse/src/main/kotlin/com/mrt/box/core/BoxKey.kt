package com.mrt.box.core

class BoxKey<T : Any, out R : T>(private val clazz: Class<R>) {
    private val matcher: (T) -> Boolean = { clazz.isInstance(it) }
    fun check(value: T) = matcher(value)
}