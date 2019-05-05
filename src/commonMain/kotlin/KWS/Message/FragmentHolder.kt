package KWS.Message

import KWS.Exceptions.ProtocolException
import KWS.OpCode.OpCode

internal class FragmentHolder(private val opCode: OpCode){

    private val fragments = mutableListOf<DataFrame>()
    var isComplete = false
        private set

    init {
        if(this.opCode == OpCode.FRAGMENT) throw ProtocolException("A non-zero OpCode must be given before sending fragments")
    }

    fun addFragment(fragment : DataFrame){
        if(this.fragments.size == 0 && fragment is FragmentMessage) throw ProtocolException("A fragment message can not be added as first fragment")
        if(this.fragments.size != 0 && fragment !is FragmentMessage) throw ProtocolException("A non-fragment message can not be added after the first fragment")

        if(this.isComplete) throw ProtocolException("The fragment message is complete, no more fragments can be added")

        if(fragment.isFIN && this.fragments.size == 0) throw ProtocolException("Can not create a fragment holder with a non-fragment message")

        this.isComplete = fragment.isFIN
        this.fragments.add(fragment)
    }

    fun getCompleteMessage() : DataFrame{
        var fullSize = 0
        var copied = 0
        this.fragments.forEach { fullSize += it.content.size }
        val completeContent = ByteArray(fullSize)
        this.fragments.forEach {
            it.content.copyInto(completeContent,copied)
            copied += it.content.size
        }

        return when(this.opCode){
            OpCode.TEXT -> StringMessage(completeContent,true)
            OpCode.BINARY -> BinaryMessage(completeContent,true)
            else -> throw Exception("Unauthorized OpCode")
        }
    }
}