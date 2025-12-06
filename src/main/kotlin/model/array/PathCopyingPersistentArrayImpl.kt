package com.github.mihanizzm.model.array

private const val BITS = 5
private const val BRANCH_FACTOR = 1 shl BITS
private const val MASK = BRANCH_FACTOR - 1

sealed class Node<T>

class Leaf<T>(val items: Array<T?>) : Node<T>()
class Branch<T>(val children: Array<Node<T>?>) : Node<T>()

class PathCopyingPersistentArray<T> private constructor(
    private val root: Node<T>,
    override val size: Int,
    private val depth: Int,
) : PersistentArray<T> {

    companion object {
        fun <T> fromList(list: List<T>): PersistentArray<T?> {
            val size = list.size
            val depth = computeNeededDepth(size)
            val paddedList = padList(list, 1 shl ((depth + 1) * BITS))
            val root = buildNode(paddedList, depth)
            return PathCopyingPersistentArray(root, size, depth)
        }

        fun <T> ofSize(size: Int): PersistentArray<T?> {
            val depth = computeNeededDepth(size)
            val paddedList = padList(listOf<T>(), 1 shl ((depth + 1) * BITS))
            val root = buildNode(paddedList, depth)
            return PathCopyingPersistentArray(root, size, depth)
        }

        private fun computeNeededDepth(size: Int): Int {
            var d = 0
            var cap = BRANCH_FACTOR
            while (cap < size) {
                cap = cap shl BITS
                d += 1
            }
            return d
        }

        private fun <T> padList(lst: List<T?>, toSize: Int): List<T?> {
            return lst + List(toSize - lst.size) { null }
        }

        private fun <T> buildNode(list: List<T>, depth: Int): Node<T> {
            if (depth == 0) {
                @Suppress("UNCHECKED_CAST")
                val arr = arrayOfNulls<Any?>(BRANCH_FACTOR) as Array<T?>
                for (i in 0 until minOf(BRANCH_FACTOR, list.size)) {
                    arr[i] = list.getOrNull(i)
                }
                return Leaf(arr)
            }
            val arr = arrayOfNulls<Node<T>>(BRANCH_FACTOR)
            val blockSize = list.size / BRANCH_FACTOR
            for (i in 0 until BRANCH_FACTOR) {
                val start = i * blockSize
                val end = minOf((i+1)*blockSize, list.size)
                if (start < end)
                    arr[i] = buildNode(list.subList(start, end), depth-1)
            }
            return Branch(arr)
        }
    }

    override fun get(index: Int): T? {
        require(index in 0 until size)
        var node: Node<T> = root
        for (level in depth downTo 1) {
            val childIdx = (index shr (level * BITS)) and MASK
            node = (node as Branch).children[childIdx]
                ?: throw IllegalStateException("Corrupted tree structure!")
        }
        val leafIdx = index and MASK
        return (node as Leaf).items[leafIdx]
    }

    override fun set(index: Int, value: T?): PersistentArray<T> {
        require(index in 0 until size)
        fun setRec(node: Node<T>, level: Int): Node<T> {
            return if (level == 0) {
                val oldItems = (node as Leaf).items
                val newItems = oldItems.copyOf()
                newItems[index and MASK] = value
                Leaf(newItems)
            } else {
                val b = node as Branch
                val idx = (index shr (level * BITS)) and MASK
                val childrenCopy = b.children.copyOf()
                childrenCopy[idx] = setRec(b.children[idx]!!, level - 1)
                Branch(childrenCopy)
            }
        }
        val newRoot = setRec(root, depth)
        return PathCopyingPersistentArray(newRoot, size, depth)
    }

    override fun iterator(): Iterator<T> = toList().iterator()

    private fun toList(): List<T> {
        val result = ArrayList<T>(size)
        fun traverse(node: Node<T>, level: Int, count: Int) {
            when (node) {
                is Leaf -> {
                    for (i in 0 until BRANCH_FACTOR) {
                        if (result.size >= size) break
                        val value = node.items[i]
                        if (value != null) result.add(value)
                    }
                }
                is Branch -> {
                    for (i in 0 until BRANCH_FACTOR) {
                        val child = node.children[i]
                        if (child != null) traverse(child, level + 1, result.size)
                        if (result.size >= size) break
                    }
                }
            }
        }
        traverse(root, 0, 0)
        return result
    }
}