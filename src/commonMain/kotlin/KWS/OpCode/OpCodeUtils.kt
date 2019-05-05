package KWS.OpCode

import KWS.Message.*
import kotlin.experimental.and

/**
* Returns the OpCode having the corresponding binary code
*
* @param Byte Byte to analyse
* @return OpCode
*/
internal fun getOpcode(raw : Byte) : OpCode{
	return when((raw and 0b00001111).toInt()){
		0 -> OpCode.FRAGMENT
		1 -> OpCode.TEXT
		2 -> OpCode.BINARY
		8 -> OpCode.CONNECTION_CLOSED
		9 -> OpCode.PING
		10-> OpCode.PONG
		
		else -> OpCode.RESERVED
	}
}

/**
 * Return the OpCode of a Sendable object
 *
 * @param obj Sendable to evaluate
 * @return OpCode of this object
 */
internal fun getOpcode(obj : Sendable) : OpCode{
	return when(obj){
		is FragmentMessage -> OpCode.FRAGMENT
		is StringMessage -> OpCode.TEXT
		is BinaryMessage -> OpCode.BINARY
		is CloseMessage -> OpCode.CONNECTION_CLOSED
		is PingMessage -> OpCode.PING
		is PongMessage-> OpCode.PONG

		else -> throw Exception("Unknown object given")
	}
}

/**
* Returns the binary representation of the OpCode
*
* @param OpCode OpCode to convert
* @return Byte
*/
internal fun getBinaryOpCode(op : OpCode) : Byte {
	return when(op){
		OpCode.FRAGMENT				-> 0
		OpCode.TEXT					-> 1
		OpCode.BINARY				-> 2
		OpCode.CONNECTION_CLOSED	-> 8
		OpCode.PING					-> 9
		OpCode.PONG					-> 10
		
		else -> throw Exception("Unknown operation code");
	}
}