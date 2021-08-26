package treecorel2

import chisel3._

// len: the bits number
trait InstConfig {
  val InstWidth    = 32
  val BusWidth     = 64
  val AxiDataWidth = 64
  val AxiAddrWidth = 64
  val AxiSizeLen   = 2
  val AxiRespLen   = 2
  val AxiProtLen   = 3
  val AxiIdLen     = 4
  val AxiUserLen   = 1
  val InstCacheLen = 128

  val RegAddrLen = 5
  val RegNum     = 32

  val PcRegStartAddr = "h80000000"
  val TrapInst       = "h0000006b"
  // flush pipeline
  val NopInst = "h00000013"
}
