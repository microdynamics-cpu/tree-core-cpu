package treecorel2

import chisel3._
import chisel3.util._

class BEU extends Module with InstConfig {
  val io = IO(new Bundle {
    val isa        = Input(UInt(InstValLen.W))
    val imm        = Input(UInt(XLen.W))
    val src1       = Input(UInt(XLen.W))
    val src2       = Input(UInt(XLen.W))
    val pc         = Input(UInt(XLen.W))
    val branIdx    = Input(UInt(GHRLen.W))
    val branchInfo = new BRANCHIO
    val branch     = Output(Bool())
    val tgt        = Output(UInt(XLen.W))
  })

  protected val b = MuxLookup(
    io.isa,
    false.B,
    Seq(
      instBEQ  -> (io.src1 === io.src2),
      instBNE  -> (io.src1 =/= io.src2),
      instBGEU -> (io.src1 >= io.src2),
      instBLTU -> (io.src1 < io.src2),
      instBGE  -> (io.src1.asSInt >= io.src2.asSInt),
      instBLT  -> (io.src1.asSInt < io.src2.asSInt)
    )
  )

  protected val bInst = MuxLookup(
    io.isa,
    false.B,
    Seq(
      instBEQ  -> true.B,
      instBNE  -> true.B,
      instBGEU -> true.B,
      instBLTU -> true.B,
      instBGE  -> true.B,
      instBLT  -> true.B
    )
  )

  protected val jal  = io.isa === instJAL
  protected val jalr = io.isa === instJALR
  io.branch := b | jal | jalr

  io.tgt := MuxLookup(
    io.isa,
    (io.pc + io.imm), // NOTE: branch target
    Seq(
      instJAL  -> (io.pc + io.imm),
      instJALR -> (io.src1 + io.imm)
    )
  )

  io.branchInfo.branch := bInst
  io.branchInfo.jump   := jal || jalr
  io.branchInfo.taken  := io.branch
  io.branchInfo.idx    := io.branIdx
  io.branchInfo.pc     := io.pc
  io.branchInfo.tgt    := io.tgt
}
