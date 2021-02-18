package dev.inmo.entity_locker

import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleLockingTest {
    @Test
    fun twoThreadsCheck() {
        val lockObject = 0
        val locker = SimpleEntityLocker<Int, Int> { it }

        val thread0StartedSync = Object()
        val thread1StartedSync = Object()

        val threadsCompletion = mutableListOf<Int>()

        val thread0 = Thread {
            locker.lock(lockObject) {
                synchronized(thread1StartedSync) {
                    synchronized(thread0StartedSync) { thread0StartedSync.notifyAll() }
                    thread1StartedSync.wait()
                }
                threadsCompletion.add(0)
            }
        }
        synchronized(thread0StartedSync) {
            thread0.start()
            thread0StartedSync.wait()
        }

        Thread {
            synchronized(thread1StartedSync) { thread1StartedSync.notifyAll() }
            locker.lock(lockObject) {
                threadsCompletion.add(1)
            }
        }.start()

        assertEquals(listOf(0, 1), threadsCompletion)
    }
}
