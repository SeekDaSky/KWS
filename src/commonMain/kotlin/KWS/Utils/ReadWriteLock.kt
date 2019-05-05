package KWS.Utils

/**
 * Multiplatform implementation of a Read-Write lock, as for now only JVM is multithreaded, JS and Native implementations
 * basically just call the operation argument. This lock should respect the following properties: Only one writer can execute
 * the opearation at a time, all readers can execute their opertion at the same time as long as there is no writer executing.
 */
expect class ReadWriteLock(){

    /**
     * Read operation, this method will block while there is writers executing
     *
     * @param operation code to execute when no writers are executing
     */
    fun <T>read(operation : () -> T) : T


    /**
     * Write operation, this method will block while there is other writers or readers
     * executing
     *
     * @param operation code to execute when no writers or readers are executing
     */
    fun <T>write(operation : () -> T) : T
}