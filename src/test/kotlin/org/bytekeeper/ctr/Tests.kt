package org.bytekeeper.ctr

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

fun <T> any(): T {
    Mockito.any<T>()
    return castedNull()
}

fun <T> eq(other: T): T {
    Mockito.eq(other)
    return castedNull()
}


fun <E, T> ArgumentCaptor<T>.cap(): E {
    this.capture()
    return castedNull()
}

private fun <T> castedNull() = null as T

inline fun <reified T> mock() = Mockito.mock(T::class.java)