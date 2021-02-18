package dev.inmo.entity_locker

import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * This [EntityLocker] will not take in focus that ids of entities may have the same ids by meaning, but different
 * by instance
 */
open class SimpleEntityLocker<T, ID>(
    private val entityIdGetter: EntityIdGetter<T, ID>
) : EntityLocker<T> {
    protected val reentrantLocks = WeakHashMap<ID, ReentrantLock>()
    protected val globalLockObject = ReentrantLock()

    override fun lock(entity: T, timeoutMillis: Long?, block: (T) -> Unit) {
        if (globalLockObject.isLocked) {
            when {
                globalLockObject.isHeldByCurrentThread -> lockGlobal(entity, block)
                else -> {
                    var canContinue = false
                    globalLockObject.wrapLocking(timeoutMillis) {
                        canContinue = true
                    }
                    if (!canContinue) return
                }
            }
        }
        val id = entityIdGetter(entity) // all instances of objects in kotlin are Any

        val lock = synchronized(reentrantLocks) {
            reentrantLocks.getOrPut(id) { ReentrantLock() }
        }
        lock.wrapLocking(timeoutMillis) {
            block(entity)
        }
    }

    override fun lockGlobal(entity: T, timeoutMillis: Long?, block: (T) -> Unit) {
        globalLockObject.wrapLocking(timeoutMillis) {
            block(entity)
        }
    }
}