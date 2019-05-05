package KWS.HTTP

fun parse(rawHeaders : String) : HttpRequest{
    val splited = rawHeaders.split("\r\n")
    val map = mutableMapOf<String,Any>()

    var method = ""
    var URL = ""
    var version = ""

    splited.forEachIndexed{ index,it ->
        it.removeSuffix("\r\n")
        val values = it.split(" ")

        if(index == 0){
            method = values[0]
            URL = values[1]
            version = values[2]
        }else if(values[0].isNotBlank()){
            //if it is a single value
            if(values.size == 2){
                map[values[0].trimEnd(':')] = values[1]
            //if there is multiple values (delimiter: space)
            }else{
                map[values[0].trimEnd(':')] = values.drop(1)
            }
        }
    }

    return HttpRequest(version,method,URL,map)
}