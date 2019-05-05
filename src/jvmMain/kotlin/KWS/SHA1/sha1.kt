package KWS.SHA1

import java.security.MessageDigest
import java.util.*

actual fun sha1(toHash : String) : String{
    return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(toHash.toByteArray(Charsets.UTF_8)))
}