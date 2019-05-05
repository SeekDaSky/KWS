package KWS.Utils

actual fun Throwable.printStackTrace(){
    this.printStackTrace()
}

actual fun measureNanoTime(operation : () -> Unit) : Long{
    return kotlin.system.measureNanoTime(operation)
}