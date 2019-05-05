package KWS.Listeners

import KWS.Client.WsClient
import KWS.Message.Sendable
import KWS.Utils.ReadWriteLock
import KWS.Utils.printStackTrace
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

abstract class Listener : ListenerInterface{

    private val boundClients = mutableListOf<WsClient>()
    protected val listenerScope = CoroutineScope(Dispatchers.Default + exceptionHandler())
    private val bindingClientLock = ReadWriteLock()

    fun bindClient(c : WsClient){
        this.bindingClientLock.write {
            this.boundClients.add(c)
        }
    }

    suspend fun broadcast(msg : Sendable){
        val deferreds = mutableListOf<Deferred<Unit>>()
        this.bindingClientLock.read {
            for(client in this.boundClientIterator()){
                deferreds.add(this.listenerScope.async {
                    client.send(msg)
                })
            }
        }

        deferreds.awaitAll()
    }

    suspend fun broadcastExceptOne(msg: Sendable, client : WsClient){
        val deferreds = mutableListOf<Deferred<Unit>>()

        this.bindingClientLock.read {
            for(c in this.boundClientIterator()){
                if(c != client){
                    deferreds.add(this.listenerScope.async {
                        client.send(msg)
                    })
                }
            }
        }

        deferreds.awaitAll()
    }

    fun unbindClient(c : WsClient){
        c.boundListener = null

        this.bindingClientLock.write {
            this.boundClients.remove(c)
        }
    }

    fun boundClientIterator() : Iterator<WsClient>{
        return this.boundClients.iterator()
    }

    fun boundClientListSize() : Int{
        return this.boundClients.size

    }

    open fun exceptionHandler() = CoroutineExceptionHandler { _, throwable ->
        println("Exception in Listener scope")
        throwable.printStackTrace()
    }

    /**
     * Method that will be called by the server to filter whether the event related to the client should be passed to the listener or not
     * Should return true if the client is allowed to access the listener
     *
     * @param Client Client to check
     * @return Boolean
     */
    open fun filter(c : WsClient) : Boolean{

        return true
    }

}