package sim.difftest

import chisel3._
import treecorel2._
import difftest._

class RAMHelper extends BlackBox with InstConfig {
  val io = IO(new Bundle {
    val clk:   Clock = Input(Clock())
    val en:    Bool  = Input(Bool())
    val rIdx:  UInt  = Input(UInt(BusWidth.W))
    val rdata: UInt  = Output(UInt(BusWidth.W))
    val wIdx:  UInt  = Input(UInt(BusWidth.W))
    val wdata: UInt  = Input(UInt(BusWidth.W))
    val wmask: UInt  = Input(UInt(BusWidth.W))
    val wen:   Bool  = Input(Bool())
  })
}

class SimTop(val ifDiffTest: Boolean) extends Module with InstConfig {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO

    // bacause the framework, some below are specific name
    val memAXI_0_aw_ready: Bool = Input(Bool())

    val memAXI_0_aw_valid:      Bool = Output(Bool())
    val memAXI_0_aw_bits_addr:  UInt = Output(UInt(AxiAddrWidth.W))
    val memAXI_0_aw_bits_prot:  UInt = Output(UInt(AxiProtLen.W))
    val memAXI_0_aw_bits_id:    UInt = Output(UInt(AxiIdLen.W))
    val memAXI_0_aw_bits_user:  UInt = Output(UInt(AxiUserLen.W))
    val memAXI_0_aw_bits_len:   UInt = Output(UInt(8.W))
    val memAXI_0_aw_bits_size:  UInt = Output(UInt(3.W))
    val memAXI_0_aw_bits_burst: UInt = Output(UInt(2.W))
    val memAXI_0_aw_bits_lock:  Bool = Output(Bool())
    val memAXI_0_aw_bits_cache: UInt = Output(UInt(4.W))
    val memAXI_0_aw_bits_qos:   UInt = Output(UInt(4.W))

    // write data
    // becuase the framework, now the 'memAXI_0_w_bits_data' need to be replaced
    // by 'memAXI_0_w_bits_data[3:0]' in Makefile
    val memAXI_0_w_ready: Bool = Input(Bool())

    val memAXI_0_w_valid:     Bool = Output(Bool())
    val memAXI_0_w_bits_data: UInt = Output(UInt(AxiDataWidth.W))
    val memAXI_0_w_bits_strb: UInt = Output(UInt(8.W))
    val memAXI_0_w_bits_last: Bool = Output(Bool())

    // write resp
    val memAXI_0_b_valid:     Bool = Input(Bool())
    val memAXI_0_b_bits_resp: UInt = Input(UInt(AxiRespLen.W))
    val memAXI_0_b_bits_id:   UInt = Input(UInt(AxiIdLen.W))
    val memAXI_0_b_bits_user: UInt = Input(UInt(AxiUserLen.W))

    val memAXI_0_b_ready: Bool = Output(Bool())

    // read addr
    val memAXI_0_ar_ready: Bool = Input(Bool())

    val memAXI_0_ar_valid:      Bool = Output(Bool())
    val memAXI_0_ar_bits_addr:  UInt = Output(UInt(AxiAddrWidth.W))
    val memAXI_0_ar_bits_prot:  UInt = Output(UInt(AxiProtLen.W))
    val memAXI_0_ar_bits_id:    UInt = Output(UInt(AxiIdLen.W))
    val memAXI_0_ar_bits_user:  UInt = Output(UInt(AxiUserLen.W))
    val memAXI_0_ar_bits_len:   UInt = Output(UInt(8.W))
    val memAXI_0_ar_bits_size:  UInt = Output(UInt(3.W))
    val memAXI_0_ar_bits_burst: UInt = Output(UInt(2.W))
    val memAXI_0_ar_bits_lock:  Bool = Output(Bool())
    val memAXI_0_ar_bits_cache: UInt = Output(UInt(4.W))
    val memAXI_0_ar_bits_qos:   UInt = Output(UInt(4.W))

    // read data
    // becuase the framework, now the 'memAXI_0_r_bits_data' need to be replaced
    // by 'memAXI_0_r_bits_data[3:0]' in Makefile
    val memAXI_0_r_valid:     Bool = Input(Bool())
    val memAXI_0_r_bits_resp: UInt = Input(UInt(AxiRespLen.W))
    val memAXI_0_r_bits_data: UInt = Input(UInt(AxiDataWidth.W))
    val memAXI_0_r_bits_last: Bool = Input(Bool())
    val memAXI_0_r_bits_id:   UInt = Input(UInt(AxiIdLen.W))
    val memAXI_0_r_bits_use:  UInt = Input(UInt(AxiUserLen.W))

