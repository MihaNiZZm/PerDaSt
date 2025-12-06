package com.github.mihanizzm

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.PersistentValue
import com.github.mihanizzm.model.array.PathCopyingPersistentArray
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
    history.undo()
    println(history.cur()["age"]) // PInt(value=30)

    // Откатываем ещё (теги)
    history.undo()
    println((history.cur()["tags"] as PersistentValue.PArray).value.toList())
    // [PString(value=admin), PString(value=editor)]
}
