package com.github.mihanizzm.model.array

import com.github.mihanizzm.model.PersistentCollection

interface PersistentArray<T> : PersistentCollection<T> {
    fun set(index: Int, value: T?): PersistentArray<T>
}
