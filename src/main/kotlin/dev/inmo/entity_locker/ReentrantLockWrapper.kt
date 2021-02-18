package dev.inmo.entity_locker

import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Wrap operations like checking of locking on thread, autounlock and others
 *
 * @param block By default do nothing
 */
fun ReentrantLock.wrapLocking(timeoutMillis: Long?, block: () -> Unit = {}) {
    var requireUnlock = true
    synchronized(this) {
        when {
            isHeldByCurrentThread -> requireUnlock = false
            timeoutMillis != null -> if (!tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                return
            }
            else -> lock()
        }
    }

    try {
        block()
    } finally {
        if (requireUnlock) {
            unlock()
        }
    }
}
