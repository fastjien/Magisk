package com.fastjien.sunny.core.utils

import java.util.AbstractList

object DummyList : AbstractList<String>() {

    override val size: Int get() = 0

    override fun get(index: Int): String {
        throw IndexOutOfBoundsException()
    }

    override fun add(element: String): Boolean = false

    override fun add(index: Int, element: String) {}

    override fun clear() {}
}
