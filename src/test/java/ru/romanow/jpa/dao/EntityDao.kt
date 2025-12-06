package ru.romanow.jpa.dao

import java.util.*

interface EntityDao {
    fun <T> findById(id: Int, cls: Class<T>): Optional<T>
    fun <T> findAll(cls: Class<T>): List<T>
}
