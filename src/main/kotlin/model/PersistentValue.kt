package com.github.mihanizzm.model

import com.github.mihanizzm.model.array.PersistentArray
import com.github.mihanizzm.model.map.PersistentMap

sealed class PersistentValue {
    data class PInt(val value: Int): PersistentValue()
    data class PDouble(val value: Double): PersistentValue()
    data class PString(val value: String): PersistentValue()
    data class PBoolean(val value: Boolean): PersistentValue()
    data class PArray(val value: PersistentArray<PersistentValue?>): PersistentValue()
    data class PMap(val value: PersistentMap<String, PersistentValue>): PersistentValue()
}