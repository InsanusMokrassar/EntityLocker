package dev.inmo.entity_locker

class EscalatableEntityLocker<T, ID>(
    entityIdGetter: EntityIdGetter<T, ID>,
    private val lockToGlobalLockEscalationCount: Int
) : EntityLocker<T>, SimpleEntityLocker<T, ID>(entityIdGetter) {
    override fun lock(entity: T, timeoutMillis: Long?, block: (T) -> Unit) {
        var requireToEscalateUpToGlobal = false
        var lockedOnThisThread = 0
        for (lock in reentrantLocks.values) {
            if (lock.isHeldByCurrentThread) {
                lockedOnThisThread++
                if (lockedOnThisThread >= lockToGlobalLockEscalationCount) {
                    requireToEscalateUpToGlobal = true
                    break
                }
            }
        }
        if (requireToEscalateUpToGlobal) {
            lockGlobal(entity, block)
        } else {
            super<SimpleEntityLocker>.lock(entity, timeoutMillis, block)
        }
    }
}