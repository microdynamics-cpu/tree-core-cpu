package treecorel2

import chisel3._
import chisel3.util._
import treecorel2.common.ConstVal._

class CLINT extends Module with InstConfig {
  val io = IO(new Bundle {
    val addrIn:    UInt = Input(UInt(BusWidth.W))
    val wtEnaIn:   Bool = Input(Bool())
    val wtDataIn:  UInt = Input(UInt(BusWidth.W))
    val rdDataOut: UInt = Output(UInt(BusWidth.W))
    val mtipOut:   Bool = Output(Bool())
    val msipOut:   Bool = Output(Bool())
  })

  protected val mtime:    UInt = RegInit(0.U(BusWidth.W))
  protected val mtimecmp: UInt = RegInit(0.U(BusWidth.W))
  protected val msip:     UInt = RegInit(0.U(BusWidth.W))
  protected val (tickCnt, cntWrap) = Counter(this.clock.asBool(), ClintTickCnt) // generate low speed clock

  msip     := Mux((io.addrIn === MSipOffset) && io.wtEnaIn, io.wtDataIn, msip)
  mtime    := Mux((io.addrIn === MTimeOffset) && io.wtEnaIn, io.wtDataIn, Mux(cntWrap, mtime + 1.U, mtime))
  mtimecmp := Mux((io.addrIn === MTimeCmpOffset) && io.wtEnaIn, io.wtDataIn, mtimecmp)
  io.rdDataOut := MuxLookup(
    io.addrIn,
    0.U,
    Array(
      MSipOffset     -> msip,
      MTimeOffset    -> mtime,
      MTimeCmpOffset -> mtimecmp
    )
  )
  io.mtipOut := RegNext((mtime >= mtimecmp) && (mtimecmp =/= 0.U))
  io.msipOut := RegNext(msip =/= 0.U)
}
