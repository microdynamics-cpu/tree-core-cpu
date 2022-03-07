package treecorel2

import chisel3._
import chisel3.util._

class CacheReqIO extends Bundle with InstConfig {
  val addr = UInt(XLen.W)
  val data = UInt(XLen.W) // for write
  val mask = UInt((XLen / 8).W) // for write
}

class CacheRespIO extends Bundle with InstConfig {
  val data = UInt(XLen.W)
}

class CacheCoreIO extends Bundle {
  val abort = Input(Bool())
  val req   = Flipped(Valid(new CacheReqIO))
  val resp  = Valid(new CacheRespIO)
}

class CacheMemReqIO extends Bundle with InstConfig {
  val id   = UInt(4.W)
  val cmd  = UInt(4.W)
  val addr = UInt(XLen.W)
  val data = UInt(XLen.W)
  val len  = UInt(2.W)
}

class CacheMemRespIO extends Bundle with InstConfig {
  val id   = UInt(4.W)
  val cmd  = UInt(4.W)
  val data = UInt(XLen.W)

}

class CacheMemIO extends Bundle {
  val req  = Decoupled(new CacheMemReqIO)
  val resp = Flipped(Decoupled(new CacheMemRespIO))
}

class Cache(val cacheType: String) extends Module with InstConfig {
  val io = IO(new Bundle {
    val core = new CacheCoreIO
    val mem  = new CacheMemIO
  })
}
