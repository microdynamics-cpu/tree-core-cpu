package treecorel2

import chisel3._
import chisel3.util._

//AXI parameters
trait AXI4Config {
  val AxiDataWidth = 64
  val AxiAddrWidth = 64
  val AxiSizeLen   = 2
  val AxiRespLen   = 2
  val AxiProtLen   = 3
  val AxiIdLen     = 4
  val AxiUserLen   = 1
  val AxiReqLen    = 2
  val AxiReqRd     = 0
  val AxiReqWt     = 1

  val AxiReqNop = 2

  // val addrBits   = 64
  // val dataBits   = 64
  // val rwaddrBits = 64
  // val rwdataBits = 64
  // val rwsizeBits = 2
  // val rwreqBits  = 1
  // val userBits   = 1
  // val idBits     = 4
  // val lenBits    = 8
  // val sizeBits   = 3
  // val burstBits  = 2
  // val cacheBits  = 4
  // val protBits   = 3
  // val qosBits    = 4
  // val respBits   = 2

  // val ALIGNED_WIDTH: Int = log2Ceil(dataBits / 8) //3
  // val OFFSET_WIDTH:  Int = log2Ceil(dataBits) //6
  // val AXI_SIZE:      Int = log2Ceil(dataBits / 8) //3
  // val MASK_WIDTH:    Int = dataBits * 2 //128
  // val TRANS_LEN:     Int = rwdataBits / dataBits //1

}
