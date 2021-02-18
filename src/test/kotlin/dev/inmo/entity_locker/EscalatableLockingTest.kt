package dev.inmo.entity_locker

import kotlin.test.Test
import kotlin.test.assertEquals

class EscalatableLockingTest {
    @Test
    fun simpleTestOfLockingEscalation() {
        val locker = EscalatableEntityLocker<Int, Int>(2) { it }

        val lockObject0 = 0
        val lockObject1 = 1
        val lockObject2 = 2

        val result = mutableListOf<Int>()

        locker.lock(lockObject0) {
            locker.lock(lockObject1) {
                val sync = Object()
                synchronized(sync) {
                    Thread {
                        locker.lockGlobal(lockObject2, 1000L) { global ->
                            result.add(global)
                        }
                        synchronized(sync) { sync.notifyAll() }
                    }.start()
                    sync.wait()
                }
                result.add(it)
            }
            result.add(it)
        }

        assertEquals(listOf(1, 0), result)
    }
}
