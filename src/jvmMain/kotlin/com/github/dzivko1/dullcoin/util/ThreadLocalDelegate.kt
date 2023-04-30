package com.github.dzivko1.dullcoin.util

import kotlin.reflect.KProperty

fun <T> threadLocal(initializer: () -> T) = ThreadLocalDelegate(initializer)

class ThreadLocalDelegate<T>(initializer: () -> T) {

    private val threadLocal = ThreadLocal.withInitial(initializer)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return threadLocal.get()
    }
}