package treecorel2

import chisel3._
import chisel3.util._

import treecorel2.common.InstConfig

object ISADecoder {
  // according to the The RISC-V Instruction Set Manual Volume I: Unprivileged ISA
  // Document Version: 20191213
  // DESC page [15-25] ABI page[130-131]

  /* Integer Register-Immediate Instructions */
  // I type inst
  def ADDI  = BitPat("b?????????????????000?????0010011")
  def ADDIW = BitPat("b?????????????????000?????0011011")
  def SLTI  = BitPat("b?????????????????010?????0010011")
  def SLTIU = BitPat("b?????????????????011?????0010011")

  def ANDI = BitPat("b?????????????????111?????0010011")
  def ORI  = BitPat("b?????????????????110?????0010011")
  def XORI = BitPat("b?????????????????100?????0010011")

  // special I type inst
  def SLLI  = BitPat("b000000???????????001?????0010011")
  def SLLIW = BitPat("b0000000??????????001?????0011011")
  def SRLI  = BitPat("b000000???????????101?????0010011")
  def SRLIW = BitPat("b0000000??????????101?????0011011")
  def SRAI  = BitPat("b010000???????????101?????0010011")
  def SRAIW = BitPat("b0100000??????????101?????0011011")
  // U type inst
  // LUI - rd = {imm, 12b'0}
  def LUI = BitPat("b?????????????????????????0110111")
  // AUIPC - rd = PC + {imm, 12b'0}
  def AUIPC = BitPat("b?????????????????????????0010111")

  /* Integer Register-Register Operations */
  // R type inst
  def ADD  = BitPat("b0000000??????????000?????0110011")
  def ADDW = BitPat("b0000000??????????000?????0111011")
  def SLT  = BitPat("b0000000??????????010?????0110011")
  def SLTU = BitPat("b0000000??????????011?????0110011")

  def AND = BitPat("b0000000??????????111?????0110011")
  def OR  = BitPat("b0000000??????????110?????0110011")
  def XOR = BitPat("b0000000??????????100?????0110011")

  def SLL  = BitPat("b0000000??????????001?????0110011")
  def SLLW = BitPat("b0000000??????????001?????0111011")
  def SRL  = BitPat("b0000000??????????101?????0110011")
  def SRLW = BitPat("b0000000??????????101?????0111011")

  def SUB  = BitPat("b0100000??????????000?????0110011")
  def SUBW = BitPat("b0100000??????????000?????0111011")
  def SRA  = BitPat("b0100000??????????101?????0110011")
  def SRAW = BitPat("b0100000??????????101?????0111011")

  // NOP - ADDI x0, 0x00(x0)
  def NOP = BitPat("b00000000000000000000000000010011")

  /* Control Transfer Instructions */
  // J type inst
  def JAL = BitPat("b?????????????????????????1101111")
  // I type inst
  def JALR = BitPat("b?????????????????000?????1100111")
  // B type inst
  def BEQ  = BitPat("b?????????????????000?????1100011")
  def BNE  = BitPat("b?????????????????001?????1100011")
  def BLT  = BitPat("b?????????????????100?????1100011")
  def BLTU = BitPat("b?????????????????110?????1100011")
  def BGE  = BitPat("b?????????????????101?????1100011")
  def BGEU = BitPat("b?????????????????111?????1100011")

  /* Load and Store Instructions */
  // I type inst
  def LB  = BitPat("b?????????????????000?????0000011")
  def LBU = BitPat("b?????????????????100?????0000011")
  def LH  = BitPat("b?????????????????001?????0000011")
  def LHU = BitPat("b?????????????????101?????0000011")
  def LW  = BitPat("b?????????????????010?????0000011")
  def LWU = BitPat("b?????????????????110?????0000011")
  def LD  = BitPat("b?????????????????011?????0000011")

  // S type inst
  def SB = BitPat("b?????????????????000?????0100011")
  def SH = BitPat("b?????????????????001?????0100011")
  def SW = BitPat("b?????????????????010?????0100011")
  def SD = BitPat("b?????????????????011?????0100011")

  // CSR inst
  def CSRRW  = BitPat("b?????????????????001?????1110011")
  def CSRRS  = BitPat("b?????????????????010?????1110011")
  def CSRRC  = BitPat("b?????????????????011?????1110011")
  def CSRRWI = BitPat("b?????????????????101?????1110011")
  def CSRRSI = BitPat("b?????????????????110?????1110011")
  def CSRRCI = BitPat("b?????????????????111?????1110011")

