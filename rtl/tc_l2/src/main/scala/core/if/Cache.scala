package treecorel2

import chisel3._
import chisel3.util._

class CacheReqIO extends Bundle {
  //
  //    |--------- tag ---------|---index---|--offset--|
  //
  val addr = UInt(64.W)
  val data = UInt(64.W)
  val mask = UInt((64 / 8).W)
  val op   = UInt(1.W) // 0: rd   1: wr
}

class CacheRespIO extends Bundle {
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
}

class MemReq extends Bundle {
  val addr = UInt(64.W)
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
  val len  = UInt(2.W) // 0: 1(64bits)    1: 2   2: 4  3: 8
  val id   = UInt(4.W)
}

class MemResp extends Bundle {
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
  val id   = UInt(4.W)
}

class Cache extends Module {}
