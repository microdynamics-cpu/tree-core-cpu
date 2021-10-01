package treecorel2

import chisel3._
import treecorel2.common.ConstVal._

class LSINSTIO extends Bundle with InstConfig {
  val func3MSB: UInt = Output(UInt(1.W))
  val operType: UInt = Output(UInt(InstOperTypeLen.W))
  val valA:     UInt = Output(UInt(BusWidth.W))
  val valB:     UInt = Output(UInt(BusWidth.W))
  val offset:   UInt = Output(UInt(BusWidth.W))
}
