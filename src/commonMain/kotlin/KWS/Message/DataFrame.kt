package KWS.Message

import KWS.OpCode.OpCode
import KWS.OpCode.getBinaryOpCode
import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer

abstract class DataFrame(val content : ByteArray, val isFIN : Boolean = true) : Sendable {

    internal fun toMultiplatformBuffer(opCode: OpCode) : MultiplatformBuffer{
        val data : MultiplatformBuffer
        val payloadSize = this.content.size

        if(payloadSize <= 125){
            data = allocMultiplatformBuffer(payloadSize + 2)
            data.run {
                putByte((0b10000000 + getBinaryOpCode(opCode)).toByte())
                putByte(payloadSize.toByte())
            }
        }else if(payloadSize <= 65535){
            data = allocMultiplatformBuffer(payloadSize + 4)
            data.run {
                putByte((0b10000000 + getBinaryOpCode(opCode)).toByte())
                putByte(126.toByte())
                putShort(payloadSize.toShort())
            }
        }else{
            data = allocMultiplatformBuffer(payloadSize + 10)
            data.run {
                putByte((0b10000000 + getBinaryOpCode(opCode)).toByte())
                putByte(127.toByte())
                putLong(payloadSize.toLong())
            }
        }

        data.putBytes(this.content)
        data.cursor = 0

        return data
    }
}