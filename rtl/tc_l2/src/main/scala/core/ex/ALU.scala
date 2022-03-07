package treecorel2

import chisel3._
import chisel3.util._

class ALU extends Module with InstConfig {
  val io = IO(new Bundle {
    val isa  = Input(UInt(InstValLen.W))
    val src1 = Input(UInt(XLen.W))
    val src2 = Input(UInt(XLen.W))
    val imm  = Input(UInt(XLen.W))
    val res  = Output(UInt(XLen.W))
  })

  io.res := MuxLookup(
    io.isa,
    0.U(XLen.W),
    Seq(
      instADDI  -> (io.src1 + io.imm),
      instADD   -> (io.src1 + io.src2),
      instLUI   -> (io.imm),
      instSUB   -> (io.src1 - io.src2),
      instADDIW -> SignExt((io.src1 + io.imm)(31, 0), 64),
      instADDW  -> SignExt((io.src1 + io.src2)(31, 0), 64),
      instSUBW  -> SignExt((io.src1 - io.src2)(31, 0), 64),
      instANDI  -> (io.src1 & io.imm),
      instAND   -> (io.src1 & io.src2),
      instORI   -> (io.src1 | io.imm),
      instOR    -> (io.src1 | io.src2),
      instXORI  -> (io.src1 ^ io.imm),
      instXOR   -> (io.src1 ^ io.src2),
      instSLT   -> Mux(io.src1.asSInt < io.src2.asSInt, 1.U(XLen.W), 0.U(XLen.W)),
      instSLTI  -> Mux(io.src1.asSInt < io.imm.asSInt, 1.U(XLen.W), 0.U(XLen.W)),
      instSLTU  -> Mux(io.src1.asUInt < io.src2.asUInt, 1.U(XLen.W), 0.U(XLen.W)),
      instSLTIU -> Mux(io.src1.asUInt < io.imm.asUInt, 1.U(XLen.W), 0.U(XLen.W)),
      instSLL   -> (io.src1 << io.src2(5, 0))(63, 0),
      instSRL   -> (io.src1 >> io.src2(5, 0)),
      instSRA   -> (io.src1.asSInt >> io.src2(5, 0)).asUInt,
      instSLLI  -> (io.src1 << io.imm(5, 0))(63, 0),
      instSRLI  -> (io.src1 >> io.imm(5, 0)),
      instSRAI  -> (io.src1.asSInt >> io.imm(5, 0)).asUInt,
      instSLLW  -> SignExt((io.src1 << io.src2(4, 0))(31, 0), 64),
      instSRLW  -> SignExt((io.src1(31, 0) >> io.src2(4, 0)), 64),
      instSRAW  -> SignExt((io.src1(31, 0).asSInt >> io.src2(4, 0)).asUInt, 64),
      instSLLIW -> SignExt((io.src1 << io.imm(4, 0))(31, 0), 64),
      instSRLIW -> SignExt((io.src1(31, 0) >> io.imm(4, 0)), 64),
      instSRAIW -> SignExt((io.src1(31, 0).asSInt >> io.imm(4, 0)).asUInt, 64)
    )
  )
}
