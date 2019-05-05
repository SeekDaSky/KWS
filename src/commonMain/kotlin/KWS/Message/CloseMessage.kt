package KWS.Message

import KWS.Exceptions.ProtocolException
import KWS.OpCode.OpCode
import KWS.OpCode.getBinaryOpCode
import KWS.UTF8.decode
import KWS.UTF8.encode
import KWS.UTF8.isValid
import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer
import Sok.Buffer.wrapMultiplatformBuffer

class CloseMessage : Sendable {

    val code : UShort
    val reason : String

    companion object {
        fun isCloseCodeValid(code: UShort) : Boolean{
            //or else the compiler force us to put .toUSHort everywhere
            val code = code.toInt()
            if(code in 0..999) return false
            if(code in 1004..1006) return false
            if(code in 1012..2999) return false

            return true
        }
    }

    constructor(raw : ByteArray){
        if(raw.size == 1) throw ProtocolException("Close message payload length can not be 1")

        if(raw.size == 0){
            this.code = 1000.toUShort()
            this.reason = ""
        }else if(raw.size == 2){
            val buf = wrapMultiplatformBuffer(raw)
            this.code = buf.getUShort()
            this.reason = ""
            buf.destroy()
        }else{
            val buf = wrapMultiplatformBuffer(raw)
            this.code = buf.getUShort()
            val encoded = buf.getBytes(buf.capacity-2)
            if(!isValid(encoded)) throw ProtocolException("Invalid UTF-8")
            this.reason = decode(encoded)
            buf.destroy()
        }
    }

    constructor(code : UShort = 1000.toUShort(), reason : String = ""){
        this.code = code
        this.reason = reason
    }

    override fun toMultiplatformBuffer(): MultiplatformBuffer {
        val data : MultiplatformBuffer
        //as the string is UTF8, we have to encoded it to get its binary size (UTF8 chars can take up to 3 bytes)
        val encodedString = if(this.reason != "") encode(this.reason) else ByteArray(0)
        val payloadSize = encodedString.size+2

        if(payloadSize <= 125){
            data = allocMultiplatformBuffer(payloadSize + 2)
            data.run {
                putByte((0b10000000 + getBinaryOpCode(OpCode.CONNECTION_CLOSED)).toByte())
                putByte(payloadSize.toByte())
            }
        }else if(payloadSize <= 65535){
            data = allocMultiplatformBuffer(payloadSize + 4)
            data.run {
                putByte((0b10000000 + getBinaryOpCode(OpCode.CONNECTION_CLOSED)).toByte())
                putByte(126.toByte())
                putShort(payloadSize.toShort())
            }
        }else{
            data = allocMultiplatformBuffer(payloadSize + 10)
            data.run {
                putByte((0b10000000 + getBinaryOpCode(OpCode.CONNECTION_CLOSED)).toByte())
                putByte(127.toByte())
                putLong(payloadSize.toLong())
            }
        }

        data.putShort(this.code.toShort())

        if(this.reason != ""){
            data.putBytes(encode(this.reason))
        }

        data.cursor = 0

        return data
    }
}