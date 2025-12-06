package com.github.mihanizzm.model

class PersistentHistory<T>(initial: T) {
    private val history = mutableListOf(initial)
    private var pointer = 0

    fun cur(): T = history[pointer]

    fun update(newVersion: T): T {
        if (pointer < history.lastIndex) {
            history.subList(pointer + 1, history.size).clear()
        }
        history.add(newVersion)
        pointer++
        return cur()
    }

    fun undo(): T {
        if (pointer > 0) pointer--
        return cur()
    }

    fun redo(): T {
        if (pointer < history.lastIndex) pointer++
        return cur()
    }

    fun canUndo() = pointer > 0
    fun canRedo() = pointer < history.lastIndex
}

