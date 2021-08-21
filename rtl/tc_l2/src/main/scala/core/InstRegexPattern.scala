package treecorel2

import chisel3._
import chisel3.util.BitPat

object InstRegexPattern {
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

  //CSR inst
  def CSRRS = BitPat("b?????????????????010?????1110011")

  // custom inst such as 0x7B
  def CUST = BitPat("b0000000??????????000?????1111011")
}
