package KWS.Buffer

import Sok.Buffer.MultiplatformBuffer
import Sok.Buffer.allocDirectMultiplatformBuffer
import Sok.Buffer.allocMultiplatformBuffer

actual fun allocateDirectBufferIfAvailable(size : Int) : MultiplatformBuffer{
    try {
        return allocDirectMultiplatformBuffer(size)
    }catch (e : OutOfMemoryError){
        System.gc()
        return allocMultiplatformBuffer(size)
    }
}