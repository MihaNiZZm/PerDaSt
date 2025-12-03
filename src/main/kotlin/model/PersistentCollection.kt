package com.github.mihanizzm.model

interface PersistentCollection<T> : Iterable<T> {
    val size: Int
    operator fun get(index: Int): T?
}