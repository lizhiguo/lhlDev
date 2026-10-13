package com.lhl.common.core.helper

object UserContext {
    private val holder = ThreadLocal<String?>()

    fun set(username: String?) = holder.set(username)
    fun get(): String? = holder.get()
    fun clear() = holder.remove()
}