package treecorel2

import chisel3._

class INTRIO extends Bundle with InstConfig {
  val mtip: Bool = Output(Bool())
  val msip: Bool = Output(Bool())
}
