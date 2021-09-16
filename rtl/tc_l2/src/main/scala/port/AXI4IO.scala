package treecorel2

import chisel3._

class AXI4USERIO extends Bundle with AXI4Config {
  val valid: Bool = Input(Bool())
  val req:   UInt = Input(UInt(AxiReqLen.W))
  val wdata: UInt = Input(UInt(AxiDataWidth.W))
  val addr:  UInt = Input(UInt(AxiDataWidth.W))
  val id:    UInt = Input(UInt(AxiIdLen.W))
  val size:  UInt = Input(UInt(AxiSizeLen.W))
  val ready: Bool = Output(Bool())
  val rdata: UInt = Output(UInt(AxiDataWidth.W))
  val resp:  UInt = Output(UInt(AxiRespLen.W))
}

class AXI4AWIO extends Bundle with AXI4Config {
// write addr
  val ready:  Bool = Input(Bool())
  val valid:  Bool = Output(Bool())
  val addr:   UInt = Output(UInt(AxiAddrWidth.W))
  val prot:   UInt = Output(UInt(AxiProtLen.W))
  val id:     UInt = Output(UInt(AxiIdLen.W))
  val user:   UInt = Output(UInt(AxiUserLen.W))
  val len:    UInt = Output(UInt(8.W))
  val size:   UInt = Output(UInt(3.W))
  val burst:  UInt = Output(UInt(AxiBurstLen.W))
  val lock:   Bool = Output(Bool())
  val cache:  UInt = Output(UInt(AxiCacheLen.W))
  val qos:    UInt = Output(UInt(AxiQosLen.W))
  val region: UInt = Output(UInt(AxiRegionLen.W)) // not use
}

class AXI4WIO extends Bundle with AXI4Config {
  // write data
  val ready: Bool = Input(Bool())
  val valid: Bool = Output(Bool())
  val data:  UInt = Output(UInt(AxiDataWidth.W))
  val strb:  UInt = Output(UInt((AxiDataWidth / 8).W))
  val last:  Bool = Output(Bool())
  val id:    UInt = Output(UInt(AxiIdLen.W))
  val user:  UInt = Output(UInt(AxiUserLen.W)) // not use
}

class AXI4BIO extends Bundle with AXI4Config {
  // write resp
  val valid: Bool = Input(Bool())
  val resp:  UInt = Input(UInt(AxiRespLen.W))
  val id:    UInt = Input(UInt(AxiIdLen.W))
  val user:  UInt = Input(UInt(AxiUserLen.W))
  val ready: Bool = Output(Bool())
}

class AXI4ARIO extends Bundle with AXI4Config {
  // read addr
  val ready:  Bool = Input(Bool())
  val valid:  Bool = Output(Bool())
  val addr:   UInt = Output(UInt(AxiAddrWidth.W))
  val prot:   UInt = Output(UInt(AxiProtLen.W))
  val id:     UInt = Output(UInt(AxiIdLen.W))
  val user:   UInt = Output(UInt(AxiUserLen.W))
  val len:    UInt = Output(UInt(8.W))
  val size:   UInt = Output(UInt(3.W))
  val burst:  UInt = Output(UInt(AxiBurstLen.W))
  val lock:   Bool = Output(Bool())
  val cache:  UInt = Output(UInt(AxiCacheLen.W))
  val qos:    UInt = Output(UInt(AxiQosLen.W))
  val region: UInt = Output(UInt(AxiRegionLen.W)) // not use
}

class AXI4RIO extends Bundle with AXI4Config {
  // read data
  val valid: Bool = Input(Bool())
  val resp:  UInt = Input(UInt(AxiRespLen.W))
  val data:  UInt = Input(UInt(AxiDataWidth.W))
  val last:  Bool = Input(Bool())
  val id:    UInt = Input(UInt(AxiIdLen.W))
  val user:  UInt = Input(UInt(AxiUserLen.W))
  val ready: Bool = Output(Bool())
}

class AXI4IO extends Bundle {
  val aw: AXI4AWIO = new AXI4AWIO
  val w:  AXI4WIO  = new AXI4WIO
  val b:  AXI4BIO  = new AXI4BIO
  val ar: AXI4ARIO = new AXI4ARIO
  val r:  AXI4RIO  = new AXI4RIO
}
