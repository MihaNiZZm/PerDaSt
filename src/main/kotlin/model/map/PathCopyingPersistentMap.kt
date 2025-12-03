package com.github.mihanizzm.model.map

data class Node<K: Comparable<K>, V>(
    val key: K,
    val value: V,
    val left: Node<K, V>? = null,
    val right: Node<K, V>? = null,
)

class PathCopyingPersistentMap<K: Comparable<K>, V> private constructor(
    private val root: Node<K, V>?,
    override val size: Int
): PersistentMap<K, V> {

    constructor(): this(null, 0)

    override fun get(key: K): V? {
        var node = root
        while (node != null) {
            node = when {
                key < node.key -> node.left
                key > node.key -> node.right
                else -> return node.value
            }
        }
        return null
    }

    override fun put(key: K, value: V): PersistentMap<K, V> {
        var added = false
        fun putRec(node: Node<K, V>?): Node<K, V> {
            if (node == null) {
                added = true
                return Node(key, value)
            }
            return when {
                key < node.key -> node.copy(left = putRec(node.left))
                key > node.key -> node.copy(right = putRec(node.right))
                else -> node.copy(value = value)
            }
        }
        val newRoot = putRec(root)
        return PathCopyingPersistentMap(newRoot, if (added) size + 1 else size)
    }

    override fun remove(key: K): PersistentMap<K, V> {
        var removed = false
        fun findMin(node: Node<K, V>): Node<K, V> =
            node.left?.let { findMin(it) } ?: node
        fun removeMin(node: Node<K, V>): Node<K, V>? =
            if (node.left == null) node.right else node.copy(left = removeMin(node.left))
        fun removeRec(node: Node<K, V>?): Node<K, V>? {
            if (node == null) return null
            return when {
                key < node.key -> node.copy(left = removeRec(node.left))
                key > node.key -> node.copy(right = removeRec(node.right))
                else -> {
                    removed = true
                    when {
                        node.left == null -> node.right
                        node.right == null -> node.left
                        else -> {
                            val min = findMin(node.right)
                            node.copy(key = min.key, value = min.value, right = removeMin(node.right))
                        }
                    }
                }
            }
        }

        val newRoot = removeRec(root)
        return if (removed) PathCopyingPersistentMap(newRoot, size - 1) else this
    }

    override fun keys(): Set<K> {
        fun inOrder(node: Node<K, V>?, result: MutableSet<K>) {
            if (node == null) return
            inOrder(node.left, result)
            result.add(node.key)
            inOrder(node.right, result)
        }
        val result = mutableSetOf<K>()
        inOrder(root, result)
        return result
    }
}
