// package treecorel2

// import chisel3._
// import chisel3.util._

// class CLINT extends Module with InstConfig {
//   val io = IO(new Bundle {
//     val addr:  UInt = Input(UInt(BusWidth.W))
//     val rdata: UInt = Output(UInt(BusWidth.W))
//     val wtena: Bool = Input(Bool())
//     val wdata: UInt = Input(UInt(BusWidth.W))
//     val mtip:  Bool = Output(Bool())
//     val msip:  Bool = Output(Bool())
//   })
//   protected val mtime:    UInt = RegInit(0.U(BusWidth.W))
//   protected val mtimecmp: UInt = RegInit(0.U(BusWidth.W))
//   protected val msip:     UInt = RegInit(0.U(BusWidth.W))
//   protected val (tickCnt, cntWrap) = Counter(this.clock.asBool(), TickCnt)

//   msip     := Mux((io.addr === MSipOffset) && io.wtena, io.wdata, msip)
//   mtime    := Mux((io.addr === MTimeOffset) && io.wtena, io.wdata, Mux(cntWrap, mtime + 1.U, mtime))
//   mtimecmp := Mux((io.addr === MTimeCmpOffset) && io.wtena, io.wdata, mtimecmp)
//   io.rdata := MuxLookup(
//     io.addr,
//     0.U,
//     Array(
//       MSipOffset     -> msip,
//       MTimeOffset    -> mtime,
//       MTimeCmpOffset -> mtimecmp
//     )
//   )
//   io.mtip := RegNext((mtime >= mtimecmp) && (mtimecmp =/= 0.U))
//   io.msip := RegNext(msip =/= 0.U)
// }
