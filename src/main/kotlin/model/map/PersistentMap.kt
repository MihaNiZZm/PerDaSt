package com.github.mihanizzm.model.map

interface PersistentMap<K: Comparable<K>, V> {
    val size: Int
    operator fun get(key: K): V?
    fun put(key: K, value: V): PersistentMap<K, V>
    fun remove(key: K): PersistentMap<K, V>
    operator fun contains(key: K): Boolean = get(key) != null
    fun keys(): Set<K>
}