package treecorel2

import chisel3._
import chisel3.util._

//AXI parameters
trait AXI4Config {
  val AxiReqLen = 2
  val AxiReqRd  = 0
  val AxiReqWt  = 1
  val AxiReqNop = 2

  val AxiDataWidth = 64
  val AxiAddrWidth = 64
  val AxiProtLen   = 3
  val AxiIdLen     = 4
  val AxiUserLen   = 1
  val AxiSizeLen   = 2
  val AxiBurstLen  = 2
  val AxiCacheLen  = 4
  val AxiQosLen    = 4
  val AxiRegionLen = 4
  val AxiRespLen   = 2

  // val ALIGNED_WIDTH: Int = log2Ceil(dataBits / 8) //3
  // val OFFSET_WIDTH:  Int = log2Ceil(dataBits) //6
  // val AXI_SIZE:      Int = log2Ceil(dataBits / 8) //3
  // val MASK_WIDTH:    Int = dataBits * 2 //128
  // val TRANS_LEN:     Int = rwdataBits / dataBits //1

}
