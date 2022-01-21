package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.InstConfig

class LSU extends Module with InstConfig {
  val io = IO(new Bundle {
    val valid  = Input(Bool())
    val isa    = Input(UInt(InstValLen.W))
    val src1   = Input(UInt(XLen.W))
    val src2   = Input(UInt(XLen.W))
    val imm    = Input(UInt(XLen.W))
    val ld     = new LDIO
    val sd     = new SDIO
    val ldData = Output(UInt(XLen.W))
  })

  protected val ldInstVis = (io.isa === instLD) || (io.isa === instLW) || (io.isa === instLH) || (io.isa === instLB) || (io.isa === instLWU) || (io.isa === instLHU) || (io.isa === instLBU)
  protected val sdInstVis = (io.isa === instSD) || (io.isa === instSW) || (io.isa === instSH) || (io.isa === instSB)

  io.ld.en   := io.valid && ldInstVis
  io.ld.addr := io.src1 + io.imm

  protected val ldSize = MuxLookup(
    io.isa,
    0.U(3.W),
    Seq(
      instLH  -> 1.U(3.W),
      instLHU -> 1.U(3.W),
      instLW  -> 2.U(3.W),
      instLWU -> 2.U(3.W),
      instLD  -> 3.U(3.W)
    )
  )
  io.ld.size := ldSize

  protected val dInstData = io.ld.data
  protected val wInstData = Mux(io.ld.addr(2).asBool(), dInstData(63, 32), dInstData(31, 0))
  protected val hTypeData = Mux(io.ld.addr(1).asBool(), wInstData(31, 16), wInstData(15, 0))
  protected val bTypeData = Mux(io.ld.addr(0).asBool(), hTypeData(15, 8), hTypeData(7, 0))

  io.ldData := MuxLookup(
    io.isa,
    0.U(XLen.W),
    Seq(
      instLD  -> dInstData,
      instLW  -> (SignExt(wInstData, XLen)),
      instLH  -> (SignExt(hTypeData, XLen)),
      instLB  -> (SignExt(bTypeData, XLen)),
      instLWU -> (ZeroExt(wInstData, XLen)),
      instLHU -> (ZeroExt(hTypeData, XLen)),
      instLBU -> (ZeroExt(bTypeData, XLen))
    )
  )

  // store signals
  io.sd.en   := io.valid && sdInstVis
  io.sd.addr := io.src1 + io.imm
  protected val storeData = MuxLookup(
    io.isa,
    0.U(XLen.W),
    Seq(
      instSD -> io.src2,
      instSW -> Cat(io.src2(31, 0), io.src2(31, 0)),
      instSH -> Cat(io.src2(15, 0), io.src2(15, 0), io.src2(15, 0), io.src2(15, 0)),
      instSB -> Cat(io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0), io.src2(7, 0))
    )
  )

  protected val sdMask = MuxLookup(
    io.isa,
    0.U(8.W), // NOTE: important!!!
    Seq(
      instSD -> "b1111_1111".U(8.W),
      instSW -> ("b0000_1111".U(8.W) << io.sd.addr(2, 0)),
      instSH -> ("b0000_0011".U(8.W) << io.sd.addr(2, 0)),
      instSB -> ("b0000_0001".U(8.W) << io.sd.addr(2, 0))
    )
  )

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
