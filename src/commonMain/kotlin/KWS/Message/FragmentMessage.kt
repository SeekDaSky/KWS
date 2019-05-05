package KWS.Message

import KWS.OpCode.OpCode
import Sok.Buffer.MultiplatformBuffer

class FragmentMessage(content : ByteArray, isFIN : Boolean = true) : DataFrame(content, isFIN) {

    override fun toMultiplatformBuffer() : MultiplatformBuffer {
        return this.toMultiplatformBuffer(OpCode.FRAGMENT)
    }
}