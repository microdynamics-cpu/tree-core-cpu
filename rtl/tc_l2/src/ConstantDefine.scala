package treecorel2

import chisel3._

// len: the bits number
trait ConstantDefine {
  val BusWidth     = 32
  val RegAddrLen   = 5
  val RegNum       = 32
  val ALUOpcodeLen = 7
  val ALUSelLen    = 3
}
