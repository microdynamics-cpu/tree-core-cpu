package treecorel2.common

import chisel3._
import chisel3.util._

trait InstConfig {
  val SoCEna            = false
  val XLen              = 64
  val InstLen           = 32
  val RegfileNum        = 32
  val FlashStartAddr    = "h0000000030000000".U(XLen.W)
  val SimStartAddr      = "h0000000080000000".U(XLen.W)
  val DiffStartBaseAddr = "h0000000080000000".U(XLen.W)
  val SoCStartBaseAddr  = "h0000000000000000".U(XLen.W)
  val DifftestAddrMask  = "hfffffffffffffff8".U(XLen.W)
  val SoCAddrMask       = "hffffffffffffffff".U(XLen.W)
  val InstSoCRSize      = 2.U
  val InstDiffRSize     = 3.U
  val DiffRWSize        = 3.U
  val CacheEna          = false

  // inst type
  // nop is equal to [addi x0, x0, 0], so the oper is same as 'addi' inst
  val InstTypeLen = 3
  val nopInstType = 2.U(InstTypeLen.W)
  val rInstType   = 1.U(InstTypeLen.W)
  val iInstType   = 2.U(InstTypeLen.W)
  val sInstType   = 3.U(InstTypeLen.W)
  val bInstType   = 4.U(InstTypeLen.W)
  val uInstType   = 5.U(InstTypeLen.W)
  val jInstType   = 6.U(InstTypeLen.W)

  val NWay          = 4
  val NBank         = 4
  val NSet          = 32
  val CacheLineSize = XLen * NBank
  val ICacheSize    = NWay * NSet * CacheLineSize
  val DCacheSize    = NWay * NSet * CacheLineSize
}
