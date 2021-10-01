package treecorel2

import chisel3._

class CTRL2PCIO extends Bundle with InstConfig {
  val jump:    Bool = Output(Bool())
  val stall:   Bool = Output(Bool())
  val maStall: Bool = Output(Bool())
  val newPC:   UInt = Output(UInt(BusWidth.W))
}
