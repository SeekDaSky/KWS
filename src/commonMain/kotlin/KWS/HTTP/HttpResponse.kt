package KWS.HTTP

class HttpResponse {
    private val version = "HTTP/1.1"

    val code : Int
    val codeText : String

    val headers : MutableMap<String,String> = mutableMapOf()

    constructor(code : Int, codeText : String){
        this.code = code
        this.codeText = codeText
    }

    fun addHeader(name : String, value : String){
        this.headers[name] = value
    }

    override fun toString(): String {
        var returned = "${this.version} ${this.code} ${this.codeText} \r\n"
        this.headers.forEach {
            returned += "${it.key}: ${it.value}\r\n"
        }
        returned += "\r\n"

        return returned
    }
}