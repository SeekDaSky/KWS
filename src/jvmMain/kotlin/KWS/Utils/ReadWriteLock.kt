package KWS.Utils

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Multiplatform implementation of a Read-Write lock, as for now only JVM is multithreaded, JS and Native implementations
 * basically just call the operation argument. This lock should respect the following properties: Only one writer can execute
 * the opearation at a time, all readers can execute their opertion at the same time as long as there is no writer executing.
 */
actual class ReadWriteLock{

    val lock = ReentrantReadWriteLock()

    /**
     * Read operation, this method will block while there is writers executing
     *
     * @param operation code to execute when no writers are executing
     */
    actual fun <T>read(operation : () -> T) : T{
        return lock.read(operation)
    }

    /**
     * Write operation, this method will block while there is other writers or readers
     * executing
     *
     * @param operation code to execute when no writers or readers are executing
     */
    actual fun <T>write(operation : () -> T) : T{
        return this.lock.write(operation)
    }
}