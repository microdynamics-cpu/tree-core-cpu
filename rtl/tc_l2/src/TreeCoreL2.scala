package treecorel2

import chisel3._

class TreeCoreL2 extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val out1: Bool = Input(Bool())
    val out2: UInt = Input(UInt(RegAddrLen.W))
    val out3: UInt = Input(UInt(BusWidth.W))

  })

  private val pcUnit        = Module(new PCRegister)
  private val instCacheUnit = Module(new InstCache)
  private val if2idUnit     = Module(new IFToID)
  private val regFile       = Module(new RegFile)
  private val instDecoder   = Module(new InstDecoderStage)
  private val id2exUnit     = Module(new IDToEX)
  private val execUnit      = Module(new ExecutionStage)

  instCacheUnit.io.instAddrIn := pcUnit.io.instAddrOut
  instCacheUnit.io.instEnaIn  := pcUnit.io.instEnaOut

  // TODO: need to pass extra instAddr to the next stage?
  // if to id
  if2idUnit.io.ifInstAddrIn := pcUnit.io.instAddrOut
  if2idUnit.io.ifInstDataIn := instCacheUnit.io.instDataOut

  // inst decoder
  instDecoder.io.instAddrIn := if2idUnit.io.idInstAddrOut
  instDecoder.io.instDataIn := if2idUnit.io.idInstDataOut
  instDecoder.io.rdDataAIn  := regFile.io.rdDataAOut
  instDecoder.io.rdDataBIn  := regFile.io.rdDataBOut

  regFile.io.rdEnaAIn  := instDecoder.io.rdEnaAOut
  regFile.io.rdAddrAIn := instDecoder.io.rdAddrAOut
  regFile.io.rdEnaBIn  := instDecoder.io.rdEnaBOut
  regFile.io.rdAddrBIn := instDecoder.io.rdAddrBOut

  // id to ex
  id2exUnit.io.idAluOperTypeIn := instDecoder.io.aluOperTypeOut
  id2exUnit.io.idRsValAIn      := instDecoder.io.rsValAOut
  id2exUnit.io.idRsValBIn      := instDecoder.io.rsValBOut
  // ex
  execUnit.io.aluOperTypeIn := id2exUnit.io.exAluOperTypeOut
  execUnit.io.rsValAIn      := id2exUnit.io.exRsValAOut
  execUnit.io.rsValBIn      := id2exUnit.io.exRsValBOut
  // ex to ma
  // ma
  // ma to wb

  // demo
  regFile.io.wtEnaIn  := io.out1
  regFile.io.wtAddrIn := io.out2
  regFile.io.wtDataIn := io.out3

}