  // system inst
  def ECALL      = BitPat("b00000000000000000000000001110011")
  def EBREAK     = BitPat("b00000000000100000000000001110011")
  def URET       = BitPat("b00000000001000000000000001110011")
  def SRET       = BitPat("b00010000001000000000000001110011")
  def MRET       = BitPat("b00110000001000000000000001110011")
  def WFI        = BitPat("b00010000010100000000000001110011")
  def SFENCE_VMA = BitPat("b0001001??????????000000001110011")
  def FENCE      = BitPat("b0000????????00000000000000001111")
  def FENCE_I    = BitPat("b00000000000000000001000000001111")
  // custom inst such as 0x7B
  def CUST = BitPat("b0000000??????????000?????1111011")
}

class ISADecoder extends Module with InstConfig {
  val io = IO(new Bundle {
    val inst = Input(UInt(InstLen.W))
    val isa  = Output(new ISAIO)
    val imm  = Output(UInt(XLen.W))
    val csr  = Output(new Bool())
    val wen  = Output(new Bool())
  })

  io.isa.SLLI       := (io.inst === ISADecoder.SLLI)
  io.isa.SLLIW      := (io.inst === ISADecoder.SLLIW)
  io.isa.SRLI       := (io.inst === ISADecoder.SRLI)
  io.isa.SRLIW      := (io.inst === ISADecoder.SRLIW)
  io.isa.SRAI       := (io.inst === ISADecoder.SRAI)
  io.isa.SRAIW      := (io.inst === ISADecoder.SRAIW)
  io.isa.ADDI       := (io.inst === ISADecoder.ADDI)
  io.isa.ADDIW      := (io.inst === ISADecoder.ADDIW)
  io.isa.XORI       := (io.inst === ISADecoder.XORI)
  io.isa.ORI        := (io.inst === ISADecoder.ORI)
  io.isa.ANDI       := (io.inst === ISADecoder.ANDI)
  io.isa.SLTI       := (io.inst === ISADecoder.SLTI)
  io.isa.SLTIU      := (io.inst === ISADecoder.SLTIU)
  io.isa.JALR       := (io.inst === ISADecoder.JALR)
  io.isa.FENCE      := (io.inst === ISADecoder.FENCE)
  io.isa.FENCE_I    := (io.inst === ISADecoder.FENCE_I)
  io.isa.ECALL      := (io.inst === ISADecoder.ECALL)
  io.isa.EBREAK     := (io.inst === ISADecoder.EBREAK)
  io.isa.CSRRW      := (io.inst === ISADecoder.CSRRW)
  io.isa.CSRRWI     := (io.inst === ISADecoder.CSRRWI)
  io.isa.CSRRS      := (io.inst === ISADecoder.CSRRS)
  io.isa.CSRRSI     := (io.inst === ISADecoder.CSRRSI)
  io.isa.CSRRC      := (io.inst === ISADecoder.CSRRC)
  io.isa.CSRRCI     := (io.inst === ISADecoder.CSRRCI)
  io.isa.LD         := (io.inst === ISADecoder.LD)
  io.isa.LW         := (io.inst === ISADecoder.LW)
  io.isa.LWU        := (io.inst === ISADecoder.LWU)
  io.isa.LH         := (io.inst === ISADecoder.LH)
  io.isa.LHU        := (io.inst === ISADecoder.LHU)
  io.isa.LB         := (io.inst === ISADecoder.LB)
  io.isa.LBU        := (io.inst === ISADecoder.LBU)
  io.isa.SLL        := (io.inst === ISADecoder.SLL)
  io.isa.SLLW       := (io.inst === ISADecoder.SLLW)
  io.isa.SRL        := (io.inst === ISADecoder.SRL)
  io.isa.SRLW       := (io.inst === ISADecoder.SRLW)
  io.isa.SRA        := (io.inst === ISADecoder.SRA)
  io.isa.SRAW       := (io.inst === ISADecoder.SRAW)
  io.isa.ADD        := (io.inst === ISADecoder.ADD)
  io.isa.ADDW       := (io.inst === ISADecoder.ADDW)
  io.isa.SUB        := (io.inst === ISADecoder.SUB)
  io.isa.SUBW       := (io.inst === ISADecoder.SUBW)
  io.isa.XOR        := (io.inst === ISADecoder.XOR)
  io.isa.OR         := (io.inst === ISADecoder.OR)
  io.isa.AND        := (io.inst === ISADecoder.AND)
  io.isa.SLT        := (io.inst === ISADecoder.SLT)
  io.isa.SLTU       := (io.inst === ISADecoder.SLTU)
  io.isa.MRET       := (io.inst === ISADecoder.MRET)
  io.isa.SRET       := (io.inst === ISADecoder.SRET)
  io.isa.WFI        := (io.inst === ISADecoder.WFI)
  io.isa.SFENCE_VMA := (io.inst === ISADecoder.SFENCE_VMA)
  io.isa.BEQ        := (io.inst === ISADecoder.BEQ)
  io.isa.BNE        := (io.inst === ISADecoder.BNE)
  io.isa.BLT        := (io.inst === ISADecoder.BLT)
  io.isa.BGE        := (io.inst === ISADecoder.BGE)
  io.isa.BLTU       := (io.inst === ISADecoder.BLTU)
  io.isa.BGEU       := (io.inst === ISADecoder.BGEU)
  io.isa.SD         := (io.inst === ISADecoder.SD)
  io.isa.SW         := (io.inst === ISADecoder.SW)
  io.isa.SH         := (io.inst === ISADecoder.SH)
  io.isa.SB         := (io.inst === ISADecoder.SB)
  io.isa.LUI        := (io.inst === ISADecoder.LUI)
  io.isa.AUIPC      := (io.inst === ISADecoder.AUIPC)
  io.isa.JAL        := (io.inst === ISADecoder.JAL)

