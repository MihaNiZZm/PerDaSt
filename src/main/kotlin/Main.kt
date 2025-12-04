package com.github.mihanizzm

import com.github.mihanizzm.model.map.PathCopyingPersistentMap

fun main() {
    val squares = PathCopyingPersistentMap<Int, Int>()

    var i = 0
    val resultMap = generateSequence { i += 1; i }
        .take(1000)
        .fold(squares) { acc, i -> acc.put(i, i * i) as PathCopyingPersistentMap<Int, Int> }

    println("${resultMap[0]}, ${resultMap[9]}, ${resultMap[42]}, ${resultMap[99]}, ${resultMap[731]}")
    println("Height: ${resultMap.treeHeight()}")
}
