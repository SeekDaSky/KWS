package KWS.Client

import KWS.Exceptions.ProtocolException
import KWS.Exceptions.UTF8Exception
import KWS.HTTP.HttpRequest
import KWS.HTTP.HttpResponse
import KWS.HTTP.parse
import KWS.Listeners.Listener
import KWS.Message.*
import KWS.OpCode.OpCode
import KWS.OpCode.getOpcode
import KWS.SHA1.sha1
import KWS.Server
import KWS.UTF8.decode
import KWS.UTF8.encode
import KWS.UTF8.isValid
import KWS.Utils.printStackTrace
import Sok.Buffer.allocMultiplatformBuffer
import Sok.Buffer.wrapMultiplatformBuffer
import Sok.Exceptions.CloseException
import Sok.Exceptions.SocketClosedException
import Sok.Socket.TCP.TCPClientSocket
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlin.experimental.and
import kotlin.experimental.xor
import kotlin.math.min

class WsClient(val socket: TCPClientSocket, val server: Server){

    val WsMessageDecoder = KWS.Message.WsMessageDecoder(this.socket)

    var isClosed = false
        private set

    private val protocols = listOf<String>()
    private var chosenProtocol = ""

    var isUpgraded = false
        private set

    private val isClosing = atomic(false)

    private val clientScope = CoroutineScope(Dispatchers.Default + exceptionHandler())
    lateinit var HttpRequest : HttpRequest

    var boundListener : Listener? = null

    private var receiveFragmentHolder : FragmentHolder? = null

    init {
        clientScope.launch {
            //process headers, we assume everything is encoded in raw ASCII so we decode the string before concatenation
            var headers = ""
            while(!headers.endsWith("\r\n\r\n")){
                val buf = Server.headerBufferPool.requestBuffer()
                this@WsClient.socket.read(buf)
                buf.limit = buf.cursor
                headers += decode(buf.toArray())
                Server.headerBufferPool.freeBuffer(buf)
            }

            //process upgrade
            this@WsClient.processUpgrade(headers)

            //start main reading loop
            while(!this@WsClient.isClosed) {

                val message : Sendable
                try {
                    message = this@WsClient.WsMessageDecoder.getNextMessage()

                    //discard messages while we are waiting for a close one
                    if(this@WsClient.isClosing.value && message !is CloseMessage){
                        continue
                    }

                    run w@{
                        when(message) {
                            is CloseMessage -> {
                                if(!CloseMessage.isCloseCodeValid(message.code)) throw ProtocolException("Invalid close code")
                                if(!this@WsClient.isClosing.compareAndSet(false,true)){
                                    this@WsClient.forceClose()
                                    return@launch
                                }else{
                                    this@WsClient.send(message)
                                    this@WsClient.server.dispatchClosedEvent(this@WsClient,message)
                                    this@WsClient.forceClose()
                                }
                            }
                            is PingMessage -> this@WsClient.send(PongMessage(message.content))
                            is PongMessage -> return@w
                            is DataFrame -> {
                                if(message is FragmentMessage && this@WsClient.receiveFragmentHolder == null) throw ProtocolException("A fragment message should be received without an ongoing fragment transmission")
                                if(!message.isFIN || this@WsClient.receiveFragmentHolder != null){
                                    if(this@WsClient.receiveFragmentHolder == null){
                                        this@WsClient.receiveFragmentHolder = FragmentHolder(getOpcode(message))
                                    }


                                    this@WsClient.receiveFragmentHolder!!.addFragment(message)

                                    if(this@WsClient.receiveFragmentHolder!!.isComplete){
                                        val complete = this@WsClient.receiveFragmentHolder!!.getCompleteMessage()
                                        if(complete is StringMessage && !isValid(complete.content)) throw UTF8Exception("Invalid UTF-8")
                                        this@WsClient.server.dispatchMessageEvent(this@WsClient,complete)
                                        this@WsClient.receiveFragmentHolder = null
                                    }
                                }else{
                                    if(message is StringMessage && !isValid(message.content)) throw UTF8Exception("Invalid UTF-8")
                                    this@WsClient.server.dispatchMessageEvent(this@WsClient,message)
                                }
                            }
                        }
                    }
                }catch (e : ProtocolException){
                    this@WsClient.close(CloseMessage(1002.toUShort(),"Protocol Error"))
                    continue
                }catch (e : UTF8Exception){
                    this@WsClient.close(CloseMessage(1007.toUShort(),"Wrong Code"))
                }
            }
        }
    }

    private suspend fun processUpgrade(HTTPHead : String){
        val request = parse(HTTPHead)
        this.HttpRequest = request

        val clientSecKey = request.getHeaderSingleValue("Sec-WebSocket-Key")

        // compute the server security key
        val secKey = sha1(clientSecKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11");

        // build the response headers
        val response = HttpResponse(101,"Switching Protocols")
        response.run {
            addHeader("Upgrade", "websocket")
            addHeader("Connection","Upgrade")
            addHeader("Sec-WebSocket-Version","13")
            addHeader("Sec-WebSocket-Accept",secKey)
        }

        if(this.protocols.isNotEmpty()){
            val clientProtocols = request.getHeaderSingleValue("Sec-WebSocket-Protocol").split(";")
            clientProtocols.forEach {
                if(this.protocols.contains(it)){
                    response.addHeader("Sec-WebSocket-Protocol",it)
                    this.chosenProtocol = it
                    return@forEach
                }
            }
        }

        //send response
        val buf = wrapMultiplatformBuffer(encode(response.toString()))
        this.socket.write(buf)
        buf.destroy()

        //dispatch the connection event
        this.isUpgraded = true
        this.server.dispatchConnectionEvent(this)

    }

    suspend fun send(message : Sendable){
        if(message is CloseMessage){
            this.isClosing.compareAndSet(false,true)
            this.receiveFragmentHolder = null
        }

        if(!this.isClosing.value || message is CloseMessage){
            val buf = message.toMultiplatformBuffer()
            this.socket.write(buf)
            buf.destroy()
        }else{
            throw Exception("Client is closing")
        }
    }

    private fun exceptionHandler() = CoroutineExceptionHandler { _, throwable ->
        if(this.isClosing.value && (throwable is CloseException || throwable is SocketClosedException)) return@CoroutineExceptionHandler
        println("Exception in client scope")
        throwable.printStackTrace()
        GlobalScope.launch {
            this@WsClient.forceClose()
        }
    }

    suspend fun close(closeToken : CloseMessage = CloseMessage()){
        this.send(closeToken)
    }

    suspend fun forceClose(){
        this.isClosing.value = true
        this.isClosed = true
        this.socket.close()
        this.clientScope.coroutineContext.cancel()
    }
}