  io.isa.MUL    := (io.inst === BitPat("b0000001_?????_?????_000_?????_0110011"))
  io.isa.MULH   := (io.inst === BitPat("b0000001_?????_?????_001_?????_0110011"))
  io.isa.MULHSU := (io.inst === BitPat("b0000001_?????_?????_010_?????_0110011"))
  io.isa.MULHU  := (io.inst === BitPat("b0000001_?????_?????_011_?????_0110011"))
  io.isa.MULW   := (io.inst === BitPat("b0000001_?????_?????_000_?????_0111011"))
  io.isa.DIV    := (io.inst === BitPat("b0000001_?????_?????_100_?????_0110011"))
  io.isa.DIVU   := (io.inst === BitPat("b0000001_?????_?????_101_?????_0110011"))
  io.isa.DIVUW  := (io.inst === BitPat("b0000001_?????_?????_101_?????_0111011"))
  io.isa.DIVW   := (io.inst === BitPat("b0000001_?????_?????_100_?????_0111011"))
  io.isa.REM    := (io.inst === BitPat("b0000001_?????_?????_110_?????_0110011"))
  io.isa.REMU   := (io.inst === BitPat("b0000001_?????_?????_111_?????_0110011"))
  io.isa.REMUW  := (io.inst === BitPat("b0000001_?????_?????_111_?????_0111011"))
  io.isa.REMW   := (io.inst === BitPat("b0000001_?????_?????_110_?????_0111011"))
  io.isa.GCD    := (io.inst === BitPat("b0000000_?????_?????_000_?????_0001000"))

  protected val arith  = io.isa.ADD || io.isa.ADDW || io.isa.ADDI || io.isa.ADDIW || io.isa.SUB || io.isa.SUBW || io.isa.LUI || io.isa.AUIPC
  protected val logc   = io.isa.XOR || io.isa.XORI || io.isa.OR || io.isa.ORI || io.isa.AND || io.isa.ANDI
  protected val shift  = io.isa.SLL || io.isa.SLLI || io.isa.SLLW || io.isa.SLLIW || io.isa.SRL || io.isa.SRLI || io.isa.SRLW || io.isa.SRLIW || io.isa.SRA || io.isa.SRAI || io.isa.SRAW || io.isa.SRAIW
  protected val comp   = io.isa.SLT || io.isa.SLTI || io.isa.SLTU || io.isa.SLTIU
  protected val link   = io.isa.JAL || io.isa.JALR
  protected val branch = io.isa.BEQ || io.isa.BNE || io.isa.BLT || io.isa.BGE || io.isa.BLTU || io.isa.BGEU
  protected val load   = io.isa.LD || io.isa.LW || io.isa.LH || io.isa.LB || io.isa.LWU || io.isa.LHU || io.isa.LBU
  protected val store  = io.isa.SD || io.isa.SW || io.isa.SH || io.isa.SB
  protected val sync   = io.isa.FENCE || io.isa.FENCE_I
  protected val env    = io.isa.ECALL || io.isa.EBREAK
  protected val csr    = io.isa.CSRRW || io.isa.CSRRS || io.isa.CSRRC || io.isa.CSRRWI || io.isa.CSRRSI || io.isa.CSRRCI
  protected val priv   = io.isa.MRET || io.isa.SRET || io.isa.WFI || io.isa.SFENCE_VMA
  protected val custom = io.isa.GCD

