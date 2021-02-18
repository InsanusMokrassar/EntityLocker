package dev.inmo.entity_locker

interface EntityLocker<T> {
    fun <R> lock(entity: T, block: (T) -> R): R
}
