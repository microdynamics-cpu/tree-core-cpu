package treecorel2

import chisel3._

class TRANSIO(addrWth: Int, dataWth: Int) extends Bundle with InstConfig {
  val ena:  Bool = Output(Bool())
  val addr: UInt = Output(UInt(addrWth.W))
  val data: UInt = Output(UInt(dataWth.W))
}
