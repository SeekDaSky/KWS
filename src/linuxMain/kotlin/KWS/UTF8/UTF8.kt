package KWS.UTF8


actual fun encode(str: String): ByteArray{
    return str.toUtf8()
}


actual fun decode(arr: ByteArray): String{
    return arr.stringFromUtf8()
}

actual fun isValid(arr : ByteArray) : Boolean{
    try{
        arr.stringFromUtf8OrThrow()
        return true
    }catch (e : Exception){
        return false
    }
}