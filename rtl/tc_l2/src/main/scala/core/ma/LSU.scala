package treecorel2

import chisel3._
import chisel3.util._

class LSU extends Module {
  val io = IO(new Bundle {
    val valid    = Input(Bool())
    val isa      = Input(new ISAIO)
    val src1     = Input(UInt(64.W))
    val src2     = Input(UInt(64.W))
    val imm      = Input(new IMMIO)
    val ld       = new LDIO
    val sd       = new SDIO
    val loadData = Output(UInt(64.W))
  })

  protected val ldInstVis = io.isa.LD || io.isa.LW || io.isa.LH || io.isa.LB || io.isa.LWU || io.isa.LHU || io.isa.LBU
  protected val sdInstVis = io.isa.SD || io.isa.SW || io.isa.SH || io.isa.SB

  io.ld.en   := io.valid && ldInstVis
  io.ld.addr := io.src1 + io.imm.I
  protected val bSize    = 0.U(3.W)
  protected val hSize    = Mux(io.isa.LH || io.isa.LHU, 1.U, 0.U)
  protected val wSize    = Mux(io.isa.LW || io.isa.LWU, 2.U, 0.U)
  protected val dSize    = Mux(io.isa.LD, 3.U, 0.U)
  protected val loadSize = bSize | hSize | wSize | dSize
  io.ld.size := loadSize

  protected val dInstData = io.ld.data
  protected val wInstData = Mux(io.ld.addr(2).asBool(), dInstData(63, 32), dInstData(31, 0))
  protected val hTypeData = Mux(io.ld.addr(1).asBool(), wInstData(31, 16), wInstData(15, 0))
  protected val bTypeData = Mux(io.ld.addr(0).asBool(), hTypeData(15, 8), hTypeData(7, 0))

  protected val ldData  = SignExt(io.isa.LD.asUInt, 64) & dInstData
  protected val lwData  = SignExt(io.isa.LW.asUInt, 64) & (SignExt(wInstData, 64))
  protected val lhData  = SignExt(io.isa.LH.asUInt, 64) & (SignExt(hTypeData, 64))
  protected val lbData  = SignExt(io.isa.LB.asUInt, 64) & (SignExt(bTypeData, 64))
  protected val lwuData = SignExt(io.isa.LWU.asUInt, 64) & (ZeroExt(wInstData, 64))
  protected val lhuData = SignExt(io.isa.LHU.asUInt, 64) & (ZeroExt(hTypeData, 64))
  protected val lbuData = SignExt(io.isa.LBU.asUInt, 64) & (ZeroExt(bTypeData, 64))
  io.loadData := ldData | lwData | lhData | lbData | lwuData | lhuData | lbuData

  io.sd.en   := io.valid && sdInstVis
  io.sd.addr := io.src1 + io.imm.S
  protected val sdData    = SignExt(io.isa.SD.asUInt, 64) & io.src2
  protected val swData    = SignExt(io.isa.SW.asUInt, 64) & Cat(io.src2(31, 0), io.src2(31, 0))
  protected val shData    = SignExt(io.isa.SH.asUInt, 64) & Cat(io.src2(15, 0), io.src2(15, 0), io.src2(15, 0), io.src2(15, 0))
  protected val sbData    = SignExt(io.isa.SB.asUInt, 64) & Cat(io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0))
  protected val storeData = sdData | swData | shData | sbData
  protected val dInstMask = SignExt(io.isa.SD.asUInt, 8) & "b1111_1111".U(8.W)
  protected val wInstMask = SignExt(io.isa.SW.asUInt, 8) & ("b0000_1111".U(8.W) << io.sd.addr(2, 0))
  protected val hInstMask = SignExt(io.isa.SH.asUInt, 8) & ("b0000_0011".U(8.W) << io.sd.addr(2, 0))
  protected val bInstMask = SignExt(io.isa.SB.asUInt, 8) & ("b0000_0001".U(8.W) << io.sd.addr(2, 0))
  protected val sdMask    = dInstMask | wInstMask | hInstMask | bInstMask

  protected val sdMask0   = Mux(sdMask(0).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask1   = Mux(sdMask(1).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask2   = Mux(sdMask(2).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask3   = Mux(sdMask(3).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask4   = Mux(sdMask(4).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask5   = Mux(sdMask(5).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask6   = Mux(sdMask(6).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val sdMask7   = Mux(sdMask(7).asBool(), "hff".U(8.W), 0.U(8.W))
  protected val extenMask = Cat(sdMask7, sdMask6, sdMask5, sdMask4, sdMask3, sdMask2, sdMask1, sdMask0)

  io.sd.data := storeData & extenMask
  io.sd.mask := sdMask
}
