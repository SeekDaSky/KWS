package KWS.Buffer

import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer

actual fun allocateDirectBufferIfAvailable(size : Int) : MultiplatformBuffer{
    return allocMultiplatformBuffer(size)
}