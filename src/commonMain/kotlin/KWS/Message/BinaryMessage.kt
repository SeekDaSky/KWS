package KWS.Message

import KWS.OpCode.OpCode
import KWS.OpCode.getBinaryOpCode
import KWS.UTF8.encode
import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer

class BinaryMessage(content : ByteArray, isFIN : Boolean = true) : DataFrame(content, isFIN) {

    override fun toMultiplatformBuffer() : MultiplatformBuffer{
        return this.toMultiplatformBuffer(OpCode.BINARY)
    }
}