import KWS.Client.WsClient
import KWS.Listeners.SynchronousListener
import KWS.Message.Sendable
import KWS.createWsServer
import Sok.Selector.Selector
import kotlinx.coroutines.runBlocking

fun main(args : Array<String>) = runBlocking{
    Selector.setDefaultScope(this)
    val server = createWsServer("127.0.0.1",9995)
    server.registerListener(EchoListener())
    server.startWs()
}


class EchoListener : SynchronousListener(){
    override suspend fun processMessage(client: WsClient, message: Sendable) {
        client.send(message)
    }

    override suspend fun processClosed(client: WsClient, message: Sendable) {
        this.unbindClient(client)
    }

    override suspend fun processConnection(c: WsClient) {
        this.bindClient(c)
    }
}