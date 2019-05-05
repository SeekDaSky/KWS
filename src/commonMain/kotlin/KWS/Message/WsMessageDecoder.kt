package KWS.Message

import KWS.Buffer.allocateDirectBufferIfAvailable
import KWS.Exceptions.ProtocolException
import KWS.OpCode.OpCode
import KWS.OpCode.getOpcode
import KWS.Server
import KWS.UTF8.decode
import KWS.Utils.measureNanoTime
import Sok.Buffer.allocMultiplatformBuffer
import Sok.Socket.TCP.TCPClientSocket
import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.math.min

class WsMessageDecoder(val socket : TCPClientSocket) {
    
    private val headersBuffer = allocateDirectBufferIfAvailable(14)
    
    suspend fun getNextMessage() : Sendable{
        this.headersBuffer.reset()
        this.headersBuffer.limit = 2
        this.socket.read(this.headersBuffer,2)

        val firstByte = this.headersBuffer[0]
        val opcode = getOpcode(firstByte)
        val rsv = firstByte and 0b01110000

        val isFIN = (firstByte and 0b10000000.toByte()).toInt() == -128

        val secondByte = this.headersBuffer[1]
        val payloadLength = (secondByte and 0b01111111).toInt()
        val masked = (secondByte.toInt() and 0b10000000) != 0

        //process real payload length
        val realPayloadLength : Long
        if(payloadLength == 126){
            this.headersBuffer.limit += 2+if(masked) 4 else 0
            this.socket.read(this.headersBuffer,2+if(masked) 4 else 0)
            realPayloadLength = this.headersBuffer.getUShort(2).toLong()
        }else if(payloadLength == 127){

            this.headersBuffer.limit += 8+if(masked) 4 else 0
            this.socket.read(this.headersBuffer,8+if(masked) 4 else 0)
            realPayloadLength = this.headersBuffer.getLong(2)
        }else{
            if(masked){
                this.headersBuffer.limit += 4
                this.socket.read(this.headersBuffer,4)
            }
            realPayloadLength = payloadLength.toLong()
        }

        val mask : ByteArray?
        if(masked){
            this.headersBuffer.cursor -= 4
            mask = this.headersBuffer.getBytes(4)
        }else{
            mask = null
        }

        val data = ByteArray(realPayloadLength.toInt())
        if (realPayloadLength > 0){
            //start data read loop. Note: KWS can't handle message larger than Int.MAX_VALUE
            val buffer = Server.dataBufferPool.requestBuffer()
            buffer.limit = min(realPayloadLength.toInt(), buffer.capacity)
            var received = 0
            this.socket.bulkRead(buffer){ buf, read ->
                buf.getBytes(data,0,received,read)
                received += read
                buf.limit = min(realPayloadLength.toInt() - received, buf.capacity)
                received != realPayloadLength.toInt()
            }
            Server.dataBufferPool.freeBuffer(buffer)
        }

        if(masked && mask != null){
            for(i in (0 until data.size)) {
                data[i] = data[i].xor(mask[i and 0x3])
            }
        }

        /**
         * Protocol realted checks
         */
        if(rsv != 0.toByte()){
            throw ProtocolException("RSV not null")
        }

        if(opcode == OpCode.RESERVED){
            throw ProtocolException("Reserved opcode received")
        }

        if(opcode in arrayOf(OpCode.PING, OpCode.PONG, OpCode.CONNECTION_CLOSED) && !isFIN){
            throw ProtocolException("Control frames should not be fragmented")
        }

        if(opcode == OpCode.CONNECTION_CLOSED && data.size >= 126){
            throw ProtocolException("Close reason is too long")
        }

        return when(opcode){
            OpCode.TEXT -> StringMessage(data,isFIN)
            OpCode.BINARY -> BinaryMessage(data,isFIN)
            OpCode.FRAGMENT -> FragmentMessage(data, isFIN)
            OpCode.PING -> PingMessage(data)
            OpCode.PONG -> PongMessage(data)
            OpCode.CONNECTION_CLOSED -> CloseMessage(data)
            OpCode.RESERVED -> throw Exception("Reserved Opcode received")
        }
    }
}