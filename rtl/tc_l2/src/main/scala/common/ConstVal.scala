package treecorel2.common

import chisel3._
import chisel3.util.log2Ceil

object ConstVal {
  // addr width
  val AddrLen      = 64
  val AddrAlignLen = log2Ceil(AddrLen / 8)

  val CSRAddrLen   = 12
  val CLINTAddrLen = 64
  // csr addr
  val mhartidAddr  = 0xf14.U(CSRAddrLen.W)
  val mstatusAddr  = 0x300.U(CSRAddrLen.W)
  val mieAddr      = 0x304.U(CSRAddrLen.W)
  val mtvecAddr    = 0x305.U(CSRAddrLen.W)
  val mscratchAddr = 0x340.U(CSRAddrLen.W)
  val mepcAddr     = 0x341.U(CSRAddrLen.W)
  val mcauseAddr   = 0x342.U(CSRAddrLen.W)
  val mipAddr      = 0x344.U(CSRAddrLen.W)
  val mcycleAddr   = 0xb00.U(CSRAddrLen.W)
  val medelegAddr  = 0x302.U(CSRAddrLen.W)

  val ClintBaseAddr  = 0x02000000.U(CLINTAddrLen.W)
  val ClintBoundAddr = 0x0200bfff.U(CLINTAddrLen.W)
  val MSipOffset     = 0x0.U(CLINTAddrLen.W)
  val MTimeOffset    = 0xbff8.U(CLINTAddrLen.W)
  val MTimeCmpOffset = 0x4000.U(CLINTAddrLen.W)
}
