package treecorel2

import chisel3._

class AXI4USERIO extends Bundle with AXI4Config with InstConfig {
  val valid:  Bool = Input(Bool())
  val req:    UInt = Input(UInt(AxiReqLen.W)) // can only read
  val addr:   UInt = Input(UInt(AxiDataWidth.W))
  val size:   UInt = Input(UInt(AxiSizeLen.W))
  val ready:  Bool = Output(Bool())
  val rdData: UInt = Output(UInt(BusWidth.W))
  val resp:   UInt = Output(UInt(AxiRespLen.W))
}

class AXI4AWIO extends Bundle with AXI4Config with InstConfig {
// write addr
  val ready:  Bool = Input(Bool())
  val valid:  Bool = Output(Bool())
  val addr:   UInt = Output(UInt(AxiAddrWidth.W))
  val prot:   UInt = Output(UInt(AxiProtLen.W))
  val id:     UInt = Output(UInt(AxiIdLen.W))
  val user:   UInt = Output(UInt(AxiUserLen.W))
  val len:    UInt = Output(UInt(8.W))
  val size:   UInt = Output(UInt(3.W))
  val burst:  UInt = Output(UInt(2.W))
  val lock:   Bool = Output(Bool())
  val cache:  UInt = Output(UInt(4.W))
  val qos:    UInt = Output(UInt(4.W))
  val region: UInt = Output(UInt(4.W)) // not use
}

class AXI4WTIO extends Bundle with AXI4Config with InstConfig {
  // write data
  val ready: Bool = Input(Bool())
  val valid: Bool = Output(Bool())
  val data:  UInt = Output(UInt(AxiDataWidth.W))
  val strb:  UInt = Output(UInt((AxiDataWidth / 8).W))
  val last:  Bool = Output(Bool())
  val id:    UInt = Output(UInt(AxiIdLen.W))
  val user:  UInt = Output(UInt(AxiUserLen.W)) // not use
}

class AXI4WTBIO extends Bundle with AXI4Config with InstConfig {
  // write resp
  val valid: Bool = Input(Bool())
  val resp:  UInt = Input(UInt(AxiRespLen.W))
  val id:    UInt = Input(UInt(AxiIdLen.W))
  val user:  UInt = Input(UInt(AxiUserLen.W))
  val ready: Bool = Output(Bool())
}

class AXI4ARIO extends Bundle with AXI4Config with InstConfig {
  // read addr
  val ready:  Bool = Input(Bool())
  val valid:  Bool = Output(Bool())
  val addr:   UInt = Output(UInt(AxiAddrWidth.W))
  val prot:   UInt = Output(UInt(AxiProtLen.W))
  val id:     UInt = Output(UInt(AxiIdLen.W))
  val user:   UInt = Output(UInt(AxiUserLen.W))
  val len:    UInt = Output(UInt(8.W))
  val size:   UInt = Output(UInt(3.W))
  val burst:  UInt = Output(UInt(2.W))
  val lock:   Bool = Output(Bool())
  val cache:  UInt = Output(UInt(4.W))
  val qos:    UInt = Output(UInt(4.W))
  val region: UInt = Output(UInt(4.W)) // not use
}

class AXI4RDIO extends Bundle with AXI4Config with InstConfig {
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
  val awSign:  AXI4AWIO  = new AXI4AWIO
  val wtSign:  AXI4WTIO  = new AXI4WTIO
  val wtbSign: AXI4WTBIO = new AXI4WTBIO
  val arSign:  AXI4ARIO  = new AXI4ARIO
  val rdSign:  AXI4RDIO  = new AXI4RDIO
}
