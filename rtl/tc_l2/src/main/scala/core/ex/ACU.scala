// package treecorel2

// import chisel3._
// import chisel3.util._

// import treecorel2.ConstVal

// class AGU extends Module {
//   val io = IO(new Bundle {
//     val isa   = Input(new ISAIO)
//     val src1  = Input(UInt(ConstVal.AddrLen.W))
//     val src2  = Input(UInt(ConstVal.AddrLen.W))
//     val valid = Output(Bool())
//     val busy  = Output(Bool())
//     val res   = Output(UInt(ConstVal.AddrLen.W))
//   })

//   // cordic or gcd
//   // https://zhuanlan.zhihu.com/p/304477416
//   // https://zhuanlan.zhihu.com/p/365058686
//   protected val val1Reg = RegInit(0.U(64.W))
//   protected val val2Reg = RegInit(0.U(64.W))
//   protected val busyReg = RegInit(false.B)
//   protected val gcdVis  = false.B

//   when(gcdVis && !busyReg) {
//     val1Reg := io.src1
//     val2Reg := io.src2
//     busyReg := true.B
//   }.elsewhen(busyReg) {
//     when(val1Reg > val2Reg) {
//       val1Reg := val1Reg - val2Reg
//     }.otherwise {
//       val2Reg := val2Reg - val1Reg
//     }
//   }

//   when(val2Reg === 0.U(64.W)) { busyReg := false.B }
//   io.valid := (val2Reg === 0.U(64.W) && busyReg)
//   io.busy  := busyReg
//   io.res   := val1Reg
// }
