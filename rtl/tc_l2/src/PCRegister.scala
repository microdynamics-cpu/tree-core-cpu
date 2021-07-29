package treecorel2

import chisel3._

class PCRegister extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instAddr: UInt = Output(UInt(BusWidth.W))
    val instEna:  Bool = Output(Bool())
  })

  private val pc: UInt = RegInit(0.U(BusWidth.W))
  pc          := pc + 4.U
  io.instAddr := pc
  io.instEna  := !this.reset.asBool()
}

// object PCRegister extends App {
//   (new chisel3.stage.ChiselStage).execute(
//     args,
//     Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new PCRegister()))
//   )
// }
