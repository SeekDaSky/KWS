package KWS.SHA1

/**
 * Kotlin implementation copied from the Javascript implementation from http://locutus.io/php/sha1/
 *
 * TODO: use cinterop to use a proper C implementation of sha1
 *
 * @param String string to hash
 */
@Suppress("NAME_SHADOWING")
actual fun sha1 (toHash : String) : String{

    var i : Int
    var j : Int
    val W = IntArray(80)

    var H0 = 0x67452301.toInt()
    var H1 = 0xEFCDAB89.toInt()
    var H2 = 0x98BADCFE.toInt()
    var H3 = 0x10325476.toInt()
    var H4 = 0xC3D2E1F0.toInt()

    var A : Int
    var B : Int
    var C : Int
    var D : Int
    var E : Int

    var temp : Int
    var tempString : String
    
    val toHashLen = toHash.length
    val wordArray = mutableListOf<Int>()
    i = 0
    while (i < toHashLen - 3) {
        j =     toHash.elementAt(i).toInt().shl(24) or
                toHash.elementAt(i + 1).toInt().shl(16) or
                toHash.elementAt(i + 2).toInt().shl(8) or
                toHash.elementAt(i + 3).toInt()
        wordArray.add(j)

        i += 4
    }

    when (toHashLen % 4) {
        0 -> i = 0x80000000.toInt()
        1 -> i = toHash.elementAt(toHashLen - 1).toInt().shl(24) or 0x0800000
        2 -> i = toHash.elementAt(toHashLen - 2).toInt().shl(24) or toHash.elementAt(toHashLen - 1).toInt().shl(16) or 0x08000
        3 -> i = toHash.elementAt(toHashLen - 3).toInt().shl(24) or
                toHash.elementAt(toHashLen - 2).toInt().shl(16) or
                toHash.elementAt(toHashLen - 1).toInt().shl(8) or 0x80
    }

    wordArray.add(i)
    while ((wordArray.size % 16) != 14) {
        wordArray.add(0)
    }

    wordArray.add(toHashLen.ushr(29))
    wordArray.add((toHashLen.shl(3) and 0xffffffff.toInt()))

    var blockstart : Int = 0
    while (blockstart < wordArray.size) {

        for (i in 0..15) {
            W[i] = wordArray[blockstart + i]
        }

        for (i in 16 .. 79) {
            W[i] = _rotLeft(W[i - 3] xor W[i - 8] xor W[i - 14] xor W[i - 16], 1)
        }

        A = H0
        B = H1
        C = H2
        D = H3
        E = H4

        for (i in 0..19) {
            temp = (_rotLeft(A, 5) + ((B and C) or (B.inv() and D)) + E + W[i] + 0x5A827999) and 0xffffffff.toInt()
            E = D
            D = C
            C = _rotLeft(B, 30)
            B = A
            A = temp
        }

        for (i in 20..39) {
            temp = (_rotLeft(A, 5) + (B xor C xor D) + E + W[i] + 0x6ED9EBA1) and 0xffffffff.toInt()
            E = D
            D = C
            C = _rotLeft(B, 30)
            B = A
            A = temp
        }

        for (i in 40..59) {
            temp = (_rotLeft(A, 5) + ((B and C) or (B and D) or (C and D)) + E + W[i] + 0x8F1BBCDC.toInt()) and 0xffffffff.toInt()
            E = D
            D = C
            C = _rotLeft(B, 30)
            B = A
            A = temp
        }

        for (i in 60..79) {
            temp = (_rotLeft(A, 5) + (B xor C xor D) + E + W[i] + 0xCA62C1D6.toInt()) and 0xffffffff.toInt()
            E = D
            D = C
            C = _rotLeft(B, 30)
            B = A
            A = temp
        }

        H0 = (H0 + A) and 0xffffffff.toInt()
        H1 = (H1 + B) and 0xffffffff.toInt()
        H2 = (H2 + C) and 0xffffffff.toInt()
        H3 = (H3 + D) and 0xffffffff.toInt()
        H4 = (H4 + E) and 0xffffffff.toInt()

        blockstart += 16
    }
    tempString = _cvtHex(H0) + _cvtHex(H1) + _cvtHex(H2) + _cvtHex(H3) + _cvtHex(H4)
    return base64Encode(hexStringToByteArray(tempString.toUpperCase()))
}

private fun _rotLeft(n : Int, s : Int) : Int{
    val t4 = (n.shl(s)) or (n.ushr(32 - s))
    return t4
}

private fun _cvtHex(value : Int) : String{
    var str = ""
    var i : Int
    var v : Int
    for(i in (0..7).reversed()){
        v = (value.ushr(i * 4)) and 0x0f
        str += v.toString(16)
    }
    return str
}

private fun base64Encode(data: ByteArray): String {
    val tbl = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/')

    var buffer = ""
    var pad = 0
    var i = 0
    while (i < data.size) {

        var b = data[i].toInt() and 0xFF shl 16 and 0xFFFFFF
        if (i + 1 < data.size) {
            b = b or (data[i + 1].toInt() and 0xFF shl 8)
        } else {
            pad++
        }
        if (i + 2 < data.size) {
            b = b or (data[i + 2].toInt() and 0xFF)
        } else {
            pad++
        }

        for (j in 0..4 - pad - 1) {
            val c = b and 0xFC0000 shr 18
            buffer += tbl[c]
            b = b shl 6
        }
        i += 3
    }
    for (j in 0..pad - 1) {
        buffer += "="
    }

    return buffer
}

private fun hexStringToByteArray(s : String) : ByteArray {

    val HEX_CHARS = "0123456789ABCDEF"
    val result = ByteArray(s.length / 2)

    for (i in 0 until s.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(s[i])
        val secondIndex = HEX_CHARS.indexOf(s[i + 1])

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}