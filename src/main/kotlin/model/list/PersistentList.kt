package com.github.mihanizzm.model.list

import com.github.mihanizzm.model.PersistentCollection

interface PersistentList<T> : PersistentCollection<T> {
    fun add(element: T): PersistentList<T>
    fun add(index: Int, element: T): PersistentList<T>
    fun set(index: Int, element: T): PersistentList<T>
    fun remove(index: Int): PersistentList<T>
    fun remove(element: T): PersistentList<T>
    fun first(): T?
    fun last(): T?
    fun asPersistentArray(): com.github.mihanizzm.model.array.PersistentArray<T>
    fun asPersistentMap(): com.github.mihanizzm.model.map.PersistentMap<Int, T>
    fun transaction(block: (MutablePersistentList<T>) -> Unit): PersistentList<T>
}

interface MutablePersistentList<T> : PersistentList<T> {
    fun commit(): PersistentList<T>
    fun rollback()
}