    val memAXI_0_r_ready: Bool = Output(Bool())
  })

  // uart io
  io.uart.in.valid  := false.B
  io.uart.out.valid := false.B
  io.uart.out.ch    := 0.U

  protected val axiBridge: AXI4Bridge = Module(new AXI4Bridge())
  axiBridge.io.axiAwReadyIn := io.memAXI_0_aw_ready
  io.memAXI_0_aw_valid      := axiBridge.io.axiAwValidOut
  io.memAXI_0_aw_bits_addr  := axiBridge.io.axiAwAddrOut
  io.memAXI_0_aw_bits_prot  := axiBridge.io.axiAwProtOut
  io.memAXI_0_aw_bits_id    := axiBridge.io.axiAwIdOut
  io.memAXI_0_aw_bits_user  := axiBridge.io.axiAwUserOut
  io.memAXI_0_aw_bits_len   := axiBridge.io.axiAwLenOut
  io.memAXI_0_aw_bits_size  := axiBridge.io.axiAwSizeOut
  io.memAXI_0_aw_bits_burst := axiBridge.io.axiAwBurstOut
  io.memAXI_0_aw_bits_lock  := axiBridge.io.axiAwLockOut
  io.memAXI_0_aw_bits_cache := axiBridge.io.axiAwCacheOut
  io.memAXI_0_aw_bits_qos   := axiBridge.io.axiAwQosOut

  axiBridge.io.axiWtReadyIn := io.memAXI_0_w_ready
  io.memAXI_0_w_valid       := axiBridge.io.axiWtValidOut
  io.memAXI_0_w_bits_data   := axiBridge.io.axiWtDataOut
  io.memAXI_0_w_bits_strb   := axiBridge.io.axiWtStrbOut
  io.memAXI_0_w_bits_last   := axiBridge.io.axiWtLastOut

  axiBridge.io.axiWtbValidIn := io.memAXI_0_b_valid
  axiBridge.io.axiWtbRespIn  := io.memAXI_0_b_bits_resp
  axiBridge.io.axWtbIdIn     := io.memAXI_0_b_bits_id
  axiBridge.io.axiWtbUserIn  := io.memAXI_0_b_bits_user
  io.memAXI_0_b_ready        := axiBridge.io.axiWtbReadyOut

  axiBridge.io.axiArReadyIn := io.memAXI_0_ar_ready
  io.memAXI_0_ar_valid      := axiBridge.io.axiArValidOut
  io.memAXI_0_ar_bits_addr  := axiBridge.io.axiArAddrOut
  io.memAXI_0_ar_bits_prot  := axiBridge.io.axiArProtOut
  io.memAXI_0_ar_bits_id    := axiBridge.io.axiArIdOut
  io.memAXI_0_ar_bits_user  := axiBridge.io.axiArUserOut
  io.memAXI_0_ar_bits_len   := axiBridge.io.axiArLenOut
  io.memAXI_0_ar_bits_size  := axiBridge.io.axiArSizeOut
  io.memAXI_0_ar_bits_burst := axiBridge.io.axiArBurstOut
  io.memAXI_0_ar_bits_lock  := axiBridge.io.axiArLockOut
  io.memAXI_0_ar_bits_cache := axiBridge.io.axiArCacheOut
  io.memAXI_0_ar_bits_qos   := axiBridge.io.axiArQosOut

  axiBridge.io.axiRdValidIn := io.memAXI_0_r_valid
  axiBridge.io.axiRdRespIn  := io.memAXI_0_r_bits_resp
  axiBridge.io.axiRdDataIn  := io.memAXI_0_r_bits_data
  axiBridge.io.axiRdLastIn  := io.memAXI_0_r_bits_last
  axiBridge.io.axiRdIdIn    := io.memAXI_0_r_bits_id
  axiBridge.io.axiRdUserIn  := io.memAXI_0_r_bits_use
  io.memAXI_0_r_ready       := axiBridge.io.axiRdReadyOut

  protected val treeCoreL2 = Module(new TreeCoreL2(ifDiffTest))

  axiBridge.io.instValidIn := treeCoreL2.io.instValidOut
  // tmp
  axiBridge.io.instReqIn  := 0.U
  axiBridge.io.instAddrIn := treeCoreL2.io.instAddrOut
  axiBridge.io.instSizeIn := treeCoreL2.io.instSizeOut

  axiBridge.io.memValidIn := DontCare
  axiBridge.io.memReqIn   := DontCare
  axiBridge.io.memDataIn  := DontCare
  axiBridge.io.memAddrIn  := DontCare
  axiBridge.io.memSizeIn  := DontCare

  treeCoreL2.io.instReadyIn  := axiBridge.io.instReadyOut
  treeCoreL2.io.instRdDataIn := axiBridge.io.instRdDataOut
  treeCoreL2.io.instRespIn   := axiBridge.io.instRespOut
}

object SimTop extends App {
  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new SimTop(true)))
  )
}
