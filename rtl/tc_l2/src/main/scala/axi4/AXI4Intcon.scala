// package treecorel2

// import chisel3._

// class AXI4AWIO extends Bundle with InstConfig {
// // write addr
//   val ready:  Bool = Input(Bool())
//   val valid:  Bool = Output(Bool())
//   val addr:   UInt = Output(UInt(AxiAddrWidth.W))
//   val prot:   UInt = Output(UInt(AxiProtLen.W))
//   val id:     UInt = Output(UInt(AxiIdLen.W))
//   val user:   UInt = Output(UInt(AxiUserLen.W))
//   val len:    UInt = Output(UInt(8.W))
//   val size:   UInt = Output(UInt(3.W))
//   val burst:  UInt = Output(UInt(2.W))
//   val lock:   Bool = Output(Bool())
//   val cache:  UInt = Output(UInt(4.W))
//   val qos:    UInt = Output(UInt(4.W))
//   val region: UInt = Output(UInt(4.W)) // not use
// }

// class AXI4WTIO extends Bundle with InstConfig {
//   // write data
//   val ready: Bool = Input(Bool())
//   val valid: Bool = Output(Bool())
//   val data:  UInt = Output(UInt(AxiDataWidth.W))
//   val strb:  UInt = Output(UInt((AxiDataWidth / 8).W))
//   val last:  Bool = Output(Bool())
//   val id:    UInt = Output(UInt(AxiIdLen.W))
//   val user:  UInt = Output(UInt(AxiUserLen.W)) // not use
// }

// class AXI4WTBIO extends Bundle with InstConfig {
//   // write resp
//   val valid: Bool = Input(Bool())
//   val resp:  UInt = Input(UInt(AxiRespLen.W))
//   val id:     UInt = Input(UInt(AxiIdLen.W))
//   val user:  UInt = Input(UInt(AxiUserLen.W))
//   val ready: Bool = Output(Bool())
// }

// class AXI4ARIO extends Bundle with InstConfig {
//   // read addr
//   val ready:  Bool = Input(Bool())
//   val valid:  Bool = Output(Bool())
//   val addr:   UInt = Output(UInt(AxiAddrWidth.W))
//   val prot:   UInt = Output(UInt(AxiProtLen.W))
//   val id:     UInt = Output(UInt(AxiIdLen.W))
//   val user:   UInt = Output(UInt(AxiUserLen.W))
//   val len:    UInt = Output(UInt(8.W))
//   val size:   UInt = Output(UInt(3.W))
//   val burst:  UInt = Output(UInt(2.W))
//   val lock:   Bool = Output(Bool())
//   val cache:  UInt = Output(UInt(4.W))
//   val qos:    UInt = Output(UInt(4.W))
//   val region: UInt = Output(UInt(4.W)) // not use
// }

// class AXI4RDIO extends Bundle with InstConfig {
//   // read data
//   val valid: Bool = Input(Bool())
//   val resp:  UInt = Input(UInt(AxiRespLen.W))
//   val data:  UInt = Input(UInt(AxiDataWidth.W))
//   val last:  Bool = Input(Bool())
//   val id:    UInt = Input(UInt(AxiIdLen.W))
//   val user:  UInt = Input(UInt(AxiUserLen.W))
//   val ready: Bool = Output(Bool())
// }

// class AXI4IO extends Bundle {
//   val awSign: AXI4AWIO = new AXI4AWIO
//   val wtSign: AXI4WTIO = new AXI4WTIO
//   val wtbSign: AXI4WTBIO = new AXI4WTBIO
//   val arSign: AXI4ARIO = new AXI4ARIO
//   val rdSign: AXI4RDIO = new AXI4RDIO
// }

// class AXI4Intcon extends Module with InstConfig {
//   val io = IO(new Bundle {
//     val mstrIfIn: AXI4IO = new Flipped(AXI4IO)
//     val mstrIfReqIn: UInt = new Input(UInt(2.W))
//     val mstrMemIn: AXI4IO = new Flipped(AXI4IO)
//     val mstrMemReqIn: UInt = new Input(UInt(2.W))

//     val slvOut: AXI4IO = new AXI4IO
//   })

//   // below are write req
//   // only if and mem req trigger meantime, and both req are rd
//   // mem rd has high prority
//   // ---> send to control unit, mean inst oper need to delay one cycle
//   // 0: rd 1: wt 2 nop
//   // need to use fsm to desc below oper!!!!!!!!!
//   when(io.mstrIfReqIn === 0.U && io.mstrMemReqIn === 0.U) {
//     io.slvOut.arSign <> io.mstrMemIn.arSign
//   }.elsewhen(io.mstrIfReqIn === 0.U && io.mstrMemReqIn === 1.U){
//       io.slvOut.arSign <> io.mstrIfIn.arSign
//       io.slvOut.awSign <> io.mstrMemIn.awSign

//   }.otherwise {
//       io.slvOut <> zero
//   }

//   // all master have same rd req oper accroding to the id
//   when(io.slvOut.rdSign.id === 0.U) {
//       io.mstrIfIn.rdSign <> io.slvOut.rdSign
//   }.otherwise {
//       io.mstrMemIn.rdSign <> io.slvOut.rdSign
//   }

// }
