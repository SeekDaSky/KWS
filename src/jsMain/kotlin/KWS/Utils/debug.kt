package KWS.Utils

actual fun Throwable.printStackTrace(){
    console.log(this)
}

actual fun measureNanoTime(operation : () -> Unit) : Long{
    val time = process.hrtime()
    operation.invoke()
    val result = process.hrtime(time)
    return time[1].toLong()
}

internal external class process{
    companion object {
        fun hrtime() : Array<Int>
        fun hrtime(from : Array<Int>) : Array<Int>
    }
}