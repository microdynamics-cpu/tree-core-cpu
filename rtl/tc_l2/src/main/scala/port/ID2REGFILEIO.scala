package treecorel2

import chisel3._

class ID2REGFILETRANSIO extends Bundle with InstConfig {
  val ena:  Bool = Output(Bool())
  val addr: UInt = Output(UInt(RegAddrLen.W))
  val data: UInt = Input(UInt(BusWidth.W))
}

class ID2REGFILEIO extends Bundle {
  val rdA: ID2REGFILETRANSIO = new ID2REGFILETRANSIO
  val rdB: ID2REGFILETRANSIO = new ID2REGFILETRANSIO
}
