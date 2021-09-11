package treecorel2

import chisel3._

class INSTIO extends Bundle with InstConfig {
  val addr: UInt = Input(UInt(BusWidth.W))
  val data: UInt = Input(UInt(InstWidth.W))
}
