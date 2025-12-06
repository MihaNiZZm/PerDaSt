package com.github.mihanizzm

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.map.PathCopyingPersistentMap

fun main() {
    val squares = PathCopyingPersistentMap<Int, Int>()
    val squaresHistory = PersistentHistory(squares)

    var i = 0
    val resultMap = generateSequence { i += 1; i }
        .take(1000)
        .fold(squares) { _, i ->
            squaresHistory.update(squaresHistory.cur().put(i, i * i))
        }

    println("${resultMap[0]}, ${resultMap[9]}, ${resultMap[42]}, ${resultMap[99]}, ${resultMap[731]}")
    println("Height: ${resultMap.treeHeight()}")

    println(squaresHistory.cur()[12])
    println(squaresHistory.cur()[1000])
    println(squaresHistory.undo()[1000])
    println(squaresHistory.redo()[1000])

    squaresHistory.update(squaresHistory.cur().put(1, 42))
    println(squaresHistory.cur()[1])

    squaresHistory.update(squaresHistory.cur().put(1, 17))
    println(squaresHistory.cur()[1])

    println(squaresHistory.undo()[1])
    println(squaresHistory.undo()[1])

    squaresHistory.update(squaresHistory.cur().put(1, 22))
    println(squaresHistory.canRedo())
    println(squaresHistory.redo()[1])
}
