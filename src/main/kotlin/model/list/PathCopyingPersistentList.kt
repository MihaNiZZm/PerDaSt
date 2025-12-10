package com.github.mihanizzm.model.list

import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.array.PersistentArray

/**
 * Внутренний узел двусвязного персистентного списка.
 *
 * @param value значение узла (может быть `null`)
 * @param prev ссылка на предыдущий узел
 * @param next ссылка на следующий узел
 */
internal data class ListNode<T>(
    val value: T?,
    val prev: ListNode<T>?,
    val next: ListNode<T>?
)

/**
 * Реализация персистентного двусвязного списка на основе алгоритма path copying.
 *
 * Основные принципы:
 * - Все операции модификации возвращают новую версию списка, оставляя предыдущие неизменными
 * - При изменениях копируются только узлы вдоль пути к модифицируемому элементу
 * - Неизмененные узлы переиспользуются между версиями
 * - Поддерживает эффективный доступ к элементам по индексу (O(n/2) в худшем случае)
 * - Совместим с [PersistentHistory] для поддержки undo/redo операций
 *
 * @param T тип элементов списка
 */
class PathCopyingPersistentList<T> private constructor(
    private val firstNode: ListNode<T>?,
    private val lastNode: ListNode<T>?,
    override val size: Int
) : PersistentList<T> {

    /**
     * Создает пустой персистентный список.
     */
    constructor() : this(null, null, 0)

    companion object {
        /**
         * Создает персистентный список из персистентного массива.
         *
         * @param array исходный персистентный массив
         * @return персистентный список с теми же элементами
         */
        fun <T> fromPersistentArray(array: PersistentArray<T?>): PathCopyingPersistentList<T> {
            val elements = mutableListOf<T?>()
            for (i in 0 until array.size) {
                elements.add(array.get(i))
            }
            
            var result = PathCopyingPersistentList<T>()
            for (element in elements) {
                result = result.addLast(element)
            }
            return result
        }
        
        /**
         * Создает персистентный список из обычного списка.
         *
         * @param elements список элементов
         * @return персистентный список с указанными элементами
         */
        fun <T> fromList(elements: List<T?>): PathCopyingPersistentList<T> {
            var result = PathCopyingPersistentList<T>()
            for (element in elements) {
                result = result.addLast(element)
            }
            return result
        }
    }

    /**
     * Возвращает элемент по указанному индексу или `null`, если элемент отсутствует.
     *
     * Оптимизация: поиск начинается с того конца списка, который ближе к целевому индексу.
     *
     * @param index позиция элемента (0-based)
     * @return элемент на позиции [index] или `null`
     * @throws IllegalArgumentException если индекс выходит за пределы [0, size)
     */
    override fun get(index: Int): T? {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        
        if (index <= size / 2) {
            var current = firstNode
            var currentIndex = 0
            while (current != null) {
                if (currentIndex == index) return current.value
                current = current.next
                currentIndex++
            }
        } else {
            var current = lastNode
            var currentIndex = size - 1
            while (current != null) {
                if (currentIndex == index) return current.value
                current = current.prev
                currentIndex--
            }
        }
        
        return null
    }

    /**
     * Возвращает итератор по не-null элементам списка.
     *
     * Элементы перебираются в порядке от первого к последнему.
     * Null-значения пропускаются.
     */
    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            private var currentNode = firstNode
            
            override fun hasNext(): Boolean {
                var current = currentNode
                while (current != null && current.value == null) {
                    current = current.next
                }
                return current != null
            }
            
            override fun next(): T {
                var current = currentNode
                while (current != null && current.value == null) {
                    current = current.next
                }
                
                if (current == null) {
                    throw NoSuchElementException()
                }
                
                currentNode = current.next
                return current.value ?: throw NoSuchElementException("Null value in list")
            }
        }
    }

    /**
     * Добавляет элемент в конец списка.
     *
     * Копирует только путь от первого до последнего узла (path copying).
     *
     * @param element добавляемый элемент (может быть `null`)
     * @return новая версия списка с добавленным элементом
     */
    override fun addLast(element: T?): PathCopyingPersistentList<T> {
        return if (lastNode == null) {
            val newNode = ListNode(element, null, null)
            PathCopyingPersistentList(newNode, newNode, 1)
        } else {
            val newLastNode = ListNode(element, lastNode, null)
            
            fun copyChain(node: ListNode<T>?): ListNode<T>? {
                if (node == null) return null
                
                return if (node == lastNode) {
                    ListNode(node.value, node.prev, newLastNode)
                } else {
                    ListNode(node.value, node.prev, copyChain(node.next))
                }
            }
            
            val newFirstNode = copyChain(firstNode)
            PathCopyingPersistentList(newFirstNode, newLastNode, size + 1)
        }
    }

    /**
     * Добавляет элемент в начало списка.
     *
     * @param element добавляемый элемент (может быть `null`)
     * @return новая версия списка с добавленным элементом в начале
     */
    override fun addFirst(element: T?): PathCopyingPersistentList<T> {
        return if (firstNode == null) {
            val newNode = ListNode(element, null, null)
            PathCopyingPersistentList(newNode, newNode, 1)
        } else {
            val newFirstNode = ListNode(element, null, firstNode)
            
            // Обновляем prev у старого первого узла
            val updatedOldFirst = ListNode(firstNode.value, newFirstNode, firstNode.next)
            
            // Копируем остальную цепочку
            fun copyChain(node: ListNode<T>?): ListNode<T>? {
                if (node == null) return null
                
                return if (node == firstNode) {
                    updatedOldFirst
                } else {
                    ListNode(node.value, node.prev, copyChain(node.next))
                }
            }
            
            val newLastNode = copyChain(lastNode)
            PathCopyingPersistentList(newFirstNode, newLastNode, size + 1)
        }
    }

    /**
     * Удаляет первый элемент списка.
     *
     * @return новая версия списка без первого элемента
     * @throws NoSuchElementException если список пуст
     */
    override fun removeFirst(): PathCopyingPersistentList<T> {
        if (firstNode == null) {
            throw NoSuchElementException("Cannot remove from empty list")
        }
        
        return if (size == 1) {
            PathCopyingPersistentList(null, null, 0)
        } else {
            val newFirstNode = firstNode.next!!
            // Обновляем prev у нового первого узла
            val updatedNewFirst = ListNode(newFirstNode.value, null, newFirstNode.next)
            
            fun copyChain(node: ListNode<T>?): ListNode<T>? {
                if (node == null) return null
                
                return if (node == newFirstNode) {
                    updatedNewFirst
                } else {
                    ListNode(node.value, node.prev, copyChain(node.next))
                }
            }
            
            val newLastNode = copyChain(lastNode)
            PathCopyingPersistentList(updatedNewFirst, newLastNode, size - 1)
        }
    }

    /**
     * Удаляет последний элемент списка.
     *
     * Копирует только путь от первого до предпоследнего узла (path copying).
     *
     * @return новая версия списка без последнего элемента
     * @throws NoSuchElementException если список пуст
     */
    override fun removeLast(): PathCopyingPersistentList<T> {
        if (lastNode == null) throw NoSuchElementException("Cannot remove from empty list")
        if (size == 1) return PathCopyingPersistentList(null, null, 0)
        
        val newLastNode = lastNode.prev!!
        
        val updatedNewLast = ListNode(newLastNode.value, newLastNode.prev, null)
        
        fun copyChain(node: ListNode<T>?): ListNode<T>? {
            if (node == null) {
                return null
            }
            
            if (node == newLastNode) {
                return updatedNewLast
            }
            
            if (node == lastNode) {
                return copyChain(node.next)
            }
            
            val newNext = copyChain(node.next)
            val result = ListNode(node.value, node.prev, newNext)
            return result
        }
        
        val newFirstNode = copyChain(firstNode)
        
        val result = PathCopyingPersistentList(newFirstNode, updatedNewLast, size - 1)
        
        return result
    }

    /**
     * Вставляет элемент по указанному индексу.
     *
     * @param index позиция для вставки (0-based)
     * @param element вставляемый элемент (может быть `null`)
     * @return новая версия списка с вставленным элементом
     * @throws IllegalArgumentException если индекс выходит за пределы [0, size]
     */
    override fun insert(index: Int, element: T?): PathCopyingPersistentList<T> {
        require(index in 0..size) { "Index $index out of bounds for size $size. Valid range: 0..$size" }
        
        return when {
            index == 0 -> addFirst(element)
            index == size -> addLast(element)
            else -> {
                // Находим узел перед позицией вставки и копируем цепочку
                fun findAndInsert(node: ListNode<T>?, currentIndex: Int): ListNode<T>? {
                    if (node == null) return null
                    
                    return if (currentIndex == index - 1) {
                        // Создаем новый узел
                        val newNode = ListNode(element, node, node.next)
                        // Обновляем текущий узел
                        ListNode(node.value, node.prev, newNode)
                    } else {
                        // Копируем дальше
                        ListNode(
                            node.value,
                            node.prev,
                            findAndInsert(node.next, currentIndex + 1)
                        )
                    }
                }
                
                val newFirstNode = findAndInsert(firstNode, 0)
                
                // Находим новый последний узел
                var newLastNode = newFirstNode
                while (newLastNode?.next != null) {
                    newLastNode = newLastNode.next
                }
                
                PathCopyingPersistentList(newFirstNode, newLastNode, size + 1)
            }
        }
    }

    /**
     * Удаляет элемент по указанному индексу.
     *
     * @param index позиция удаляемого элемента (0-based)
     * @return новая версия списка без удаленного элемента
     * @throws IllegalArgumentException если индекс выходит за пределы [0, size)
     */
    override fun removeAt(index: Int): PathCopyingPersistentList<T> {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        
        return when {
            index == 0 -> removeFirst()
            index == size - 1 -> removeLast()
            else -> {
                // Находим узел перед удаляемым и копируем цепочку
                fun findAndRemove(node: ListNode<T>?, currentIndex: Int): ListNode<T>? {
                    if (node == null) return null
                    
                    return if (currentIndex == index - 1) {
                        // node - узел перед удаляемым
                        val nodeToRemove = node.next!!
                        // Связываем текущий узел со следующим после удаляемого
                        val newNext = nodeToRemove.next
                        ListNode(
                            node.value,
                            node.prev,
                            if (newNext != null) {
                                // Обновляем prev у следующего узла
                                ListNode(newNext.value, node, newNext.next)
                            } else {
                                null
                            }
                        )
                    } else {
                        // Копируем дальше
                        ListNode(
                            node.value,
                            node.prev,
                            findAndRemove(node.next, currentIndex + 1)
                        )
                    }
                }
                
                val newFirstNode = findAndRemove(firstNode, 0)
                
                // Находим новый последний узел
                var newLastNode = newFirstNode
                while (newLastNode?.next != null) {
                    newLastNode = newLastNode.next
                }
                
                PathCopyingPersistentList(newFirstNode, newLastNode, size - 1)
            }
        }
    }

    /**
     * Заменяет элемент по указанному индексу.
     *
     * Копирует путь до указанного индекса, заменяя значение в целевом узле.
     *
     * @param index позиция заменяемого элемента (0-based)
     * @param element новое значение элемента (может быть `null`)
     * @return новая версия списка с замененным элементом
     * @throws IllegalArgumentException если индекс выходит за пределы [0, size)
     */
    override fun set(index: Int, element: T?): PathCopyingPersistentList<T> {
        require(index in 0 until size) { "Index $index out of bounds for size $size" }
        
        fun copyAndUpdate(node: ListNode<T>?, currentIndex: Int): ListNode<T>? {
            if (node == null) return null
            
            return if (currentIndex == index) {
                // Заменяем значение в узле
                ListNode(element, node.prev, node.next)
            } else {
                // Копируем дальше
                ListNode(
                    node.value,
                    node.prev,
                    copyAndUpdate(node.next, currentIndex + 1)
                )
            }
        }
        
        val newFirstNode = copyAndUpdate(firstNode, 0)
        
        // Находим новый последний узел (он может измениться, если изменились ссылки)
        var newLastNode = newFirstNode
        while (newLastNode?.next != null) {
            newLastNode = newLastNode.next
        }
        
        return PathCopyingPersistentList(newFirstNode, newLastNode, size)
    }

    /**
     * Преобразует список в персистентный массив.
     *
     * Создает новую версию персистентного массива, содержащую те же элементы
     * в том же порядке. Преобразование выполняется за O(n) времени и памяти.
     *
     * @return персистентный массив с элементами списка
     */
    fun toPersistentArray(): PersistentArray<T?> {
        if (size == 0) {
            return PathCopyingPersistentArray.ofSize(0)
        }
        
        val elements = mutableListOf<T?>()
        var current = firstNode
        var count = 0
        while (current != null && count < size) {
            elements.add(current.value)
            current = current.next
            count++
        }
        
        return PathCopyingPersistentArray.fromList(elements)
    }

    /**
     * Возвращает первый элемент списка или `null`, если список пуст.
     */
    override fun first(): T? = firstNode?.value

    /**
     * Возвращает последний элемент списка или `null`, если список пуст.
     */
    override fun last(): T? = lastNode?.value
}