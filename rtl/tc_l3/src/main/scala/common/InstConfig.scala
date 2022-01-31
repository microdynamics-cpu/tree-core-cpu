package treecorel2

import chisel3._
import chisel3.util._

trait IOConfig {
  val XLen       = 64
  val InstLen    = 32
  val RegfileLen = 5
  val RegfileNum = 1 << RegfileLen
  val ISALen     = 6
  // branch prediction
  val GHRLen    = 5
  val PHTSize   = 1 << GHRLen
  val BTBIdxLen = 5
  val BTBPcLen  = XLen - BTBIdxLen
  val BTBTgtLen = XLen
  val BTBSize   = 1 << BTBIdxLen
}

trait InstConfig extends IOConfig {
  val SoCEna   = true
  val CacheEna = false
  // fetch
  val FlashStartAddr = "h0000000030000000".U(XLen.W)
  val DiffStartAddr  = "h0000000080000000".U(XLen.W)
  // cache
  val NWay          = 4
  val NBank         = 4
  val NSet          = 32
  val CacheLineSize = XLen * NBank
  val ICacheSize    = NWay * NSet * CacheLineSize
  val DCacheSize    = NWay * NSet * CacheLineSize

  // clint
  val ClintBaseAddr  = 0x02000000.U(XLen.W)
  val ClintBoundAddr = 0x0200bfff.U(XLen.W)
  val MSipOffset     = 0x0.U(XLen.W)
  val MTimeOffset    = 0xbff8.U(XLen.W)
  val MTimeCmpOffset = 0x4000.U(XLen.W)
}
