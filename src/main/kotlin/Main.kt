package com.github.mihanizzm

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.PersistentValue
import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.list.PathCopyingPersistentList
import com.github.mihanizzm.model.map.PathCopyingPersistentMap

fun main() {

    println("=== Comprehensive test of all methods ===")

    // 1. Создаем пустой список и историю
    val list = PathCopyingPersistentList<Int>()
    val history = PersistentHistory(list)

    println("1. Empty list: ${history.cur().toList()}, size: ${history.cur().size}")

    // 2. Добавляем элементы в конец
    history.update(history.cur().add(10))
    history.update(history.cur().add(20))
    history.update(history.cur().add(30))
    println("2. After add(10), add(20), add(30): ${history.cur().toList()}, size: ${history.cur().size}")

    // 3. Добавляем в начало
    history.update(history.cur().addFirst(5))
    println("3. After addFirst(5): ${history.cur().toList()}, size: ${history.cur().size}")

    // 4. Вставляем в середину
    history.update(history.cur().insert(2, 15))
    println("4. After insert(2, 15): ${history.cur().toList()}, size: ${history.cur().size}")

    // 5. Изменяем элемент
    history.update(history.cur().set(3, 25))
    println("5. After set(3, 25): ${history.cur().toList()}, size: ${history.cur().size}")

    // 6. Удаляем первый
    history.update(history.cur().removeFirst())
    println("6. After removeFirst(): ${history.cur().toList()}, size: ${history.cur().size}")

    // 7. Удаляем последний
    history.update(history.cur().removeLast())
    println("7. After removeLast(): ${history.cur().toList()}, size: ${history.cur().size}")

    // 8. Удаляем по индексу (середина)
    history.update(history.cur().removeAt(1))
    println("8. After removeAt(1): ${history.cur().toList()}, size: ${history.cur().size}")

    // Теперь проверяем undo/redo для всех операций
    println("\n=== Testing Undo/Redo ===")

    // Undo все шаги
    println("Undo step 8 (removeAt):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Undo step 7 (removeLast):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Undo step 6 (removeFirst):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Undo step 5 (set):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Undo step 4 (insert):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Undo step 3 (addFirst):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Undo step 2 (add x3):")
    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    history.undo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("\nNow we should be back to empty list")
    println("Empty: ${history.cur().toList()}, size: ${history.cur().size}")

    // Redo все шаги
    println("\n=== Testing Redo ===")
    println("Redo add(10):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo add(20):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo add(30):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo addFirst(5):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo insert(2, 15):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo set(3, 25):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo removeFirst():")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo removeLast():")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    println("Redo removeAt(1):")
    history.redo()
    println("  List: ${history.cur().toList()}, size: ${history.cur().size}")

    // Проверка edge cases
    println("\n=== Testing edge cases ===")

    // removeAt на пустом списке
    try {
        PathCopyingPersistentList<Int>().removeAt(0)
        println("ERROR: Should have thrown exception!")
    } catch (e: IllegalArgumentException) {
        println("Correctly caught removeAt on empty list: ${e.message}")
    }

    // removeAt с некорректным индексом
    val smallList = PathCopyingPersistentList<Int>().add(1).add(2)
    try {
        smallList.removeAt(5)
        println("ERROR: Should have thrown exception!")
    } catch (e: IllegalArgumentException) {
        println("Correctly caught removeAt with invalid index: ${e.message}")
    }

    // insert с некорректным индексом
    try {
        smallList.insert(5, 3)
        println("ERROR: Should have thrown exception!")
    } catch (e: IllegalArgumentException) {
        println("Correctly caught insert with invalid index: ${e.message}")
    }

    // set с некорректным индексом
    try {
        smallList.set(5, 3)
        println("ERROR: Should have thrown exception!")
    } catch (e: IllegalArgumentException) {
        println("Correctly caught set with invalid index: ${e.message}")
    }

    // Проверка работы с null значениями
    println("\n=== Testing with null values ===")
    val nullList = PathCopyingPersistentList<Int?>()
        .add(1)
        .add(null)
        .add(3)
        .insert(1, null)
        .set(2, null)

    println("List with nulls: ${nullList.toList()}, size: ${nullList.size}")
    println("get(1): ${nullList.get(1)} (should be null)")
    println("get(2): ${nullList.get(2)} (should be null)")
}
