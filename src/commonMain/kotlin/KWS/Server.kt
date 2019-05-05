package KWS

import KWS.Buffer.allocateDirectBufferIfAvailable
import KWS.Client.WsClient
import KWS.Listeners.Listener
import KWS.Listeners.ListenerInterface
import KWS.Message.CloseMessage
import KWS.Message.Sendable
import KWS.Utils.ReadWriteLock
import Sok.Buffer.BufferPool
import Sok.Exceptions.OptionNotSupportedException
import Sok.Socket.Options.Options
import Sok.Socket.Options.SocketOption
import Sok.Socket.TCP.TCPServerSocket
import Sok.Socket.TCP.createTCPServerSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

private val _headerBufferPool = BufferPool(32,512){
    allocateDirectBufferIfAvailable(it)
}
private val _dataBufferPool = BufferPool(16,Server.BUFSIZE){
    allocateDirectBufferIfAvailable(it)
}

class Server{

    companion object {
        internal val BUFSIZE = 65_536
        //workaround for the freeze by default companion properties on K/N
        internal val headerBufferPool : BufferPool
            get() {
                return _headerBufferPool
            }
        internal val dataBufferPool : BufferPool
            get() {
                return _dataBufferPool
            }
    }

    private val serverScope = CoroutineScope(Dispatchers.Default + exceptionHandler())

    private val WsListeningSocket : TCPServerSocket

    private val listeners = mutableListOf<Listener>()

    private val listenersLock = ReadWriteLock()

    var isClosed = false
        private set

    internal constructor(socket: TCPServerSocket){
        this.WsListeningSocket = socket
    }

    fun startWs(){
        this.serverScope.launch {
            while(!this@Server.isClosed){
                val socket = this@Server.WsListeningSocket.accept()

                try {
                    socket.setOption(SocketOption(Options.SO_RCVBUF,Server.BUFSIZE))
                }catch (e : OptionNotSupportedException){

                }

                WsClient(socket,this@Server)
            }
        }
    }

    fun dispatchConnectionEvent(client : WsClient){
        this.serverScope.launch {
            this@Server.searchListener(client)!!.postConnection(client)
        }
    }

    fun dispatchMessageEvent(client: WsClient, message : Sendable){
        this.serverScope.launch {
            this@Server.searchListener(client)!!.postMessage(client,message)
        }
    }

    fun dispatchClosedEvent(client: WsClient, message : CloseMessage){
        this.serverScope.launch {
            this@Server.searchListener(client)!!.postClosed(client,message)
        }
    }

    fun registerListener(l : Listener){
        this.listenersLock.write {
            listeners.add(l)
        }
    }

    fun unregisterListener(l : Listener){
        this.listenersLock.write {
            listeners.remove(l)
        }
    }

    private fun searchListener(client : WsClient) : ListenerInterface?{
        if(client.boundListener != null){
            return client.boundListener
        }

        return this.listenersLock.read {
            for(l in this.listeners.iterator()){
                if(l.filter(client)){
                    return@read l
                }
            }

            return@read null
        }
    }

    private fun exceptionHandler() = CoroutineExceptionHandler { _, throwable ->
        println("Exception occured in server scope: ${throwable.message}")
    }
}

suspend fun createWsServer(address: String, port : Int) : Server{
    return Server(createTCPServerSocket(address,port))
}