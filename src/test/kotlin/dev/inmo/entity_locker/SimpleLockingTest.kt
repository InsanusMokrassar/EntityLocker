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
        val thread1CompletedSync = Object()

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
            synchronized(thread1CompletedSync) { thread1CompletedSync.notifyAll() }
        }.let {
            synchronized(thread1CompletedSync) {
                it.start()
                thread1CompletedSync.wait()
            }
        }

        assertEquals(listOf(0, 1), threadsCompletion)
    }

    @Test
    fun twoThreadsCheckOneWillBeLocked() {
        val lockObject = 0
        val locker = SimpleEntityLocker<Int, Int> { it }

        val thread0StartedSync = Object()
        val thread1StartedSync = Object()
        val thread1WaitCompletedSync = Object()
        val thread1CompletedSync = Object()

        val threadsCompletion = mutableListOf<Int>()

        val thread0 = Thread {
            locker.lock(lockObject) {
                synchronized(thread1WaitCompletedSync) {
                    synchronized(thread1StartedSync) {
                        synchronized(thread0StartedSync) { thread0StartedSync.notifyAll() }
                        thread1StartedSync.wait()
                    }
                    thread1WaitCompletedSync.wait()
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
            locker.lock(lockObject, 1000L) {
                threadsCompletion.add(1)
            }
            synchronized(thread1WaitCompletedSync) { thread1WaitCompletedSync.notifyAll() }
            synchronized(thread1CompletedSync) { thread1CompletedSync.notifyAll() }
        }.let {
            synchronized(thread1CompletedSync) {
                it.start()
                thread1CompletedSync.wait()
            }
        }

        assertEquals(listOf(0), threadsCompletion)
    }

    @Test
    fun oneThreadWithReentrantCheck() {
        val lockObject = 0
        val locker = SimpleEntityLocker<Int, Int> { it }

        val threadsCompletion = mutableListOf<Int>()
        val threadCompletedSync = Object()

        Thread {
            locker.lock(lockObject) {
                threadsCompletion.add(0)
                locker.lock(lockObject, 1000L) {
                    threadsCompletion.add(1)
                }
            }
            synchronized(threadCompletedSync) { threadCompletedSync.notifyAll() }
        }.let {
            synchronized(threadCompletedSync) {
                it.start()
                threadCompletedSync.wait()
            }
        }

        assertEquals(listOf(0, 1), threadsCompletion)
    }
}
