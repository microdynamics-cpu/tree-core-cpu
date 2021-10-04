package treecorel2.common

import chisel3._

object ConstVal {
  val InstTypeLen       = 3
  val EXUOperNumTypeLen = 3
  val wtDataSrcTypeLen  = 2
  val InstOperTypeLen   = 6
  val CSRAddrLen        = 12
  val CLINTAddrLen      = 64
  val UARTAddrLen       = 32
  val SPIAddrLen        = 32
  val CHIPLINKAddrLen   = 32

  val PrivModeLen = 2
  // exu inst type
  val aluADDIType  = 0.U(InstOperTypeLen.W)
  val aluADDIWType = 1.U(InstOperTypeLen.W)
  val aluSLTIType  = 2.U(InstOperTypeLen.W)
  val aluSLTIUType = 3.U(InstOperTypeLen.W)
  val aluANDIType  = 4.U(InstOperTypeLen.W)
  val aluORIType   = 5.U(InstOperTypeLen.W)
  val aluXORIType  = 6.U(InstOperTypeLen.W)
  val aluSLLIType  = 7.U(InstOperTypeLen.W)
  val aluSLLIWType = 8.U(InstOperTypeLen.W)
  val aluSRLIType  = 9.U(InstOperTypeLen.W)
  val aluSRLIWType = 10.U(InstOperTypeLen.W)
  val aluSRAIType  = 11.U(InstOperTypeLen.W)
  val aluSRAIWType = 12.U(InstOperTypeLen.W)

  val aluLUIType   = 13.U(InstOperTypeLen.W)
  val aluAUIPCType = 14.U(InstOperTypeLen.W)

  val aluADDType  = 15.U(InstOperTypeLen.W)
  val aluADDWType = 16.U(InstOperTypeLen.W)
  val aluSLTType  = 17.U(InstOperTypeLen.W)
  val aluSLTUType = 18.U(InstOperTypeLen.W)
  val aluANDType  = 19.U(InstOperTypeLen.W)
  val aluORType   = 20.U(InstOperTypeLen.W)
  val aluXORType  = 21.U(InstOperTypeLen.W)
  val aluSLLType  = 22.U(InstOperTypeLen.W)
  val aluSLLWType = 23.U(InstOperTypeLen.W)
  val aluSRLType  = 24.U(InstOperTypeLen.W)
  val aluSRLWType = 25.U(InstOperTypeLen.W)
  val aluSUBType  = 26.U(InstOperTypeLen.W)
  val aluSUBWType = 27.U(InstOperTypeLen.W)
  val aluSRAType  = 28.U(InstOperTypeLen.W)
  val aluSRAWType = 29.U(InstOperTypeLen.W)

  val beuJALType  = 30.U(InstOperTypeLen.W)
  val beuJALRType = 31.U(InstOperTypeLen.W)
  val beuBEQType  = 32.U(InstOperTypeLen.W)
  val beuBNEType  = 33.U(InstOperTypeLen.W)
  val beuBLTType  = 34.U(InstOperTypeLen.W)
  val beuBLTUType = 35.U(InstOperTypeLen.W)
  val beuBGEType  = 36.U(InstOperTypeLen.W)
  val beuBGEUType = 37.U(InstOperTypeLen.W)

  val lsuLBType  = 38.U(InstOperTypeLen.W)
  val lsuLBUType = 39.U(InstOperTypeLen.W)
  val lsuLHType  = 40.U(InstOperTypeLen.W)
  val lsuLHUType = 41.U(InstOperTypeLen.W)
  val lsuLWType  = 42.U(InstOperTypeLen.W)
  val lsuLWUType = 43.U(InstOperTypeLen.W)
  val lsuLDType  = 44.U(InstOperTypeLen.W)

  val lsuSBType = 45.U(InstOperTypeLen.W)
  val lsuSHType = 46.U(InstOperTypeLen.W)
  val lsuSWType = 47.U(InstOperTypeLen.W)
  val lsuSDType = 48.U(InstOperTypeLen.W)

  val csrRWType  = 49.U(InstOperTypeLen.W)
  val csrRSType  = 50.U(InstOperTypeLen.W)
  val csrRCType  = 51.U(InstOperTypeLen.W)
  val csrRWIType = 52.U(InstOperTypeLen.W)
  val csrRSIType = 53.U(InstOperTypeLen.W)
  val csrRCIType = 54.U(InstOperTypeLen.W)

  val sysECALLType = 55.U(InstOperTypeLen.W)
  val sysMRETType  = 56.U(InstOperTypeLen.W)

  val custInstType = 62.U(InstOperTypeLen.W)
  val aluNopType   = 63.U(InstOperTypeLen.W)

  // jump type
  val JumpTypeLen  = 2
  val noJumpType   = 0.U(JumpTypeLen.W)
  val uncJumpType  = 1.U(JumpTypeLen.W)
  val condJumpType = 2.U(JumpTypeLen.W)
  val csrJumpType  = 3.U(JumpTypeLen.W)

  // csr addr
  val mHartidAddr  = 0xf14.U(CSRAddrLen.W)
  val mStatusAddr  = 0x300.U(CSRAddrLen.W)
  val mIeAddr      = 0x304.U(CSRAddrLen.W)
  val mTvecAddr    = 0x305.U(CSRAddrLen.W)
  val mScratchAddr = 0x340.U(CSRAddrLen.W)
  val mEpcAddr     = 0x341.U(CSRAddrLen.W)
  val mCauseAddr   = 0x342.U(CSRAddrLen.W)
  val mTvalAddr    = 0x343.U(CSRAddrLen.W)
  val mIpAddr      = 0x344.U(CSRAddrLen.W)
  val mCycleAddr   = 0xb00.U(CSRAddrLen.W)
  // clint addr
  val ClintTickCnt   = 2
  val ClintBaseAddr  = 0x02000000.U(CLINTAddrLen.W)
  val ClintBoundAddr = 0x0200bfff.U(CLINTAddrLen.W)
  val MSipOffset     = 0x0.U(CLINTAddrLen.W)
  val MTimeOffset    = 0xbff8.U(CLINTAddrLen.W)
  val MTimeCmpOffset = 0x4000.U(CLINTAddrLen.W)

  // UART addr
  val UartBaseAddr  = 0x10000000.U(UARTAddrLen.W)
  val UartBoundAddr = 0x10000fff.U(UARTAddrLen.W)
  // SPI addr
  val SpiBaseAddr  = 0x10001000.U(SPIAddrLen.W)
  val SpiBoundAddr = 0x10001fff.U(SPIAddrLen.W)
  // ChipLink addr
  val ChiplinkBaseAddr  = 0x40000000.U(CHIPLINKAddrLen.W)
  val ChiplinkBoundAddr = 0x7fffffff.U(CHIPLINKAddrLen.W)

  // privMode
  val mPrivMode = 3.U(PrivModeLen.W)
  val sPrivMode = 1.U(PrivModeLen.W)
  val uPrivMode = 0.U(PrivModeLen.W)
}
