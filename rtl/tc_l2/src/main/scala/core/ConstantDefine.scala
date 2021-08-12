package treecorel2

import chisel3._

// len: the bits number
trait ConstantDefine {
  val BusWidth     = 64
  val InstWidth    = 32
  val InstCacheLen = 128

  val RegAddrLen = 5
  val RegNum     = 32

  // InstTypeLen has same name local var in id stage
  val InstTypeLen = 3
  // ALUOperTypeLen has same name local var in exec stage
  val ALUOperTypeLen = 6

  val PcRegStartAddr = "h80000000"
  val TrapInst       = "h0000006b"
}
