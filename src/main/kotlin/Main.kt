package com.github.mihanizzm

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.PersistentValue
import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.list.PathCopyingPersistentList
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

    println("-------------------------------------------------------------")

    // Создаём профиль
    val profile = PathCopyingPersistentMap<String, PersistentValue>()
        .put("user", PersistentValue.PString("Alice"))
        .put("age", PersistentValue.PInt(30))
        .put("tags", PersistentValue.PArray(PathCopyingPersistentArray.fromList(listOf<PersistentValue>(
            PersistentValue.PString("admin"), PersistentValue.PString("editor")
        ))))

    // Подключаем историю изменений для профиля
    val history = PersistentHistory(profile)

    // Меняем теги
    val oldTags = (history.cur()["tags"] as PersistentValue.PArray).value
    val newTags = oldTags.set(1, PersistentValue.PString("moderator"))
    val newProfile = history.cur().put("tags", PersistentValue.PArray(newTags))
    history.update(newProfile)
    println((history.cur()["tags"] as PersistentValue.PArray).value.toList())
    // [PString(value=admin), PString(value=moderator)]

    // Меняем возраст
    history.update(history.cur().put("age", PersistentValue.PInt(31)))
    println(history.cur()["age"]) // PInt(value=31)

    // Откатываем последнее изменение (возраст)




    println("-------------------------------------------------------------")

    // Простой сценарий: Список дел с историей изменений
    println("=== Список дел с историей изменений ===")

    // Создаем пустой список дел
    val emptyTodoList = PathCopyingPersistentList<String>()
    val todoHistory = PersistentHistory(emptyTodoList)

    println("1. Начинаем с пустого списка дел")

    // Добавляем задачи
    todoHistory.update(todoHistory.cur().addLast("Купить молоко"))
    println("2. Добавили: Купить молоко")

    todoHistory.update(todoHistory.cur().addLast("Позвонить маме"))
    println("3. Добавили: Позвонить маме")

    todoHistory.update(todoHistory.cur().addLast("Сделать домашку"))
    println("4. Добавили: Сделать домашку")

    todoHistory.update(todoHistory.cur().addLast("Почистить зубы"))
    println("5. Добавили: Почистить зубы")

    todoHistory.update(todoHistory.cur().addLast("Лечь спать до 23:00"))
    println("6. Добавили: Лечь спать до 23:00")

    // Показываем текущий список
    println("\nТекущий список дел (${todoHistory.cur().size} задач):")
    todoHistory.cur().forEachIndexed { index, task ->
        println("  ${index + 1}. $task")
    }

    // Выполняем задачу (удаляем)
    println("\n7. Выполняем задачу 'Почистить зубы'...")
    val currentList = todoHistory.cur()
    val taskIndex = currentList.indexOf("Почистить зубы")
    if (taskIndex != -1) {
        todoHistory.update(currentList.removeAt(taskIndex))
        println("   Задача выполнена и удалена из списка!")
    }

    // Показываем текущий список
    println("\nТекущий список дел (${todoHistory.cur().size} задач):")
    todoHistory.cur().forEachIndexed { index, task ->
        println("  ${index + 1}. $task")
    }

    // Меняем приоритет (перемещаем задачу)
    println("\n8. Повышаем приоритет 'Лечь спать до 23:00'...")
    val listWithMoved = todoHistory.cur()
    val sleepIndex = listWithMoved.indexOf("Лечь спать до 23:00")
    if (sleepIndex != -1) {
        // Удаляем и вставляем в начало
        val task = listWithMoved.get(sleepIndex)
        val withoutTask = listWithMoved.removeAt(sleepIndex)
        todoHistory.update(withoutTask.addFirst(task))
        println("   Задача перемещена в начало списка!")
    }

    // Показываем текущий список
    println("\nТекущий список дел (${todoHistory.cur().size} задач):")
    todoHistory.cur().forEachIndexed { index, task ->
        println("  ${index + 1}. $task")
    }

    // Отменяем последнее изменение
    println("\n9. Отменяем последнее изменение...")
    todoHistory.undo()
    println("   Вернули список до перемещения задачи")

    // Показываем текущий список
    println("\nТекущий список дел (${todoHistory.cur().size} задач):")
    todoHistory.cur().forEachIndexed { index, task ->
        println("  ${index + 1}. $task")
    }

    // Редактируем задачу
    println("\n10. Редактируем задачу 'Сделать домашку'...")
    val listToEdit = todoHistory.cur()
    val homeworkIndex = listToEdit.indexOf("Сделать домашку")
    if (homeworkIndex != -1) {
        todoHistory.update(listToEdit.set(homeworkIndex, "Сделать домашку по математике"))
        println("   Задача обновлена!")
    }

    // Показываем финальный список
    println("\nФинальный список дел (${todoHistory.cur().size} задач):")
    todoHistory.cur().forEachIndexed { index, task ->
        println("  ${index + 1}. $task")
    }

    // Демонстрация истории
    println("\n=== История изменений ===")
    println("Можно отменить изменений: ${todoHistory.canUndo()}")
    println("Можно вернуть отмененное: ${todoHistory.canRedo()}")

    // Сохраняем список в файл (имитация)
    println("\n11. Сохраняем список в 'персистентный массив'...")
    val savedArray = todoHistory.cur().toPersistentArray()
    println("   Список сохранен (${savedArray.size} задач)")

    // Восстанавливаем из 'файла'
    println("\n12. Восстанавливаем список из 'файла'...")
    val restoredList = PathCopyingPersistentList.fromPersistentArray(savedArray)
    println("   Список восстановлен (${restoredList.size} задач):")
    restoredList.forEachIndexed { index, task ->
        println("    ${index + 1}. $task")
    }

    // Создаем копию для планирования на завтра
    println("\n13. Создаем копию для завтрашнего дня...")
    val tomorrowList = restoredList
        .addLast("Сходить в магазин")
        .addLast("Подготовиться к встрече")
        .removeAt(0) // Удаляем "Купить молоко" (уже купили)

    println("   Список на завтра (${tomorrowList.size} задач):")
    tomorrowList.forEachIndexed { index, task ->
        println("    ${index + 1}. $task")
    }

    println("\n=== Список дел: демонстрация завершена ===")
    println("Все операции выполнены без изменения оригинальных списков!")
}
