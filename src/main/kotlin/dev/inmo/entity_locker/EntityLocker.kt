package dev.inmo.entity_locker

interface EntityLocker<T> {
    fun lock(entity: T, timeoutMillis: Long?, block: (T) -> Unit)
    fun lock(entity: T, block: (T) -> Unit) = lock(entity, null, block)
    fun lockGlobal(entity: T, timeoutMillis: Long?, block: (T) -> Unit)
    fun lockGlobal(entity: T, block: (T) -> Unit) = lockGlobal(entity, null, block)
}