  protected val decodeTable = Array(
    // i type inst
    ISADecoder.ADDI  -> List(iInstType),
    ISADecoder.ADDIW -> List(iInstType),
    ISADecoder.SLTI  -> List(iInstType),
    ISADecoder.SLTIU -> List(iInstType),
    ISADecoder.ANDI  -> List(iInstType),
    ISADecoder.ORI   -> List(iInstType),
    ISADecoder.XORI  -> List(iInstType),
    ISADecoder.SLLI  -> List(iInstType),
    ISADecoder.SLLIW -> List(iInstType),
    ISADecoder.SRLI  -> List(iInstType),
    ISADecoder.SRLIW -> List(iInstType),
    ISADecoder.SRAI  -> List(iInstType),
    ISADecoder.SRAIW -> List(iInstType),
    // u type inst
    ISADecoder.LUI   -> List(uInstType),
    ISADecoder.AUIPC -> List(uInstType),
    // r type inst
    ISADecoder.ADD  -> List(rInstType),
    ISADecoder.ADDW -> List(rInstType),
    ISADecoder.SLT  -> List(rInstType),
    ISADecoder.SLTU -> List(rInstType),
    ISADecoder.AND  -> List(rInstType),
    ISADecoder.OR   -> List(rInstType),
    ISADecoder.XOR  -> List(rInstType),
    ISADecoder.SLL  -> List(rInstType),
    ISADecoder.SLLW -> List(rInstType),
    ISADecoder.SRL  -> List(rInstType),
    ISADecoder.SRLW -> List(rInstType),
    ISADecoder.SUB  -> List(rInstType),
    ISADecoder.SUBW -> List(rInstType),
    ISADecoder.SRA  -> List(rInstType),
    ISADecoder.SRAW -> List(rInstType),
    // nop inst
    ISADecoder.NOP -> List(nopInstType),
    // j type inst
    ISADecoder.JAL  -> List(jInstType),
    ISADecoder.JALR -> List(iInstType),
    // b type inst
    ISADecoder.BEQ  -> List(bInstType),
    ISADecoder.BNE  -> List(bInstType),
    ISADecoder.BLT  -> List(bInstType),
    ISADecoder.BLTU -> List(bInstType),
    ISADecoder.BGE  -> List(bInstType),
    ISADecoder.BGEU -> List(bInstType),
    // special i type inst
    ISADecoder.LB  -> List(iInstType),
    ISADecoder.LBU -> List(iInstType),
    ISADecoder.LH  -> List(iInstType),
    ISADecoder.LHU -> List(iInstType),
    ISADecoder.LW  -> List(iInstType),
    ISADecoder.LWU -> List(iInstType),
    ISADecoder.LD  -> List(iInstType),
    // s type inst
    ISADecoder.SB -> List(sInstType),
    ISADecoder.SH -> List(sInstType),
    ISADecoder.SW -> List(sInstType),
    ISADecoder.SD -> List(sInstType),
    // csr inst
    ISADecoder.CSRRW  -> List(iInstType),
    ISADecoder.CSRRS  -> List(iInstType),
    ISADecoder.CSRRC  -> List(iInstType),
    ISADecoder.CSRRWI -> List(iInstType),
    ISADecoder.CSRRSI -> List(iInstType),
    ISADecoder.CSRRCI -> List(iInstType),
    // system inst
    ISADecoder.ECALL   -> List(nopInstType),
    ISADecoder.MRET    -> List(nopInstType),
    ISADecoder.FENCE   -> List(nopInstType),
    ISADecoder.FENCE_I -> List(nopInstType),
    // custom inst
    ISADecoder.CUST -> List(nopInstType)
  )

  protected val immExten = Module(new ImmExten)
  protected val defRes   = List(nopInstType)

  immExten.io.inst     := io.inst
  immExten.io.instType := ListLookup(io.inst, defRes, decodeTable)(0)
  io.imm               := immExten.io.imm
  io.csr               := csr
  io.wen               := arith || logc || shift || comp || link || load || csr || custom
}
