package dev.inmo.entity_locker

fun interface EntityIdGetter<T, ID> {
    fun getId(entity: T): ID
    operator fun invoke(entity: T): ID = getId(entity)
}
