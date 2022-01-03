package treecorel2

import chisel3._
import chisel3.util._

class SOCAXI4ARWIO extends Bundle {
  val addr  = Output(UInt(32.W))
  val id    = Output(UInt(4.W))
  val len   = Output(UInt(8.W))
  val size  = Output(UInt(3.W))
  val burst = Output(UInt(2.W))
}

class AXI4ARWIO extends SOCAXI4ARWIO {
  override val addr = Output(UInt(64.W))
  val prot          = Output(UInt(3.W))
  val user          = Output(UInt(1.W))
  val lock          = Output(Bool())
  val cache         = Output(UInt(4.W))
  val qos           = Output(UInt(4.W))
}

class SOCAXI4WIO extends Bundle {
  val data = Output(UInt(64.W))
  val strb = Output(UInt(8.W))
  val last = Output(Bool())
}

class AXI4WIO extends SOCAXI4WIO {}

class SOCAXI4BIO extends Bundle {
  val resp = Output(UInt(2.W))
  val id   = Output(UInt(4.W))
}

class AXI4BIO extends SOCAXI4BIO {
  val user = Output(UInt(1.W))
}

class SOCAXI4RIO extends Bundle {
  val resp = Output(UInt(2.W))
  val data = Output(UInt(64.W))
  val last = Output(Bool())
  val id   = Output(UInt(4.W))
}

class AXI4RIO extends SOCAXI4RIO {
  val user = Output(UInt(1.W))
}

class SOCAXI4IO extends Bundle {
  val aw = Decoupled(new SOCAXI4ARWIO)
  val w  = Decoupled(new SOCAXI4WIO)
  val b  = Flipped(Decoupled(new SOCAXI4BIO))
  val ar = Decoupled(new SOCAXI4ARWIO)
  val r  = Flipped(Decoupled(new SOCAXI4RIO))
}

class AXI4IO extends SOCAXI4IO {
  override val aw = Decoupled(new AXI4ARWIO)
  override val w  = Decoupled(new AXI4WIO)
  override val b  = Flipped(Decoupled(new AXI4BIO))
  override val ar = Decoupled(new AXI4ARWIO)
  override val r  = Flipped(Decoupled(new AXI4RIO))
}
