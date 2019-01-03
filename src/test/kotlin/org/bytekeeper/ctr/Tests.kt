package org.bytekeeper.ctr

import org.mockito.Mockito

fun <T> any(): T {
    Mockito.any<T>()
    return null as T
}

fun <T> eq(other: T): T {
    Mockito.eq(other)
    return null as T
}

inline fun <reified T> mock() = Mockito.mock(T::class.java)