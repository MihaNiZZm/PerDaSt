package com.github.mihanizzm.model

interface UndoRedoable {
    fun undo(): UndoRedoable?
    fun redo(): UndoRedoable?
    fun canUndo(): Boolean
    fun canRedo(): Boolean
    fun saveCheckpoint()
}

class UndoRedoManager<T : UndoRedoable> {
    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()
    private var current: T
    
    constructor(initial: T) {
        current = initial
        undoStack.addLast(initial)
    }
    
    fun execute(action: (T) -> T): T {
        redoStack.clear()
        val newState = action(current)
        undoStack.addLast(current)
        current = newState
        return current
    }
    
    fun undo(): T? {
        if (undoStack.size < 2) return null
        redoStack.addLast(current)
        undoStack.removeLast()
        current = undoStack.last()
        return current
    }
    
    fun redo(): T? {
        if (redoStack.isEmpty()) return null
        current = redoStack.removeLast()
        undoStack.addLast(current)
        return current
    }
    
    fun getCurrent(): T = current
}