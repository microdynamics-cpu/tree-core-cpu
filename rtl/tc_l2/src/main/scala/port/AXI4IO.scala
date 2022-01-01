package treecorel2

import chisel3._
import chisel3.util._

class AXI4IO extends Bundle {
  val aw = new AXI4AWIO()
  val w = new AXI4WIO()
  val b = new AXI4BIO()
  val ar = new AXI4ARIO()
  val r = new AXI4RIO()
}

class AXI4AWIO extends Bundle {
  val valid = Output(Bool())
  val ready = Input(Bool())
  val size  = Output(UInt(3.W))
  val addr  = Output(UInt(64.W))
}

class AXI4WIO extends Bundle {
  val valid = Output(Bool())
  val ready = Input(Bool())
  val strb  = Output(UInt(8.W))
  val data  = Output(UInt(64.W))
}

class AXI4BIO extends Bundle {
  val valid = Input(Bool())
  val ready = Output(Bool())
}

class AXI4ARIO extends Bundle {
  val valid = Output(Bool())
  val ready = Input(Bool())
  val size  = Output(UInt(3.W))
  val addr  = Output(UInt(64.W))
}

class AXI4RIO extends Bundle {
  val valid = Input(Bool())
  val ready = Output(Bool())
  val data  = Input(UInt(64.W))
}