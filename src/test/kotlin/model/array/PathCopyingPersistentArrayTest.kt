package model.array

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.PersistentValue
import com.github.mihanizzm.model.array.PathCopyingPersistentArray
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("NonAsciiCharacters")
class PathCopyingPersistentArrayTest {
    @Test
    fun `Создание массива и базовые операции get и set`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(1, 2, 3))
        assertEquals(3, arr.size)
        assertEquals(2, arr[1])

        val arr2 = arr.set(1, 100)
        assertEquals(100, arr2[1])           // Новая версия
        assertEquals(2, arr[1])              // Старая осталась неизменной
    }

    @Test
    fun `Создание массива заданного размера`() {
        val arr = PathCopyingPersistentArray.ofSize<Int>(10)
        assertEquals(10, arr.size)
        for (i in arr) {
            assertNull(i)
        }
    }

    @Test
    fun `Операция get за пределами размера должна кидать IllegalArgumentException`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(1, 2, 3))
        assertThrows<IllegalArgumentException> { arr[10] }
    }

    @Test
    fun `Операция set с индексом за границами size кидает исключение IllegalArgumentException`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(1, 2, 3))
        assertThrows<IllegalArgumentException> { arr.set(5, 10) }
    }

    @Test
    fun `Последовательные set создают независимые версии`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(7, 8, 9))
        val arr2 = arr.set(0, 100)
        val arr3 = arr2.set(2, 200)

        assertEquals(listOf(100, 8, 9), arr2.toList())
        assertEquals(listOf(100, 8, 200), arr3.toList())
        assertEquals(listOf(7, 8, 9), arr.toList())
    }

    @Test
    fun `Операции undo и redo работают корректно`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(2, 4, 6))
        val hist = PersistentHistory(arr)
        assertFalse(hist.canUndo())

        hist.update(hist.cur().set(1, 20))   // [2, 20, 6]
        hist.update(hist.cur().set(2, 30))   // [2, 20, 30]
        hist.update(hist.cur().set(0, 99))   // [99, 20, 30]

        assertEquals(listOf(99, 20, 30), hist.cur().toList())
        hist.undo()
        assertEquals(listOf(2, 20, 30), hist.cur().toList())
        hist.undo()
        assertEquals(listOf(2, 20, 6), hist.cur().toList())
        hist.redo()
        assertEquals(listOf(2, 20, 30), hist.cur().toList())
        hist.update(hist.cur().set(1, 88))   // теперь redo невозможен
        assertFalse(hist.canRedo())
    }

    @Test
    fun `Массив поддерживает вложенность PersistentValue`() {
        val inner = PathCopyingPersistentArray.fromList<PersistentValue>(
            listOf(PersistentValue.PInt(1))
        )
        val outer = PathCopyingPersistentArray.fromList(listOf(
            PersistentValue.PString("test"),
            PersistentValue.PArray(inner)
        ))

        val inner2 = (outer[1] as PersistentValue.PArray).value.set(0, PersistentValue.PInt(5))
        val outer2 = outer.set(1, PersistentValue.PArray(inner2))

        assertEquals(PersistentValue.PInt(5), (outer2[1] as PersistentValue.PArray).value[0])
        assertEquals(PersistentValue.PInt(1), (outer[1] as PersistentValue.PArray).value[0])
    }

    @Test
    fun `Итератор возвращает только реальные элементы массива`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(10, 20, 30))
        val result = arr.toList()
        assertEquals(listOf(10, 20, 30), result)
        assertTrue(arr.iterator().asSequence().toList().containsAll(listOf(10, 20, 30)))
    }

    @Test
    fun `Массив из одного элемента корректно работает`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(123))
        assertEquals(1, arr.size)
        assertEquals(123, arr[0])
        assertThrows<IllegalArgumentException> { arr[1] }
    }

    @Test
    fun `Массив с null значениями поддерживается`() {
        val arr = PathCopyingPersistentArray.fromList(listOf(null, 1, null, 2))
        assertNull(arr[0])
        assertEquals(1, arr[1])
        assertNull(arr[2])
        assertEquals(2, arr[3])
    }

    @Test
    fun `Множественные set не затрагивают старые версии`() {
        val arr = PathCopyingPersistentArray.fromList((1..32).toList())
        val arr2 = arr.set(0, 999)
        val arr3 = arr2.set(31, 888)
        assertEquals(999, arr2[0])
        assertEquals(888, arr3[31])
        assertEquals(1, arr[0])
        assertEquals(32, arr[31])
    }
}