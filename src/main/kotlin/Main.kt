package com.github.mihanizzm

import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.map.PathCopyingPersistentMap

fun main() {
    val age = PathCopyingPersistentMap<String, Int>()

    val age2 = age.put("Misha", 22)
    val age3 = age2.put("Lesya", 22)
    val age4 = age3.put("Nikita", 25)
    val age5 = age4.put("Vanya", 24)

    println(age5.keys())
    println(age5["Misha"])
}