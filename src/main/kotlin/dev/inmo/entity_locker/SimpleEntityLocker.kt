package dev.inmo.entity_locker

/**
 * This [EntityLocker] will not take in focus that ids of entities may have the same ids by meaning, but different
 * by instance
 */
class SimpleEntityLocker<T, ID>(
    private val entityIdGetter: EntityIdGetter<T, ID>
) : EntityLocker<T> {
    override fun <R> lock(entity: T, block: (T) -> R): R {
        val id = entityIdGetter(entity) as Any // all instances of objects in kotlin are Any
        return synchronized(id) {
            block(entity)
        }
    }
}