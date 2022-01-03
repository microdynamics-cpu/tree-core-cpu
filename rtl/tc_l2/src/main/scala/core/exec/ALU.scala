package treecorel2

import chisel3._
import chisel3.util._

class ALU extends Module {
  val io = IO(new Bundle {
    val isa  = Input(new ISAIO)
    val src1 = Input(UInt(64.W))
    val src2 = Input(UInt(64.W))
    val imm  = Input(new IMMIO)
    val res  = Output(UInt(64.W))
  })

  protected val addi  = SignExt(io.isa.ADDI.asUInt, 64) & (io.src1 + io.imm.I)
  protected val add   = SignExt(io.isa.ADD.asUInt, 64) & (io.src1 + io.src2)
  protected val lui   = SignExt(io.isa.LUI.asUInt, 64) & (io.imm.U)
  protected val sub   = SignExt(io.isa.SUB.asUInt, 64) & (io.src1 - io.src2)
  protected val addiw = SignExt(io.isa.ADDIW.asUInt, 64) & SignExt((io.src1 + io.imm.I)(31, 0), 64)
  protected val addw  = SignExt(io.isa.ADDW.asUInt, 64) & SignExt((io.src1 + io.src2)(31, 0), 64)
  protected val subw  = SignExt(io.isa.SUBW.asUInt, 64) & SignExt((io.src1 - io.src2)(31, 0), 64)
  protected val arith = addi | add | lui | sub | addiw | addw | subw

  protected val andi = SignExt(io.isa.ANDI.asUInt, 64) & (io.src1 & io.imm.I)
  protected val and  = SignExt(io.isa.AND.asUInt, 64) & (io.src1 & io.src2)
  protected val ori  = SignExt(io.isa.ORI.asUInt, 64) & (io.src1 | io.imm.I)
  protected val or   = SignExt(io.isa.OR.asUInt, 64) & (io.src1 | io.src2)
  protected val xori = SignExt(io.isa.XORI.asUInt, 64) & (io.src1 ^ io.imm.I)
  protected val xor  = SignExt(io.isa.XOR.asUInt, 64) & (io.src1 ^ io.src2)
  protected val logc = andi | and | ori | or | xori | xor

  protected val slt   = Mux((io.isa.SLT && (io.src1.asSInt < io.src2.asSInt)), 1.U(64.W), 0.U(64.W))
  protected val slti  = Mux((io.isa.SLTI && (io.src1.asSInt < io.imm.I.asSInt)), 1.U(64.W), 0.U(64.W))
  protected val sltu  = Mux((io.isa.SLTU && (io.src1.asUInt < io.src2.asUInt)), 1.U(64.W), 0.U(64.W))
  protected val sltiu = Mux((io.isa.SLTIU && (io.src1.asUInt < io.imm.I.asUInt)), 1.U(64.W), 0.U(64.W))
  protected val comp  = slt | slti | sltu | sltiu

  protected val sll   = SignExt(io.isa.SLL.asUInt, 64) & (io.src1 << io.src2(5, 0))(63, 0)
  protected val srl   = SignExt(io.isa.SRL.asUInt, 64) & (io.src1 >> io.src2(5, 0))
  protected val sra   = SignExt(io.isa.SRA.asUInt, 64) & (io.src1.asSInt >> io.src2(5, 0)).asUInt
  protected val slli  = SignExt(io.isa.SLLI.asUInt, 64) & (io.src1 << io.imm.I(5, 0))(63, 0)
  protected val srli  = SignExt(io.isa.SRLI.asUInt, 64) & (io.src1 >> io.imm.I(5, 0))
  protected val srai  = SignExt(io.isa.SRAI.asUInt, 64) & (io.src1.asSInt >> io.imm.I(5, 0)).asUInt
  protected val sllw  = SignExt(io.isa.SLLW.asUInt, 64) & SignExt((io.src1 << io.src2(4, 0))(31, 0), 64)
  protected val srlw  = SignExt(io.isa.SRLW.asUInt, 64) & SignExt((io.src1(31, 0) >> io.src2(4, 0)), 64)
  protected val sraw  = SignExt(io.isa.SRAW.asUInt, 64) & SignExt((io.src1(31, 0).asSInt >> io.src2(4, 0)).asUInt, 64)
  protected val slliw = SignExt(io.isa.SLLIW.asUInt, 64) & SignExt((io.src1 << io.imm.I(4, 0))(31, 0), 64)
  protected val srliw = SignExt(io.isa.SRLIW.asUInt, 64) & SignExt((io.src1(31, 0) >> io.imm.I(4, 0)), 64)
  protected val sraiw = SignExt(io.isa.SRAIW.asUInt, 64) & SignExt((io.src1(31, 0).asSInt >> io.imm.I(4, 0)).asUInt, 64)
  protected val shift = sll | srl | sra | slli | srli | srai | sllw | srlw | sraw | slliw | srliw | sraiw

  io.res := arith | logc | comp | shift
}
