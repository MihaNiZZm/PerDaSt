package com.github.mihanizzm.model.list

import com.github.mihanizzm.model.PersistentCollection
import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.map.PathCopyingPersistentMap

class PathCopyingPersistentList<T> private constructor(
    private val head: ListNode<T>?,
    private val tail: ListNode<T>?,
    override val size: Int,
    private val version: Long = System.nanoTime(),
    private val history: List<Long> = emptyList()
) : PersistentList<T>, MutablePersistentList<T> {

    companion object {
        private const val MAX_HISTORY = 100
    }

    constructor() : this(null, null, 0)

    private constructor(
        head: ListNode<T>?,
        tail: ListNode<T>?,
        size: Int,
        version: Long,
        history: List<Long>
    ) : this(head, tail, size) {
        (this as? MutableList<T>)?.let {
            it.historyState = history
            it.currentVersion = version
        }
    }

    override fun get(index: Int): T? {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        var current = head
        repeat(index) { current = current?.next }
        return current?.value
    }

    override fun add(element: T): PersistentList<T> {
        val newNode = ListNode(element, tail, null)
        return when {
            head == null -> PathCopyingPersistentList(
                newNode, newNode, 1, 
                newVersion(), history + version
            )
            else -> {
                val newTail = tail?.copyWithNext(newNode)
                PathCopyingPersistentList(
                    head, newTail?.next, size + 1,
                    newVersion(), history + version
                )
            }
        }
    }

    override fun add(index: Int, element: T): PersistentList<T> {
        require(index in 0..size) { "Index $index out of bounds for size $size" }
        
        return when (index) {
            0 -> {
                val newNode = ListNode(element, null, head)
                val newHead = head?.copyWithPrev(newNode)
                PathCopyingPersistentList(
                    newNode, if (size == 0) newNode else tail,
                    size + 1, newVersion(), history + version
                )
            }
            size -> add(element)
            else -> {
                var prevNode = head
                repeat(index - 1) { prevNode = prevNode?.next }
                val nextNode = prevNode?.next
                val newNode = ListNode(element, prevNode, nextNode)
                val newPrev = prevNode?.copyWithNext(newNode)
                val newNext = nextNode?.copyWithPrev(newNode)
                PathCopyingPersistentList(
                    if (index == 1) newPrev?.next ?: newNode else head,
                    tail, size + 1, newVersion(), history + version
                )
            }
        }
    }

    override fun set(index: Int, element: T): PersistentList<T> {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        
        var current = head
        var prev: ListNode<T>? = null
        repeat(index) {
            prev = current
            current = current?.next
        }
        
        val newNode = ListNode(element, prev, current?.next)
        
        val newHead = if (index == 0) newNode else copyPathToIndex(head, index - 1, newNode)
        val newTail = if (index == size - 1) newNode else tail
        
        return PathCopyingPersistentList(
            newHead, newTail, size,
            newVersion(), history + version
        )
    }

    override fun remove(index: Int): PersistentList<T> {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        
        return when {
            size == 1 -> PathCopyingPersistentList(
                null, null, 0, newVersion(), history + version
            )
            index == 0 -> {
                val newHead = head?.next?.copyWithPrev(null)
                PathCopyingPersistentList(
                    newHead, tail, size - 1, newVersion(), history + version
                )
            }
            index == size - 1 -> { 
                val newTail = tail?.prev?.copyWithNext(null)
                PathCopyingPersistentList(
                    head, newTail, size - 1, newVersion(), history + version
                )
            }
            else -> { 
                var prevNode = head
                repeat(index - 1) { prevNode = prevNode?.next }
                val nodeToRemove = prevNode?.next
                val nextNode = nodeToRemove?.next
                val newPrev = prevNode?.copyWithNext(nextNode)
                val newNext = nextNode?.copyWithPrev(newPrev)
                PathCopyingPersistentList(
                    if (index == 1) newPrev?.next ?: newNext else head,
                    tail, size - 1, newVersion(), history + version
                )
            }
        }
    }

    override fun remove(element: T): PersistentList<T> {
        var current = head
        var index = 0
        while (current != null) {
            if (current.value == element) {
                return remove(index)
            }
            current = current.next
            index++
        }
        return this
    }

    override fun first(): T? = head?.value
    override fun last(): T? = tail?.value

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var current = head
        
        override fun hasNext(): Boolean = current != null
        
        override fun next(): T {
            val value = current?.value ?: throw NoSuchElementException()
            current = current?.next
            return value
        }
    }

    private val versionHistory = mutableMapOf<Long, PathCopyingPersistentList<T>>()
    private var currentVersion = version
    private var historyState = history.takeLast(MAX_HISTORY).toMutableList()
    
    fun undo(): PersistentList<T>? {
        if (historyState.isEmpty()) return null
        val prevVersion = historyState.removeLast()
        return versionHistory[prevVersion]
    }
    
    fun redo(): PersistentList<T>? {
        // (требует хранения отмененных версий)
        return null // Упрощенно
    }
    
    private fun newVersion(): Long {
        val newVersion = System.nanoTime()
        versionHistory[version] = this
        historyState.add(version)
        if (historyState.size > MAX_HISTORY) {
            historyState.removeFirst()
        }
        return newVersion
    }

    override fun asPersistentArray(): com.github.mihanizzm.model.array.PersistentArray<T> {
        val array = PathCopyingPersistentArray.ofSize<T>(size)
        var current = head
        var index = 0
        var result = array
        while (current != null) {
            result = result.set(index, current.value) as PathCopyingPersistentArray<T>
            current = current.next
            index++
        }
        return result
    }

    override fun asPersistentMap(): com.github.mihanizzm.model.map.PersistentMap<Int, T> {
        var map = PathCopyingPersistentMap<Int, T>()
        var current = head
        var index = 0
        while (current != null) {
            map = map.put(index, current.value) as PathCopyingPersistentMap<Int, T>
            current = current.next
            index++
        }
        return map
    }

    private class Transaction<T>(
        val original: PathCopyingPersistentList<T>,
        var workingCopy: MutableList<T> = original.toMutableList()
    ) {
        var committed = false
        var rolledBack = false
    }
    
    private val transactions = ThreadLocal<Transaction<T>?>()
    
    override fun transaction(block: (MutablePersistentList<T>) -> Unit): PersistentList<T> {
        val transaction = Transaction(this)
        transactions.set(transaction)
        
        try {
            block(object : MutablePersistentList<T> {
                override fun commit(): PersistentList<T> {
                    if (transaction.committed || transaction.rolledBack) {
                        throw IllegalStateException("Transaction already completed")
                    }
                    transaction.committed = true
                    var result = PathCopyingPersistentList<T>()
                    for (item in transaction.workingCopy) {
                        result = result.add(item) as PathCopyingPersistentList<T>
                    }
                    transactions.set(null)
                    return result
                }
                
                override fun rollback() {
                    transaction.rolledBack = true
                    transactions.set(null)
                }
                
                override fun add(element: T) = apply { 
                    transaction.workingCopy.add(element) 
                }.let { this }
                
                override fun add(index: Int, element: T) = apply {
                    transaction.workingCopy.add(index, element)
                }.let { this }
                
                override fun set(index: Int, element: T) = apply {
                    transaction.workingCopy[index] = element
                }.let { this }
                
                override fun remove(index: Int) = apply {
                    transaction.workingCopy.removeAt(index)
                }.let { this }
                
                override fun remove(element: T) = apply {
                    transaction.workingCopy.remove(element)
                }.let { this }
                
                override fun first(): T? = transaction.workingCopy.firstOrNull()
                override fun last(): T? = transaction.workingCopy.lastOrNull()
                override fun asPersistentArray() = throw UnsupportedOperationException()
                override fun asPersistentMap() = throw UnsupportedOperationException()
                override fun transaction(block: (MutablePersistentList<T>) -> Unit) = 
                    throw UnsupportedOperationException("Nested transactions not supported")
                
                override val size: Int get() = transaction.workingCopy.size
                override fun get(index: Int): T? = transaction.workingCopy.getOrNull(index)
                override fun iterator(): Iterator<T> = transaction.workingCopy.iterator()
            })
            
            return transaction.committed ?: this
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }

    override fun commit(): PersistentList<T> {
        // Для STM - возвращаем текущее состояние
        return this
    }

    override fun rollback() {
        // Для STM - сбрасываем транзакцию
        transactions.set(null)
    }

    private fun copyPathToIndex(
        start: ListNode<T>?, 
        targetIndex: Int, 
        newNode: ListNode<T>
    ): ListNode<T>? {
        if (start == null) return null
        
        val result = start.copyWithNext(
            if (targetIndex == 0) newNode else copyPathToIndex(start.next, targetIndex - 1, newNode)
        )
        
        return if (targetIndex == 0) result.copyWithNext(newNode) else result
    }

    override fun toString(): String {
        val elements = mutableListOf<T>()
        var current = head
        while (current != null) {
            elements.add(current.value)
            current = current.next
        }
        return elements.toString()
    }
}