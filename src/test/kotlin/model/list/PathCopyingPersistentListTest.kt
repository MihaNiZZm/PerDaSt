package model.list

import com.github.mihanizzm.model.PersistentHistory
import com.github.mihanizzm.model.list.PathCopyingPersistentList
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

@Suppress("NonAsciiCharacters")
class PathCopyingPersistentListTest {

    @Test
    fun `Пустой список имеет корректные свойства`() {
        val list = PathCopyingPersistentList<Int>()
        
        assertEquals(0, list.size)
        assertNull(list.first())
        assertNull(list.last())
        assertFalse(list.iterator().hasNext())
    }

    @Test
    fun `Операция add добавляет элемент в конец`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
        
        assertEquals(3, list.size)
        assertEquals(1, list.first())
        assertEquals(3, list.last())
        assertEquals(2, list.get(1))
    }

    @Test
    fun `Операция addFirst добавляет элемент в начало`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(3)
            .addFirst(2)
            .addFirst(1)
        
        assertEquals(3, list.size)
        assertEquals(1, list.first())
        assertEquals(3, list.last())
        assertEquals(2, list.get(1))
    }

    @Test
    fun `Разные версии списка не влияют друг на друга`() {
        val l1 = PathCopyingPersistentList<Int>()
        val l2 = l1.addLast(1)
        val l3 = l2.addLast(2)
        val l4 = l3.set(1, 99)
        
        assertEquals(0, l1.size)
        assertEquals(1, l2.size)
        assertEquals(2, l3.size)
        assertEquals(2, l4.size)
        assertEquals(2, l3.get(1))
        assertEquals(99, l4.get(1))
    }

    @Test
    fun `Операция removeFirst удаляет только первый элемент`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
        val list2 = list.removeFirst()
        
        assertEquals(3, list.size)
        assertEquals(1, list.first())
        assertEquals(2, list2.size)
        assertEquals(2, list2.first())
        assertEquals(3, list2.last())
    }

    @Test
    fun `Операция removeLast удаляет только последний элемент`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
        val list2 = list.removeLast()
        
        assertEquals(3, list.size)
        assertEquals(3, list.last())
        assertEquals(2, list2.size)
        assertEquals(1, list2.first())
        assertEquals(2, list2.last())
    }

    @Test
    fun `Операция insert вставляет элемент по указанному индексу`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(3)
            .insert(1, 2)
        
        assertEquals(3, list.size)
        assertEquals(1, list.get(0))
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
    }

    @Test
    fun `Операция insert в начало работает как addFirst`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(2)
            .addLast(3)
            .insert(0, 1)
        
        assertEquals(3, list.size)
        assertEquals(1, list.first())
        assertEquals(2, list.get(1))
    }

    @Test
    fun `Операция insert в конец работает как add`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .insert(2, 3)
        
        assertEquals(3, list.size)
        assertEquals(1, list.first())
        assertEquals(3, list.last())
    }

    @Test
    fun `Операция removeAt удаляет элемент по указанному индексу`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
        val list2 = list.removeAt(1)
        
        assertEquals(3, list.size)
        assertEquals(2, list2.size)
        assertEquals(1, list2.get(0))
        assertEquals(3, list2.get(1))
    }

    @Test
    fun `Операция removeAt в начале работает как removeFirst`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
        val list2 = list.removeAt(0)
        
        assertEquals(2, list.size)
        assertEquals(1, list2.size)
        assertEquals(2, list2.first())
    }

    @Test
    fun `Операция removeAt в конце работает как removeLast`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
        val list2 = list.removeAt(1)
        
        assertEquals(2, list.size)
        assertEquals(1, list2.size)
        assertEquals(1, list2.first())
    }

    @Test
    fun `Операция set заменяет элемент по индексу`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
        val history = PersistentHistory(list)
        
        val original = history.cur()
        
        history.update(history.cur().set(1, 98))
        val updated = history.cur()
        
        assertEquals(3, original.size)
        assertEquals(2, original.get(1))
        
        assertEquals(3, updated.size)
        assertEquals(98, updated.get(1))
        
        assertEquals(1, updated.get(0))
        assertEquals(3, updated.get(2))
    }

    @Test
    fun `Операции undo и redo восстанавливают состояния списка`() {
        val list = PathCopyingPersistentList<Int>()
        val hist = PersistentHistory(list)
        
        hist.update(hist.cur().addLast(1))
        hist.update(hist.cur().addLast(2))
        hist.update(hist.cur().set(1, 99))
        hist.update(hist.cur().removeLast())
        
        assertEquals(1, hist.cur().size)
        hist.undo()
        assertEquals(2, hist.cur().size)
        assertEquals(99, hist.cur().get(1))
        hist.undo()
        assertEquals(2, hist.cur().get(1))
        hist.undo()
        assertEquals(1, hist.cur().size)
        hist.redo()
        assertEquals(2, hist.cur().size)
    }

        @Test
    fun `Итератор правильно обходит не-null элементы`() {
        val list = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
            .addLast(4)
            .addLast(5)
        
        val result = mutableListOf<Int>()
        for (item in list) {
            result.addLast(item)
        }
        
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun `Работа с null значениями корректна`() {
        val list = PathCopyingPersistentList<Int?>()
            .addLast(1)
            .addLast(null)
            .addLast(3)
            .set(1, 2)
            .insert(3, null)
        
        assertEquals(4, list.size)
        assertEquals(1, list.get(0))
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
        assertNull(list.get(3))
    }

    @Test
    fun `Удаление из пустого списка вызывает исключение`() {
        val list = PathCopyingPersistentList<Int>()
        
        assertThrows(NoSuchElementException::class.java) {
            list.removeFirst()
        }
        assertThrows(NoSuchElementException::class.java) {
            list.removeLast()
        }
    }

    @Test
    fun `Некорректные индексы вызывают исключения`() {
        val list = PathCopyingPersistentList<Int>().addLast(1).addLast(2)
        
        assertThrows(IllegalArgumentException::class.java) {
            list.get(5)
        }
        assertThrows(IllegalArgumentException::class.java) {
            list.set(5, 3)
        }
        assertThrows(IllegalArgumentException::class.java) {
            list.insert(5, 3)
        }
        assertThrows(IllegalArgumentException::class.java) {
            list.removeAt(5)
        }
    }

    @Test
    fun `Операция get с оптимизацией поиска с конца`() {
        val list = PathCopyingPersistentList<Int>()
        val history = PersistentHistory(list)

        for (i in 1..100) {
            history.update(history.cur().addLast(i))
        }
        
        assertEquals(1, history.cur().get(0))
        assertEquals(50, history.cur().get(49))
        assertEquals(100, history.cur().get(99))
    }

    @Test
    fun `Проверка first и last на списках разных размеров`() {
        val empty = PathCopyingPersistentList<Int>()
        val single = empty.addLast(42)
        val multiple = single.addLast(43).addLast(44)
        
        assertNull(empty.first())
        assertNull(empty.last())
        assertEquals(42, single.first())
        assertEquals(42, single.last())
        assertEquals(42, multiple.first())
        assertEquals(44, multiple.last())
    }

    @Test
    fun `Удаление единственного элемента делает список пустым`() {
        val single = PathCopyingPersistentList<Int>().addLast(42)
        val afterRemoveFirst = single.removeFirst()
        val afterRemoveLast = single.removeLast()
        val afterRemoveAt = single.removeAt(0)
        
        assertEquals(0, afterRemoveFirst.size)
        assertEquals(0, afterRemoveLast.size)
        assertEquals(0, afterRemoveAt.size)
        assertNull(afterRemoveFirst.first())
        assertNull(afterRemoveFirst.last())
    }

    @Test
    fun `Преобразование списка в массив и обратно работает корректно`() {
        val originalList = PathCopyingPersistentList<Int>()
            .addLast(1)
            .addLast(2)
            .addLast(3)
            .addLast(null)
            .addLast(5)
        
        val array = originalList.toPersistentArray()
        
        assertEquals(5, array.size)
        assertEquals(1, array.get(0))
        assertEquals(2, array.get(1))
        assertEquals(3, array.get(2))
        assertNull(array.get(3))
        assertEquals(5, array.get(4))
        
        val listFromArray = PathCopyingPersistentList.fromPersistentArray(array)
        
        assertEquals(5, listFromArray.size)
        assertEquals(1, listFromArray.get(0))
        assertEquals(2, listFromArray.get(1))
        assertEquals(3, listFromArray.get(2))
        assertNull(listFromArray.get(3))
        assertEquals(5, listFromArray.get(4))
        
        val modifiedArray = array.set(1, 99)
        assertEquals(99, modifiedArray.get(1))
        assertEquals(2, originalList.get(1)) 
        assertEquals(2, listFromArray.get(1)) 
    }

    @Test
    fun `Создание списка из обычного списка работает корректно`() {
        val elements = listOf(1, null, 3, 4, 5)
        val list = PathCopyingPersistentList.fromList(elements)
        
        assertEquals(5, list.size)
        assertEquals(1, list.get(0))
        assertNull(list.get(1))
        assertEquals(3, list.get(2))
        assertEquals(4, list.get(3))
        assertEquals(5, list.get(4))
    }

}