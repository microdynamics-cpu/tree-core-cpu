package treecorel2

import chisel3._
import chisel3.util._
import treecorel2.common.ConstVal._

class CLINT extends Module with InstConfig {
  val io = IO(new Bundle {
    val wt:       TRANSIO = Flipped(new TRANSIO) // from ma
    val rd:       TRANSIO = new TRANSIO // to wb
    val intrInfo: INTRIO  = new INTRIO // to csr
  })

  protected val mtime:    UInt = RegInit(0.U(BusWidth.W))
  protected val mtimecmp: UInt = RegInit(10000.U(BusWidth.W)) // HACK: need to modify this code
  protected val msip:     UInt = RegInit(0.U(BusWidth.W))
  protected val (tickCnt, cntWrap) = Counter(this.clock.asBool(), ClintTickCnt) // generate low speed clock

  msip     := Mux((io.wt.addr === ClintBaseAddr + MSipOffset) && io.wt.ena, io.wt.data, msip)
  mtime    := Mux((io.wt.addr === ClintBaseAddr + MTimeOffset) && io.wt.ena, io.wt.data, Mux(cntWrap, mtime + 1.U, mtime))
  mtimecmp := Mux((io.wt.addr === ClintBaseAddr + MTimeCmpOffset) && io.wt.ena, io.wt.data, mtimecmp)

  io.rd.addr := io.wt.addr // TODO: modify the rd addr var name
  // HACK
  io.rd.ena := MuxLookup(
    io.rd.addr,
    false.B,
    Array(
      ClintBaseAddr + MSipOffset     -> true.B,
      ClintBaseAddr + MTimeOffset    -> true.B,
      ClintBaseAddr + MTimeCmpOffset -> true.B
    )
  )

  io.rd.data := MuxLookup(
    io.rd.addr,
    0.U,
    Array(
      ClintBaseAddr + MSipOffset     -> msip,
      ClintBaseAddr + MTimeOffset    -> mtime,
      ClintBaseAddr + MTimeCmpOffset -> mtimecmp
    )
  )

  io.intrInfo.mtip := WireDefault(mtime >= mtimecmp) // FIXME: some time sequence problem
  io.intrInfo.msip := WireDefault(msip =/= 0.U)
}
