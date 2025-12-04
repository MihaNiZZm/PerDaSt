package com.github.mihanizzm

import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.map.PathCopyingPersistentMap
import com.github.mihanizzm.model.list.PathCopyingPersistentList

fun main() {
    // 1. Двусвязный persistent список
    val list = PathCopyingPersistentList<String>()
    
    // 2. Undo-redo
    val manager = UndoRedoManager(list)
    manager.execute { it.add("First") }
    manager.execute { it.add("Second") }
    println(manager.getCurrent()) // [First, Second]
    
    manager.undo()
    println(manager.getCurrent()) // [First]
    
    // 3. STM транзакции
    val result = list.transaction { tx ->
        tx.add("A")
        tx.add("B")
        tx.commit()
    }
    println(result) // [A, B]
    
    // 4. Преобразование
    val array = list.asPersistentArray()
    val map = list.asPersistentMap()
    
    // 5. Вложенные структуры
    val nestedList = PathCopyingPersistentList<PersistentList<Int>>()
    val innerList = PathCopyingPersistentList<Int>().add(1).add(2)
    val updatedNested = nestedList.add(innerList)
}