package treecorel2

import chisel3._
import chisel3.util._

trait AXI4Config extends IOConfig {
  val AxiProtLen   = 3
  val AxiIdLen     = 4
  val AxiUserLen   = 1
  val AxiSizeLen   = 3 // NOTE: or 2?
  val AxiLen       = 8
  val AxiStrb      = 8
  val AxiBurstLen  = 2
  val AxiCacheLen  = 4
  val AxiQosLen    = 4
  val AxiRegionLen = 4
  val AxiRespLen   = 2
}
