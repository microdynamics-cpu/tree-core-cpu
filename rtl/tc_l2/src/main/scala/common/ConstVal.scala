package treecorel2.common

import chisel3._

object ConstVal {
  val InstTypeLen       = 3
  val EXUOperNumTypeLen = 3
  val wtDataSrcTypeLen  = 2

  val EXUOperTypeLen = 6

  // exu inst type
  val aluADDIType  = 0.U(EXUOperTypeLen.W)
  val aluADDIWType = 1.U(EXUOperTypeLen.W)
  val aluSLTIType  = 2.U(EXUOperTypeLen.W)
  val aluSLTIUType = 3.U(EXUOperTypeLen.W)
  val aluANDIType  = 4.U(EXUOperTypeLen.W)
  val aluORIType   = 5.U(EXUOperTypeLen.W)
  val aluXORIType  = 6.U(EXUOperTypeLen.W)
  val aluSLLIType  = 7.U(EXUOperTypeLen.W)
  val aluSLLIWType = 8.U(EXUOperTypeLen.W)
  val aluSRLIType  = 9.U(EXUOperTypeLen.W)
  val aluSRLIWType = 10.U(EXUOperTypeLen.W)
  val aluSRAIType  = 11.U(EXUOperTypeLen.W)
  val aluSRAIWType = 12.U(EXUOperTypeLen.W)

  val aluLUIType   = 13.U(EXUOperTypeLen.W)
  val aluAUIPCType = 14.U(EXUOperTypeLen.W)

  val aluADDType  = 15.U(EXUOperTypeLen.W)
  val aluADDWType = 16.U(EXUOperTypeLen.W)
  val aluSLTType  = 17.U(EXUOperTypeLen.W)
  val aluSLTUType = 18.U(EXUOperTypeLen.W)
  val aluANDType  = 19.U(EXUOperTypeLen.W)
  val aluORType   = 20.U(EXUOperTypeLen.W)
  val aluXORType  = 21.U(EXUOperTypeLen.W)
  val aluSLLType  = 22.U(EXUOperTypeLen.W)
  val aluSLLWType = 23.U(EXUOperTypeLen.W)
  val aluSRLType  = 24.U(EXUOperTypeLen.W)
  val aluSRLWType = 25.U(EXUOperTypeLen.W)
  val aluSUBType  = 26.U(EXUOperTypeLen.W)
  val aluSUBWType = 27.U(EXUOperTypeLen.W)
  val aluSRAType  = 28.U(EXUOperTypeLen.W)
  val aluSRAWType = 29.U(EXUOperTypeLen.W)

  val beuJALType = 30.U(EXUOperTypeLen.W)
  val beuJALRType = 31.U(EXUOperTypeLen.W)

  val aluNopType = 63.U(EXUOperTypeLen.W)

  // jump type
  val JumpTypeLen  = 2
  val noJumpType   = 0.U(JumpTypeLen.W)
  val uncJumpType  = 1.U(JumpTypeLen.W)
  val condJumpType = 2.U(JumpTypeLen.W)
}
