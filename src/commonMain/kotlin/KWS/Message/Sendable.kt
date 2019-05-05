package KWS.Message

import Sok.Buffer.MultiplatformBuffer

interface Sendable {
    fun toMultiplatformBuffer() : MultiplatformBuffer
}