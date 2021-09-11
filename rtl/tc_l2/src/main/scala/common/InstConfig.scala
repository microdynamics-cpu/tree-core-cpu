package treecorel2

import chisel3._

// len: the bits number
trait InstConfig {
  val InstWidth    = 32
  val BusWidth     = 64
  val InstCacheLen = 128
  val RegAddrLen   = 5
  val RegNum       = 32

  /** the number of cycles required to increment the mtime register by 1 */
  // val TickCnt:        Int  = 0x100
  // val MSipOffset:     UInt = 0x0.U
  // val MTimeCmpOffset: UInt = 0x4000.U
  // val MTimeOffset:    UInt = 0xbff8.U

  val PcRegStartAddr = "h80000000"
  val TrapInst       = "h0000006b"
  // flush pipeline
  val NopInst = "h00000013"
}
