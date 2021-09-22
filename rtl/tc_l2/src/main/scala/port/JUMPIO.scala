package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class JUMPIO extends Bundle with InstConfig {
  val kind: UInt = Output(UInt(JumpTypeLen.W))
  val addr: UInt = Output(UInt(BusWidth.W))
}
