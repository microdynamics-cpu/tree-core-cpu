package treecorel2

import chisel3._
import chisel3.util._

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
    0.U(LDSize.W),
    Seq(
      instLH  -> 1.U(LDSize.W),
      instLHU -> 1.U(LDSize.W),
      instLW  -> 2.U(LDSize.W),
      instLWU -> 2.U(LDSize.W),
      instLD  -> 3.U(LDSize.W)
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

  val sdMask = MuxLookup(
    io.isa,
    0.U(MaskLen.W), // NOTE: important!!!
    Seq(
      instSD -> "b1111_1111".U(MaskLen.W),
      instSW -> ("b0000_1111".U(MaskLen.W) << io.sd.addr(2, 0)),
      instSH -> ("b0000_0011".U(MaskLen.W) << io.sd.addr(2, 0)),
      instSB -> ("b0000_0001".U(MaskLen.W) << io.sd.addr(2, 0))
    )
  )

  val tmpMask = Wire(Vec(8, UInt((XLen / 8).W)))
  for (i <- 0 until 8) {
    tmpMask(i) := Mux(sdMask(i).asBool(), "hff".U((XLen / 8).W), 0.U((XLen / 8).W))
  }
  protected val extenMask = tmpMask.asUInt()

  io.sd.data := storeData & extenMask
  io.sd.mask := sdMask
}
