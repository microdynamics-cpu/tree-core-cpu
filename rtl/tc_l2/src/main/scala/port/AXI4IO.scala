package treecorel2

import chisel3._
import chisel3.util._

class AXI4USERIO extends Bundle with AXI4Config {
  val valid: Bool = Input(Bool())
  val req:   UInt = Input(UInt(AxiReqLen.W))
  val wdata: UInt = Input(UInt(AxiDataWidth.W))
  val addr:  UInt = Input(UInt(AxiAddrWidth.W))
  val id:    UInt = Input(UInt(AxiIdLen.W))
  val size:  UInt = Input(UInt(AxiSizeLen.W))
  val ready: Bool = Output(Bool())
  val rdata: UInt = Output(UInt(AxiDataWidth.W))
  val resp:  UInt = Output(UInt(AxiRespLen.W))
}

class SOCAXI4ARWIO extends Bundle with AXI4Config {
  val addr:  UInt = Output(UInt(AxiAddrWidth.W))
  val id:    UInt = Output(UInt(AxiIdLen.W))
  val len:   UInt = Output(UInt(8.W))
  val size:  UInt = Output(UInt(3.W))
  val burst: UInt = Output(UInt(AxiBurstLen.W))
}

class AXI4ARWIO extends SOCAXI4ARWIO {
  val prot:  UInt = Output(UInt(AxiProtLen.W))
  val user:  UInt = Output(UInt(AxiUserLen.W))
  val lock:  Bool = Output(Bool())
  val cache: UInt = Output(UInt(AxiCacheLen.W))
  val qos:   UInt = Output(UInt(AxiQosLen.W))
}

class SOCAXI4WIO extends Bundle with AXI4Config {
  val data: UInt = Output(UInt(AxiDataWidth.W))
  val strb: UInt = Output(UInt((AxiDataWidth / 8).W))
  val last: Bool = Output(Bool())
}

class AXI4WIO extends SOCAXI4WIO {}

class SOCAXI4BIO extends Bundle with AXI4Config {
  val resp: UInt = Output(UInt(AxiRespLen.W))
  val id:   UInt = Output(UInt(AxiIdLen.W))
}

class AXI4BIO extends SOCAXI4BIO {
  val user: UInt = Output(UInt(AxiUserLen.W))
}

class SOCAXI4RIO extends Bundle with AXI4Config {
  val resp: UInt = Output(UInt(AxiRespLen.W))
  val data: UInt = Output(UInt(AxiDataWidth.W))
  val last: Bool = Output(Bool())
  val id:   UInt = Output(UInt(AxiIdLen.W))
}

//dont user in both sim
class AXI4RIO extends SOCAXI4RIO {
  val user: UInt = Output(UInt(AxiUserLen.W))
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
