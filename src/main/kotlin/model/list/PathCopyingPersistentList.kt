package com.github.mihanizzm.model.list

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
        
        if (index < size / 2) {
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
    override fun add(element: T?): PathCopyingPersistentList<T> {
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
            PathCopyingPersistentList(newFirstNode, lastNode, size + 1)
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
            PathCopyingPersistentList(newFirstNode, lastNode, size - 1)
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
        if (lastNode == null) {
            throw NoSuchElementException("Cannot remove from empty list")
        }
        
        return if (size == 1) {
            PathCopyingPersistentList(null, null, 0)
        } else {
            val newLastNode = lastNode.prev!!
            
            fun copyChain(node: ListNode<T>?): ListNode<T>? {
                if (node == null) return null
                
                if (node == newLastNode) {
                    return ListNode(node.value, node.prev, null)
                }
                
                if (node.prev == newLastNode) {
                    return copyChain(node.next)
                }
                
                return ListNode(node.value, node.prev, copyChain(node.next))
            }
            
            val newFirstNode = copyChain(firstNode)
            PathCopyingPersistentList(newFirstNode, newLastNode, size - 1)
        }
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
            index == size -> add(element)
            else -> {
                fun findAndInsert(node: ListNode<T>?, currentIndex: Int): ListNode<T>? {
                    if (node == null) return null
                    
                    return if (currentIndex == index - 1) {
                        val newNode = ListNode(element, node, node.next)
                        ListNode(node.value, node.prev, newNode)
                    } else {
                        ListNode(
                            node.value,
                            node.prev,
                            findAndInsert(node.next, currentIndex + 1)
                        )
                    }
                }
                
                val newFirstNode = findAndInsert(firstNode, 0)
                PathCopyingPersistentList(newFirstNode, lastNode, size + 1)
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
                fun findAndRemove(node: ListNode<T>?, currentIndex: Int): ListNode<T>? {
                    if (node == null) return null
                    
                    return if (currentIndex == index - 1) {
                        val nodeToRemove = node.next!!
                        ListNode(
                            node.value,
                            node.prev,
                            nodeToRemove.next?.let { nextNode ->
                                ListNode(nextNode.value, node, nextNode.next)
                            }
                        )
                    } else {
                        ListNode(
                            node.value,
                            node.prev,
                            findAndRemove(node.next, currentIndex + 1)
                        )
                    }
                }
                
                val newFirstNode = findAndRemove(firstNode, 0)
                PathCopyingPersistentList(newFirstNode, lastNode, size - 1)
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
                ListNode(element, node.prev, node.next)
            } else {
                ListNode(
                    node.value,
                    node.prev,
                    copyAndUpdate(node.next, currentIndex + 1)
                )
            }
        }
        
        val newFirstNode = copyAndUpdate(firstNode, 0)
        return PathCopyingPersistentList(newFirstNode, lastNode, size)
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