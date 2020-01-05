package com.mrt.mvse.repo.local

/**
 * Created by jaehochoe on 2019-09-24.
 */
interface Storage<K, V> {
    fun put(namespace: String = DEFAULT, key: K, value: V): Boolean
    fun get(namespace: String = DEFAULT, key: K): V?
    fun contains(namespace: String = DEFAULT, key: K): Boolean
    fun remove(namespace: String = DEFAULT, key: K): Boolean
    fun wipe(namespace: String = DEFAULT): Boolean
}

const val DEFAULT = "default"