package treecorel2

import chisel3._

class TRANSIO extends Bundle with InstConfig {
  val ena:  Bool = Output(Bool())
  val addr: UInt = Output(UInt(BusWidth.W))
  val data: UInt = Output(UInt(BusWidth.W))
}
