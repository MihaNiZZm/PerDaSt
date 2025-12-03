package com.github.mihanizzm

import com.github.mihanizzm.model.array.PathCopyingPersistentArray

fun main() {
    val pArr = PathCopyingPersistentArray.fromList(listOf(1, 2, 3, 4))
    println(pArr.size)
    println(pArr[2])
    val pArr2 = pArr.set(2, 7)
    var s1 = ""
    var s2 = ""
    for (i in 0 until pArr.size) {
        s1 += " ${pArr[i]}"
        s2 += " ${pArr2[i]}"
    }
    println(s1)
    println(s2)
}