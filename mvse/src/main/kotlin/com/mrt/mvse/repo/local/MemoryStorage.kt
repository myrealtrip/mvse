package com.mrt.mvse.repo.local

/**
 * Created by jaehochoe on 2019-09-24.
 */
class MemoryStorage<V> : Storage<String, V> {

    val storage = hashMapOf<String, V>()

    override fun put(namespace: String, key: String, value: V): Boolean {
        return storage.put(key, value) != null
    }

    override fun get(namespace: String, key: String): V? {
        return storage.get(key)
    }

    override fun contains(namespace: String, key: String): Boolean {
        return storage.containsKey(key)
    }

    override fun remove(namespace: String, key: String): Boolean {
        return storage.remove(key) != null
    }

    override fun wipe(namespace: String): Boolean {
        storage.clear()
        return true
    }
}