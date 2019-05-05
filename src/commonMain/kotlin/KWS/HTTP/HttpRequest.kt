package KWS.HTTP

class HttpRequest {

    val version : String

    val method : String

    val URL : String

    val headers : Map<String,Any>

    constructor(httpVersion : String , method : String, URL : String, headers : Map<String,Any>){
        this.version = httpVersion
        this.method = method
        this.URL = URL
        this.headers =  headers
    }

    fun getHeaderSingleValue(name : String) : String{
        return this.headers[name].toString()
    }

    fun getHeaderMultipleValues(name : String) : List<String>?{
        val returned = this.headers[name]

        //messy shit
        if(returned is List<*>){
            @Suppress("UNCHECKED_CAST")
            return returned as List<String>
        }else{
            return null
        }
    }

    override fun toString(): String {
        return "method: ${this.method} \r\n" +
                "URL: ${this.URL} \r\n" +
                "HTTP version: ${this.version} \r\n" +
                "headers: ${this.headers.keys} \r\n" +
                "header values: ${this.headers.values}"
    }
}