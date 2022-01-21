package treecorel2

import chisel3._
import chisel3.util._

class SOCAXI4ARWIO extends Bundle with AXI4Config {
  val addr  = Output(UInt(32.W))
  val id    = Output(UInt(AxiIdLen.W))
  val len   = Output(UInt(AxiLen.W))
  val size  = Output(UInt(AxiSizeLen.W))
  val burst = Output(UInt(AxiBurstLen.W))
}

class AXI4ARWIO extends SOCAXI4ARWIO {
  override val addr = Output(UInt(XLen.W))
  val prot          = Output(UInt(AxiProtLen.W))
  val user          = Output(UInt(AxiUserLen.W))
  val lock          = Output(Bool())
  val cache         = Output(UInt(AxiCacheLen.W))
  val qos           = Output(UInt(AxiQosLen.W))
}

class SOCAXI4WIO extends Bundle with AXI4Config {
  val data = Output(UInt(XLen.W))
  val strb = Output(UInt(AxiStrb.W))
  val last = Output(Bool())
}

class AXI4WIO extends SOCAXI4WIO {}

class SOCAXI4BIO extends Bundle with AXI4Config {
  val resp = Output(UInt(AxiRespLen.W))
  val id   = Output(UInt(AxiIdLen.W))
}

class AXI4BIO extends SOCAXI4BIO {
  val user = Output(UInt(AxiUserLen.W))
}

class SOCAXI4RIO extends Bundle with AXI4Config {
  val resp = Output(UInt(AxiRespLen.W))
  val data = Output(UInt(XLen.W))
  val last = Output(Bool())
  val id   = Output(UInt(AxiIdLen.W))
}

class AXI4RIO extends SOCAXI4RIO {
  val user = Output(UInt(AxiUserLen.W))
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
