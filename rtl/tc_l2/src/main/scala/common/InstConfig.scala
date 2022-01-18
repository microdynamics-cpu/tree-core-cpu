package treecorel2.common

import chisel3._
import chisel3.util._

trait InstConfig {
  val SoCEna            = false
  val XLen              = 64
  val flashStartAddr    = "h0000000030000000".U(XLen.W)
  val simStartAddr      = "h0000000080000000".U(XLen.W)
  val diffStartBaseAddr = "h0000000080000000".U(XLen.W)
  val socStartBaseAddr  = "h0000000000000000".U(XLen.W)
  val difftestAddrMask  = "hfffffffffffffff8".U(XLen.W)
  val socAddrMask       = "hffffffffffffffff".U(XLen.W)
  val instSoCRSize      = 2.U
  val instDiffRSize     = 3.U
  val diffRWSize        = 3.U
  val CacheEna          = false

  val NWay          = 4
  val NBank         = 4
  val NSet          = 32
  val CacheLineSize = XLen * NBank
  val ICacheSize    = NWay * NSet * CacheLineSize
  val DCacheSize    = NWay * NSet * CacheLineSize
}
