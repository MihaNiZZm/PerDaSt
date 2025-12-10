package com.github.mihanizzm.model.list

import com.github.mihanizzm.model.PersistentCollection

/**
 * Персистентный двусвязный список.
 *
 * Все операции модификации возвращают новый список, не изменяя исходный.
 * Реализация должна поддерживать эффективный доступ к элементам по индексу
 * и двунаправленную навигацию.
 *
 * Элементы списка могут быть null, что соответствует пустой ячейке.
 */
interface PersistentList<T> : PersistentCollection<T> {
    
    /**
     * Добавляет элемент [element] в конец списка.
     * Возвращает новый список с добавленным элементом.
     */
    fun addLast(element: T?): PersistentList<T>
    
    /**
     * Добавляет элемент [element] в начало списка.
     * Возвращает новый список с добавленным элементом.
     */
    fun addFirst(element: T?): PersistentList<T>
    
    /**
     * Вставляет элемент [element] по указанному [индексу].
     * 
     * Предусловие: `index in 0..size`, где:
     * - `index == 0` - вставка в начало
     * - `index == size` - вставка в конец (эквивалентно [add])
     * 
     * @throws IndexOutOfBoundsException если индекс выходит за допустимые пределы
     */
    fun insert(index: Int, element: T?): PersistentList<T>
    
    /**
     * Удаляет элемент по указанному [индексу].
     * Возвращает новый список без удаленного элемента.
     * 
     * Предусловие: `index in 0 until size`
     * @throws IndexOutOfBoundsException если индекс выходит за допустимые пределы
     */
    fun removeAt(index: Int): PersistentList<T>
    
    /**
     * Удаляет первый элемент списка.
     * Возвращает новый список без первого элемента.
     * 
     * Предусловие: список не должен быть пустым
     * @throws NoSuchElementException если список пуст
     */
    fun removeFirst(): PersistentList<T>
    
    /**
     * Удаляет последний элемент списка.
     * Возвращает новый список без последнего элемента.
     * 
     * Предусловие: список не должен быть пустым
     * @throws NoSuchElementException если список пуст
     */
    fun removeLast(): PersistentList<T>
    
    /**
     * Заменяет элемент по указанному [индексу] на [element].
     * Возвращает новый список с замененным элементом.
     * 
     * Предусловие: `index in 0 until size`
     * @throws IndexOutOfBoundsException если индекс выходит за допустимые пределы
     */
    fun set(index: Int, element: T?): PersistentList<T>
    
    /**
     * Возвращает первый элемент списка или null, если список пуст.
     */
    fun first(): T?
    
    /**
     * Возвращает последний элемент списка или null, если список пуст.
     */
    fun last(): T?
}