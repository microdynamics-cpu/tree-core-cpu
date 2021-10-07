package treecorel2

import chisel3._
import chisel3.util._
//AXI parameters
trait AXI4Config extends InstConfig {
  val AxiReqLen = 2
  val AxiReqRd  = 0
  val AxiReqWt  = 1
  val AxiReqNop = 2

  val AxiDataWidth      = 64
  val AxiFlashDataWidth = 32
  val AxiPerifDataWidth = 32
  val AxiAddrWidth      = 32 // FIME: is right? the original val is 64
  val AxiProtLen        = 3
  val AxiIdLen          = 4
  val AxiUserLen        = 1
  val AxiSizeLen        = 2
  val AxiBurstLen       = 2
  val AxiCacheLen       = 4
  val AxiQosLen         = 4
  val AxiRegionLen      = 4
  val AxiRespLen        = 2
}
