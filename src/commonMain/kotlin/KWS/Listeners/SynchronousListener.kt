package KWS.Listeners

import KWS.Client.WsClient
import KWS.Message.CloseMessage
import KWS.Message.Sendable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

abstract class SynchronousListener : Listener(){

    private val dispatcherActor = createDispatcherActor()

    override suspend fun postMessage(client : WsClient, message: Sendable){
        this.dispatcherActor.send(Message(client,message))
    }

    override suspend fun postClosed(client: WsClient, message: CloseMessage){
        this.dispatcherActor.send(Closed(client,message))
    }

    override suspend fun postConnection(c : WsClient){
        this.dispatcherActor.send(Connection(c))
    }

    private fun createDispatcherActor() : SendChannel<Event>{
        val channel = Channel<Event>()
        this.listenerScope.launch {
            for(msg in channel){
                when(msg){
                    is Message -> this@SynchronousListener.processMessage(msg.client, msg.message)
                    is Closed -> this@SynchronousListener.processClosed(msg.client, msg.message)
                    is Connection -> this@SynchronousListener.processConnection(msg.client)
                }
            }
        }
        return channel
    }
}

sealed class Event(val client : WsClient)
class Message(client : WsClient, val message : Sendable) : Event(client)
class Connection(client: WsClient) : Event(client)
class Closed(client : WsClient, val message: Sendable) : Event(client)