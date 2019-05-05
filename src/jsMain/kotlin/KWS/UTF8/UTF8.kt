package KWS.UTF8

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array

actual fun encode(str: String): ByteArray{
    //use nodejs buffer to convert the string
    val buf = Buffer.from(str,"utf8")

    //get the uint8array (there is probably a cleaner way to get the int8array from the buffer but i'm lazy)
    val tmp = Uint8Array(buf.length)
    buf.copy(tmp,0,0,buf.length)

    //transform it to a int8array (which is mapped to the kotlin ByteArray type)
    return Int8Array.from(tmp)
}

actual fun decode(arr: ByteArray): String{
    val buf = Buffer.from(arr.unsafeCast<Int8Array>().buffer)
    return buf.toString("utf8")
}

/**
 * This implementation of isValid needs the package "utf-8-validate"
 */
actual fun isValid(arr : ByteArray) : Boolean{
    val buf = Buffer.from(arr.unsafeCast<Int8Array>().buffer)
    return require("utf-8-validate")(buf)
}

internal external fun require(module : String) : dynamic

internal external class Buffer : Uint8Array{

    companion object {
        fun alloc(size : Int) : Buffer
        fun allocUnsafe(size : Int) : Buffer
        fun from(arr : Array<Byte>) : Buffer
        fun from(str : String, charset : String) : Buffer
        fun from(buf : ArrayBuffer) : Buffer
        fun from(str : String) : Buffer
        fun compare(buf1 : Buffer, buf2 : Buffer) : Int
        fun concat(buf1 : Array<Buffer>, totalLength : Int) : Buffer
        fun concat(buf1 : Array<Buffer>) : Buffer
    }

    fun readInt8(offset : Int) : Byte
    fun readUInt8(offset : Int) : Byte
    fun readInt16BE(offset: Int) : Short
    fun readUInt16BE(offset: Int) : Short
    fun readInt32BE(offset: Int) : Int
    fun readUInt32BE(offset: Int) : Int

    fun writeInt8(value : Byte , offset: Int)
    fun writeInt16BE(value : Short, offset: Int)
    fun writeInt32BE(value : Int , offset: Int)

    fun write(str : String, offset: Int, encoding : String) : Int

    fun toString(charset: String) : String

    fun copy(target : Uint8Array, targetStart : Int, sourceStart : Int, sourceEnd : Int) : Int
    fun copy(target : Buffer, targetStart : Int, sourceStart : Int, sourceEnd : Int) : Int
    fun copy(target : Buffer) : Int
    fun copy(target : Uint8Array) : Int

    fun slice(begin : Int, end : Int) : Buffer
}

external class Int8Array{

    val buffer : ArrayBuffer

    constructor(size : Int)

    constructor(buffer : ArrayBuffer)

    companion object {
        fun from(arr : Uint8Array) : ByteArray
    }
}