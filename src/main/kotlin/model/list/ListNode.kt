package com.github.mihanizzm.model.list

internal data class ListNode<T>(
    val value: T,
    val prev: ListNode<T>? = null,
    var next: ListNode<T>? = null,
    val id: Long = System.nanoTime()
) {
    fun copyWithPrev(newPrev: ListNode<T>?): ListNode<T> =
        ListNode(value, newPrev, next, id)
    
    fun copyWithNext(newNext: ListNode<T>?): ListNode<T> =
        ListNode(value, prev, newNext, id)
}