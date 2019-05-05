package KWS.Message

import KWS.OpCode.OpCode
import KWS.OpCode.getBinaryOpCode
import KWS.UTF8.decode
import KWS.UTF8.encode
import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer

class StringMessage(content : ByteArray, isFIN : Boolean = true) : DataFrame(content, isFIN) {

    val text by lazy(LazyThreadSafetyMode.NONE){
        decode(this.content)
    }

    override fun toMultiplatformBuffer() : MultiplatformBuffer{
        return toMultiplatformBuffer(OpCode.TEXT)
    }
}