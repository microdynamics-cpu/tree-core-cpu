package treecorel2

import chisel3._
import chisel3.util._

object CacheConfig {
  val ICacheSize = (256 * 1024)
  val DCacheSize = (256 * 1024)
  val LineSize   = 256
  val NWay       = 4
  val NBAN       = 4
  val IdLen      = 1
}

class CACHEREQIO extends Bundle {
  val addr = UInt(64.W)
  val data = UInt(64.W)
  val mask = UInt((64 / 8).W)
  val op   = UInt(1.W) // 0: rd   1: wr
}

class CACHERESPIO extends Bundle {
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
}

class MEMREQIO extends Bundle {
  val addr = UInt(64.W)
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
  val len  = UInt(2.W) // 0: 1(64bits)    1: 2   2: 4  3: 8
  val id   = UInt(CacheConfig.IdLen.W)
}

class MEMRESPIO extends Bundle {
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
  val id   = UInt(CacheConfig.IdLen.W)
}

class WayIn(val tagWidth: Int, val idxWidth: Int, val offsetWidth: Int) extends Bundle {
  val wt = Valid(new Bundle {
    val tag    = UInt(tagWidth.W)
    val idx    = UInt(idxWidth.W)
    val offset = UInt(offsetWidth.W)
    val v      = UInt(1.W)
    val d      = UInt(1.W)
    val mask   = UInt(((CacheConfig.LineSize / CacheConfig.NBank) / 8).W)
    val data   = UInt(p(XLen).W)
    val op     = UInt(1.W) // must 1
  })
  val rd = Valid(new Bundle {
    val idx = UInt(idxWidth.W)
    val op  = UInt(1.W) // must 0
  })
}

class WayOut(val tagWidth: Int) extends Bundle {
  val tag  = UInt(tagWidth.W)
  val v    = UInt(1.W)
  val d    = UInt(1.W)
  val data = Vec(CacheConfig.NBank, UInt((CacheConfig.LineSize / CacheConfig.NBank).W))
}

class CACHE2CPUIO extends Bundle {
  val req  = Flipped(Decoupled(new CACHEREQIO))
  val resp = Valid(new CACHERESPIO)
}

class CACHE2MEMIO extends Bundle {
  val req  = Decoupled(new MEMREQIO)
  val resp = Flipped(Decoupled(new MEMRESPIO))
}

class Cache extends Module {}
