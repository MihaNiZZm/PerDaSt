package model.map

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.PersistentValue
import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import com.github.mihanizzm.model.map.PathCopyingPersistentMap
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

@Suppress("NonAsciiCharacters")
class PathCopyingPersistentMapTest {
    @Test
    fun `Операции put и get работают как ожидается`() {
        val map = PathCopyingPersistentMap<String, Int>()
            .put("a", 1)
            .put("b", 2)

        assertEquals(1, map["a"])
        assertEquals(2, map["b"])
        assertNull(map["c"])
        assertEquals(2, map.size)
    }

    @Test
    fun `Разные версии не влияют друг на друга`() {
        val m1 = PathCopyingPersistentMap<String, String>()
        val m2 = m1.put("k", "v")
        val m3 = m2.put("k", "v2")

        assertNull(m1["k"])
        assertEquals("v", m2["k"])
        assertEquals("v2", m3["k"])
    }

    @Test
    fun `Операция remove удаляет ключ только из текущей версии`() {
        val m1 = PathCopyingPersistentMap<String, Int>().put("x", 5)
        val m2 = m1.remove("x")
        assertEquals(5, m1["x"])
        assertNull(m2["x"])
        assertEquals(1, m1.size)
        assertEquals(0, m2.size)
    }

    @Test
    fun `Операция remove не падает и не изменяет размер, если ключа не было`() {
        val map = PathCopyingPersistentMap<String, Int>()
        val map2 = map.remove("nope")
        assertSame(map, map2)
    }

    @Test
    fun `Операции undo и redo восстанавливают значения`() {
        val map = PathCopyingPersistentMap<String, String>()
        val hist = PersistentHistory(map)
        hist.update(hist.cur().put("x", "a"))
        hist.update(hist.cur().put("y", "b"))
        hist.update(hist.cur().remove("x"))

        assertNull(hist.cur()["x"])
        hist.undo()
        assertEquals("a", hist.cur()["x"])
        hist.undo()
        assertNull(hist.cur()["y"])
        hist.redo()
        assertEquals("b", hist.cur()["y"])
    }

    @Test
    fun `Значения могут быть вложенными PersistentValue-maps и arrays`() {
        val array = PathCopyingPersistentArray.fromList(listOf(
            PersistentValue.PInt(1),
            PersistentValue.PString("hi"),
        ))
        val pm = PathCopyingPersistentMap<String, PersistentValue>()
            .put("arr", PersistentValue.PArray(array))
            .put("other", PersistentValue.PInt(42))

        assertTrue(pm["arr"] is PersistentValue.PArray)
        assertEquals(PersistentValue.PInt(42), pm["other"])
    }

    @Test
    fun `Функция keys возвращает только реальные ключи`() {
        val map = PathCopyingPersistentMap<String, Int>()
            .put("a", 1)
            .put("b", 2)
        assertTrue(map.keys().containsAll(listOf("a","b")))
        assertEquals(2, map.keys().size)
    }

    @Test
    fun `Операция put с уже существующим ключом обновляет только значение`() {
        val map = PathCopyingPersistentMap<Int, String>().put(42, "a")
        val map2 = map.put(42, "b")
        assertEquals("a", map[42])
        assertEquals("b", map2[42])
        assertEquals(1, map2.size)
    }

    @Test
    fun `Операция get для ненайденного ключа возвращает null`() {
        val map = PathCopyingPersistentMap<String, String>()
        assertNull(map["нет такого"])
    }

    @Test
    fun `Удаление первого элемента, добавленного в мапу и проверка, остались ли остальные`() {
        val map = PathCopyingPersistentMap<String, Int>().put("first", 1).put("second", 2)
        val map2 = map.remove("first")
        assertNull(map2["first"])
        assertEquals(1, map2.size)
    }

    @Test
    fun `Операция remove над ключом, который был удалён раньше, ничего не меняет`() {
        val map = PathCopyingPersistentMap<String, Int>().put("x", 1).remove("x").remove("x")
        assertEquals(0, map.size)
    }
}