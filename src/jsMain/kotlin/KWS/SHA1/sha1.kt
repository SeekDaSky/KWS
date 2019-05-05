package KWS.SHA1

actual fun sha1(toHash : String) : String{
    val crypto = require("crypto")
    val shasum = crypto.createHash("sha1");
    shasum.update(toHash);
    return shasum.digest("base64");
}

external fun require(module : String) : dynamic