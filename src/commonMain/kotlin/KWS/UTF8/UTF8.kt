package KWS.UTF8

/**
 * Transform a String object into a properly UTF8 encoded ByteArray
 * @param str Chaine de charactère a convertir
 * @return UTF-8 encoded binary
 */
expect fun encode(str: String): ByteArray

/**
 * Transform an UTF8 ByteArray into a proper String object
 *
 * @param arr Array representing an UTF8 String
 * @return Decded string
 */
expect fun decode(arr: ByteArray): String

/**
 * Return true if the given ByteArray is a valid UTF-8 string
 * @param arr UTF-8 to test
 */
expect fun isValid(arr : ByteArray) : Boolean