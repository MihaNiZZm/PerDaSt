package com.github.mihanizzm.model.map

data class Node<K: Comparable<K>, V>(
    val key: K,
    val value: V,
    val left: Node<K, V>? = null,
    val right: Node<K, V>? = null,
    val height: Int = 1,
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

    override fun put(key: K, value: V): PathCopyingPersistentMap<K, V> {
        var added = false
        fun putRec(node: Node<K, V>?): Node<K, V> {
            if (node == null) {
                added = true
                return Node(key, value)
            }
            return when {
                key < node.key -> balance(node.copy(left = putRec(node.left)))
                key > node.key -> balance(node.copy(right = putRec(node.right)))
                else -> node.copy(value = value)
            }
        }
        val newRoot = putRec(root)
        return PathCopyingPersistentMap(newRoot, if (added) size + 1 else size)
    }

    override fun remove(key: K): PathCopyingPersistentMap<K, V> {
        var removed = false
        fun findMin(node: Node<K, V>): Node<K, V> =
            node.left?.let { findMin(it) } ?: node
        fun removeMin(node: Node<K, V>): Node<K, V>? =
            if (node.left == null) node.right else balance(node.copy(left = removeMin(node.left)))
        fun removeRec(node: Node<K, V>?): Node<K, V>? {
            if (node == null) return null
            return when {
                key < node.key -> balance(node.copy(left = removeRec(node.left)))
                key > node.key -> balance(node.copy(right = removeRec(node.right)))
                else -> {
                    removed = true
                    when {
                        node.left == null -> node.right
                        node.right == null -> node.left
                        else -> {
                            val min = findMin(node.right)
                            balance(node.copy(key = min.key, value = min.value, right = removeMin(node.right)))
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

    fun treeHeight() = height(root)

    fun printTree(node: Node<K, V>? = root as Node<K, V>, prefix: String = "", isLeft: Boolean = true) {
        if (node == null) return
        printTree(node.right, prefix + if (isLeft) "│   " else "    ", false)

        println(prefix +
            (if (isLeft) "└── " else "┌── ") +
            "${node.key}[h=${node.height}]"
        )

        printTree(node.left, prefix + if (isLeft) "    " else "│   ", true)
    }

    private fun height(node: Node<K, V>?): Int = node?.height ?: 0


    private fun balanceFactor(node: Node<K, V>?): Int =
        height(node?.right) - height(node?.left)


    private fun updateHeight(node: Node<K, V>): Node<K, V> {
        val newHeight = maxOf(height(node.left), height(node.right)) + 1
        return node.copy(height = newHeight)
    }

    private fun rotateRight(node: Node<K, V>): Node<K, V> {
        val q = node.left!!

        val newP = updateHeight(node.copy(left = q.right))
        return updateHeight(q.copy(right = newP))
    }

    private fun rotateLeft(node: Node<K, V>): Node<K, V> {
        val p = node.right!!

        val newQ = updateHeight(node.copy(right = p.left))
        return updateHeight(p.copy(left = newQ))
    }

    private fun balance(node: Node<K, V>): Node<K, V> {
        var newNode = updateHeight(node)
        if (balanceFactor(newNode) == 2) {
            if (balanceFactor(newNode.right) < 0) {
                newNode = newNode.copy(right = rotateRight(newNode.right!!))
            }
            return rotateLeft(newNode)
        }
        if (balanceFactor(newNode) == -2) {
            if (balanceFactor(newNode.left) > 0) {
                newNode = newNode.copy(left = rotateLeft(newNode.left!!))
            }
            return rotateRight(newNode)
        }
        return newNode
    }
}
