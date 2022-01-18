package treecorel2.common

import chisel3._
import chisel3.util._

trait InstConfig {
  val SoCEna            = false
  val XLen              = 64
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

  val NWay          = 4
  val NBank         = 4
  val NSet          = 32
  val CacheLineSize = XLen * NBank
  val ICacheSize    = NWay * NSet * CacheLineSize
  val DCacheSize    = NWay * NSet * CacheLineSize
}
