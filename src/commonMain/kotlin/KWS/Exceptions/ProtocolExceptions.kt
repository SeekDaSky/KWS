package KWS.Exceptions

/**
 * Exception thrown when trying to parse a malformed message according to the RFC
 */
class ProtocolException(message : String? = null) : Exception(message)

/**
 * Exception thrown when delaing with an invalid UT-8 payload
 */
class UTF8Exception(message: String? = null) : Exception(message)