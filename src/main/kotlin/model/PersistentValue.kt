package com.github.mihanizzm.model

import com.github.mihanizzm.model.array.PersistentArray
import com.github.mihanizzm.model.list.PersistentList
import com.github.mihanizzm.model.map.PersistentMap

/**
 * Тип‑сумма для представления «динамических» значений с сохранением типобезопасности.
 *
 * Вместо небезопасного использования «сырых» типов или `Any` значение упаковывается
 * в специализированный вариант запечатанного класса. Это позволяет:
 * - исчерпывающе разбирать случаи через `when` (компилятор проверяет полноту),
 * - сохранять строгую типизацию без приведений,
 * - безопасно хранить вложенные структуры (персистентный массив и мапу).
 *
 * Поддерживаемые варианты:
 * - [PInt] — целое,
 * - [PDouble] — число с плавающей точкой,
 * - [PString] — строка,
 * - [PBoolean] — булево,
 * - [PArray] — персистентный массив значений [PersistentValue] (ячейки могут быть `null`),
 * - [PMap] — персистентная мапа `String` → [PersistentValue].
 */
sealed class PersistentValue {
    /**
     * Целочисленное значение.
     */
    data class PInt(val value: Int): PersistentValue()
    /**
     * Вещественное значение двойной точности.
     */
    data class PDouble(val value: Double): PersistentValue()
    /**
     * Строковое значение.
     */
    data class PString(val value: String): PersistentValue()
    /**
     * Логическое значение.
     */
    data class PBoolean(val value: Boolean): PersistentValue()
    /**
     * Персистентный массив значений [PersistentValue].
     *
     * - Элементы массива имеют тип `PersistentValue?`, что позволяет явно хранить пустые ячейки.
     * - Семантика и сложность операций определяются реализацией [PersistentArray].
     */
    data class PArray(val value: PersistentArray<PersistentValue?>): PersistentValue()
    /**
     * Персистентная мапа строковых ключей на значения [PersistentValue].
     *
     * - Ключи — строки.
     * - Семантика и сложность операций определяются реализацией [PersistentMap].
     */
    data class PMap(val value: PersistentMap<String, PersistentValue>): PersistentValue()
     /**
     * Персистентный список значений [PersistentValue].
     *
     * - Элементы списка имеют тип `PersistentValue?`, что позволяет явно хранить пустые значения.
     * - Семантика и сложность операций определяются реализацией [PersistentList].
     */
    data class PList(val value: PersistentList<PersistentValue?>): PersistentValue()
}