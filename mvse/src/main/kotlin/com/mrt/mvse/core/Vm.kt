package com.mrt.mvse.core

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface Vm {
    fun intent(event: Any)
    fun intent(className: String, vararg arguments: Any) {
        Class.forName(className)?.let { clazz ->
            try {
                intent(clazz.constructors[0].newInstance(*arguments))
            } catch (e: Exception) {
                try {
                    intent(
                        clazz.getConstructor(*arguments.map { it::class.java as Class<*> }.toTypedArray()).newInstance(
                            *arguments
                        )
                    )
                } catch (e: Exception) {
                }
            }
        }
    }
}