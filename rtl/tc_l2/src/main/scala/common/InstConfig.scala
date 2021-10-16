package treecorel2

import chisel3._

// len: the bits number
trait InstConfig {
  val InstWidth    = 32
  val BusWidth     = 64
  val InstCacheLen = 128
  val RegAddrLen   = 5
  val RegNum       = 32
  val MemOffsetLen = 12

  val PCLoadStartAddr  = "h80000000"
  val PCFlashStartAddr = "h30000000"
  val TrapInst         = "h0000006b"
  // flush pipeline
  val NopInst = "h00000013"
  // control the data gen
  // |===============|
  // |  diff |  soc  |
  // |===============|
  // | false | false |
  // | true  | false |
  // | false | true  |
  // |===============|
  // val DiffEna = true
  // val SoCEna  = false
  //======================
  val DiffEna = false
  val SoCEna  = true
}
