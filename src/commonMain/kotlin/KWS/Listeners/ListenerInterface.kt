package KWS.Listeners

import KWS.Client.WsClient
import KWS.Message.CloseMessage
import KWS.Message.Sendable

interface ListenerInterface {
    /**
     * Method that will handle the standard events
     */
    suspend fun processMessage(client : WsClient, message: Sendable)

    /**
     * Method that will handle a client disconnecting
     */
    suspend fun processClosed(client: WsClient, message: Sendable)

    /**
     * Method that will handle a client connecting
     */
    suspend fun processConnection(c : WsClient)

    /**
     * Post a new message to process. The listener may execute it right away or later, it depends on the type
     * of listener
     */
    suspend fun postMessage(client : WsClient, message: Sendable)

    /**
     * Post a new close event to process. The listener may execute it right away or later, it depends on the type
     * of listener
     */
    suspend fun postClosed(client: WsClient, message: CloseMessage)

    /**
     * Post a new connection event to process. The listener may execute it right away or later, it depends on the type
     * of listener
     */
    suspend fun postConnection(c : WsClient)

}