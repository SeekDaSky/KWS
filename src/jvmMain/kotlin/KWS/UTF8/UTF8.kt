package KWS.UTF8

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

actual fun encode(str: String): ByteArray{
    return str.toByteArray(Charsets.UTF_8)
}

actual fun decode(arr: ByteArray): String{
    return String(arr,Charsets.UTF_8)
}

actual fun isValid(arr : ByteArray) : Boolean{
    try {
        Charset.forName("UTF8").newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(arr))
        return true
    }catch (e : Exception){
        return false
    }

}