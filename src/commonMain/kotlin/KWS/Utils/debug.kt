package KWS.Utils

expect fun Throwable.printStackTrace()

expect fun measureNanoTime(operation : () -> Unit) : Long