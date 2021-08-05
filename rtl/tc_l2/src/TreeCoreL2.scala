package treecorel2

import chisel3._

class TreeCoreL2 extends Module with ConstantDefine {
  val io = IO(new Bundle {
    val instDataIn: UInt = Input(UInt(InstWidth.W))
    // val memRDataIn: UInt = Input(UInt(BusWidth.W))

    val instAddrOut: UInt = Output(UInt(BusWidth.W))
    val instEnaOut:  Bool = Output(Bool())

    // val memAddrOut:    UInt = Output(UInt(BusWidth.W))
    // val memDoWriteOut: Bool = Output(Bool())

    // val memEnaOut:    Bool = Output(Bool())
    // val memMaskOut:   UInt = Output(UInt(BusWidth.W))
    // val memWtDataOut: UInt = Output(UInt(BusWidth.W))
  })

  protected val pcUnit = Module(new PCRegister)
  // protected val instCacheUnit = Module(new InstCache)
  protected val if2idUnit   = Module(new IFToID)
  protected val regFile     = Module(new RegFile)
  protected val instDecoder = Module(new InstDecoderStage)
  protected val id2exUnit   = Module(new IDToEX)
  protected val execUnit    = Module(new ExecutionStage)
  protected val ex2maUnit   = Module(new EXToMA)
  protected val memAccess   = Module(new MemoryAccessStage)
  protected val ma2wbUnit   = Module(new MAToWB)

  // instCacheUnit.io.instAddrIn := pcUnit.io.instAddrOut
  // instCacheUnit.io.instEnaIn  := pcUnit.io.instEnaOut
  // TODO: need to pass extra instAddr to the next stage?
  // if to id
  if2idUnit.io.ifInstAddrIn := pcUnit.io.instAddrOut
  // if2idUnit.io.ifInstDataIn := instCacheUnit.io.instDataOut
  if2idUnit.io.ifInstDataIn := io.instDataIn

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
  id2exUnit.io.idWtEnaIn       := instDecoder.io.wtEnaOut
  id2exUnit.io.idWtAddrIn      := instDecoder.io.wtAddrOut
  // ex
  execUnit.io.aluOperTypeIn := id2exUnit.io.exAluOperTypeOut
  execUnit.io.rsValAIn      := id2exUnit.io.exRsValAOut
  execUnit.io.rsValBIn      := id2exUnit.io.exRsValBOut
  // ex to ma
  ex2maUnit.io.exResIn    := execUnit.io.resOut
  ex2maUnit.io.exWtEnaIn  := id2exUnit.io.exWtEnaOut
  ex2maUnit.io.exWtAddrIn := id2exUnit.io.exWtAddrOut
  // ma
  memAccess.io.resIn    := ex2maUnit.io.maResOut
  memAccess.io.wtEnaIn  := ex2maUnit.io.maWtEnaOut
  memAccess.io.wtAddrIn := ex2maUnit.io.maWtAddrOut
  // ma to wb
  ma2wbUnit.io.maResIn    := memAccess.io.resOut
  ma2wbUnit.io.maWtEnaIn  := memAccess.io.wtEnaOut
  ma2wbUnit.io.maWtAddrIn := memAccess.io.wtAddrOut
  
  // wb
  regFile.io.wtDataIn := ma2wbUnit.io.wbResOut
  regFile.io.wtEnaIn  := ma2wbUnit.io.wbWtEnaOut
  regFile.io.wtAddrIn := ma2wbUnit.io.wbWtAddrOut

  io.instAddrOut := pcUnit.io.instAddrOut
  io.instEnaOut  := pcUnit.io.instEnaOut
}
