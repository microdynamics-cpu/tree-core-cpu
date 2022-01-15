package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.InstConfig

class CacheReqIO extends Bundle with InstConfig {
  val addr = UInt(XLen.W)
  val data = UInt(XLen.W) // for write
  val mask = UInt((XLen / 8).W) // for write
  val op   = UInt(1.W)
}

class CacheRespIO extends Bundle with InstConfig {
  val cmd  = UInt(4.W)
  val data = UInt(XLen.W)
}

class CacheCoreIO extends Bundle {
  val req  = Flipped(Decoupled(new CacheReqIO))
  val resp = Valid(new CacheRespIO)
}
