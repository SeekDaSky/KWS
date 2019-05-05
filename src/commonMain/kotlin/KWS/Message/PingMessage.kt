package KWS.Message

import KWS.Exceptions.ProtocolException
import KWS.OpCode.OpCode
import KWS.OpCode.getBinaryOpCode
import KWS.UTF8.decode
import KWS.UTF8.encode
import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer

class PingMessage(var content : ByteArray) : Sendable {

    constructor(content : String) : this(encode(content))

    val stringContent : String
        get() {
            return decode(this.content)
        }

    init{
        if(this.content.size > 125){
            throw ProtocolException("Ping payload is too big (>125)")
        }
    }

    override fun toMultiplatformBuffer() : MultiplatformBuffer {
        val data : MultiplatformBuffer
        val payloadSize = this.content.size

        data = allocMultiplatformBuffer(payloadSize + 2)
        data.run {
            putByte((0b10000000 + getBinaryOpCode(OpCode.PING)).toByte())
            putByte(payloadSize.toByte())
        }

        data.putBytes(this.content)
        data.cursor = 0

        return data
    }
}