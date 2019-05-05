package KWS.Utils

import kotlin.system.getTimeNanos

actual fun Throwable.printStackTrace(){
    this.printStackTrace()
}

actual fun measureNanoTime(operation : () -> Unit) : Long{
    val time = getTimeNanos()
    operation.invoke()
    return getTimeNanos()-time
}