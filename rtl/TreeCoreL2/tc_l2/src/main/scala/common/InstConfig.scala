package treecorel2

import chisel3._
import chisel3.util._

trait IOConfig {
  val XLen       = 64
  val InstLen    = 32
  val RegfileLen = 5
  val RegfileNum = 1 << RegfileLen
  val ISALen     = 6
  // mem
  val MaskLen = 8
  val LDSize  = 3
  // branch prediction
  val GHRLen    = 5
  val PHTSize   = 1 << GHRLen
  val BTBIdxLen = 5
  val BTBPcLen  = XLen - BTBIdxLen
  val BTBTgtLen = XLen
  val BTBSize   = 1 << BTBIdxLen
}

trait InstConfig extends IOConfig {
  val SoCEna   = false
  val CacheEna = false

  val FlashStartAddr    = "h0000000030000000".U(XLen.W)
  val SimStartAddr      = "h0000000080000000".U(XLen.W)
  val DiffStartBaseAddr = "h0000000080000000".U(XLen.W)
  val SoCStartBaseAddr  = "h0000000000000000".U(XLen.W)
  val DifftestAddrMask  = "hfffffffffffffff8".U(XLen.W)
  val SoCAddrMask       = "hffffffffffffffff".U(XLen.W)
  val InstSoCRSize      = 2.U
  val InstDiffRSize     = 3.U
  val DiffRWSize        = 3.U

  val NOPInst = 0x13.U
  // inst type
  // nop is equal to [addi x0, x0, 0], so the oper is same as 'addi' inst
  val InstTypeLen = 3
  val nopInstType = 2.U(InstTypeLen.W)
  val rInstType   = 1.U(InstTypeLen.W)
  val iInstType   = 2.U(InstTypeLen.W)
  val sInstType   = 3.U(InstTypeLen.W)
  val bInstType   = 4.U(InstTypeLen.W)
  val uInstType   = 5.U(InstTypeLen.W)
  val jInstType   = 6.U(InstTypeLen.W)
  val wtRegTrue   = true.B
  val wtRegFalse  = false.B
  // inst
  val InstValLen  = 6
  val instADDI    = 0.U(InstValLen.W)
  val instADDIW   = 1.U(InstValLen.W)
  val instSLTI    = 2.U(InstValLen.W)
  val instSLTIU   = 3.U(InstValLen.W)
  val instANDI    = 4.U(InstValLen.W)
  val instORI     = 5.U(InstValLen.W)
  val instXORI    = 6.U(InstValLen.W)
  val instSLLI    = 7.U(InstValLen.W)
  val instSLLIW   = 8.U(InstValLen.W)
  val instSRLI    = 9.U(InstValLen.W)
  val instSRLIW   = 10.U(InstValLen.W)
  val instSRAI    = 11.U(InstValLen.W)
  val instSRAIW   = 12.U(InstValLen.W)
  val instLUI     = 13.U(InstValLen.W)
  val instAUIPC   = 14.U(InstValLen.W)
  val instADD     = 15.U(InstValLen.W)
  val instADDW    = 16.U(InstValLen.W)
  val instSLT     = 17.U(InstValLen.W)
  val instSLTU    = 18.U(InstValLen.W)
  val instAND     = 19.U(InstValLen.W)
  val instOR      = 20.U(InstValLen.W)
  val instXOR     = 21.U(InstValLen.W)
  val instSLL     = 22.U(InstValLen.W)
  val instSLLW    = 23.U(InstValLen.W)
  val instSRL     = 24.U(InstValLen.W)
  val instSRLW    = 25.U(InstValLen.W)
  val instSUB     = 26.U(InstValLen.W)
  val instSUBW    = 27.U(InstValLen.W)
  val instSRA     = 28.U(InstValLen.W)
  val instSRAW    = 29.U(InstValLen.W)
  val instNOP     = 30.U(InstValLen.W)
  val instJAL     = 31.U(InstValLen.W)
  val instJALR    = 32.U(InstValLen.W)
  val instBEQ     = 33.U(InstValLen.W)
  val instBNE     = 34.U(InstValLen.W)
  val instBLT     = 35.U(InstValLen.W)
  val instBLTU    = 36.U(InstValLen.W)
  val instBGE     = 37.U(InstValLen.W)
  val instBGEU    = 38.U(InstValLen.W)
  val instLB      = 39.U(InstValLen.W)
  val instLBU     = 40.U(InstValLen.W)
  val instLH      = 41.U(InstValLen.W)
  val instLHU     = 42.U(InstValLen.W)
  val instLW      = 43.U(InstValLen.W)
  val instLWU     = 44.U(InstValLen.W)
  val instLD      = 45.U(InstValLen.W)
  val instSB      = 46.U(InstValLen.W)
  val instSH      = 47.U(InstValLen.W)
  val instSW      = 48.U(InstValLen.W)
  val instSD      = 49.U(InstValLen.W)
  val instCSRRW   = 50.U(InstValLen.W)
  val instCSRRS   = 51.U(InstValLen.W)
  val instCSRRC   = 52.U(InstValLen.W)
  val instCSRRWI  = 53.U(InstValLen.W)
  val instCSRRSI  = 54.U(InstValLen.W)
  val instCSRRCI  = 55.U(InstValLen.W)
  val instECALL   = 56.U(InstValLen.W)
  val instMRET    = 57.U(InstValLen.W)
  val instFENCE   = 58.U(InstValLen.W)
  val instFENCE_I = 59.U(InstValLen.W)
  val instCUST    = 60.U(InstValLen.W)

  // cache
  val NWay          = 4
  val NBank         = 4
  val NSet          = 32
  val CacheLineSize = XLen * NBank
  val ICacheSize    = NWay * NSet * CacheLineSize
  val DCacheSize    = NWay * NSet * CacheLineSize

  // clint
  val ClintBaseAddr  = 0x02000000.U(XLen.W)
  val ClintBoundAddr = 0x0200bfff.U(XLen.W)
  val MSipOffset     = 0x0.U(XLen.W)
  val MTimeOffset    = 0xbff8.U(XLen.W)
  val MTimeCmpOffset = 0x4000.U(XLen.W)
  // csr addr
  val CSRAddrLen   = 12
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
  val timeCause    = "h8000_0000_0000_0007".U(XLen.W)
  val ecallCause   = "h0000_0000_0000_000b".U(XLen.W)

  // special inst
  val customInst = "h0000007b".U(InstLen.W)
  val haltInst   = "h0000006b".U(InstLen.W)
}
