package KWS.Listeners

import KWS.Client.WsClient
import KWS.Message.CloseMessage
import KWS.Message.Sendable

abstract class AsynchronousListener : Listener(){
    override suspend fun postMessage(client : WsClient, message: Sendable){
        this.processMessage(client,message)
    }

    override suspend fun postClosed(client: WsClient, message: CloseMessage){
        this.processClosed(client,message)
    }

    override suspend fun postConnection(c : WsClient){
        this.processConnection(c)
    }
}