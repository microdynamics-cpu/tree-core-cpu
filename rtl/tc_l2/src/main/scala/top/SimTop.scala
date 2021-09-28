package treecorel2

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

class SimTop(val ifDiffTest: Boolean) extends Module with AXI4Config with InstConfig {
  val io = IO(new Bundle {
    val logCtrl  = new LogCtrlIO
    val perfInfo = new PerfInfoIO
    val uart     = new UARTIO

    // bacause the framework, some below are specific name
    val memAXI_0_aw_ready:      Bool = Input(Bool())
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
    val memAXI_0_w_ready:     Bool = Input(Bool())
    val memAXI_0_w_valid:     Bool = Output(Bool())
    val memAXI_0_w_bits_data: UInt = Output(UInt(AxiDataWidth.W))
    val memAXI_0_w_bits_strb: UInt = Output(UInt(8.W))
    val memAXI_0_w_bits_last: Bool = Output(Bool())

    // write resp
    val memAXI_0_b_valid:     Bool = Input(Bool())
    val memAXI_0_b_bits_resp: UInt = Input(UInt(AxiRespLen.W))
    val memAXI_0_b_bits_id:   UInt = Input(UInt(AxiIdLen.W))
    val memAXI_0_b_bits_user: UInt = Input(UInt(AxiUserLen.W))
    val memAXI_0_b_ready:     Bool = Output(Bool())

    // read addr
    val memAXI_0_ar_ready:      Bool = Input(Bool())
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
    val memAXI_0_r_ready:     Bool = Output(Bool())
  })

  // protected val instBridge: AXI4SigBridge = Module(new AXI4SigBridge)
  // protected val memBridge:  AXI4SigBridge = Module(new AXI4SigBridge)
  // protected val axiIntcon:  AXI4Intcon    = Module(new AXI4Intcon)
  // axiIntcon.io.inst <> instBridge.io.axi
  // axiIntcon.io.mem  <> memBridge.io.axi

  protected val axiBridge: AXI4Bridge = Module(new AXI4Bridge)
  axiBridge.io.axi.aw.ready := io.memAXI_0_aw_ready
  io.memAXI_0_aw_valid      := axiBridge.io.axi.aw.valid
  io.memAXI_0_aw_bits_addr  := axiBridge.io.axi.aw.addr
  io.memAXI_0_aw_bits_prot  := axiBridge.io.axi.aw.prot
  io.memAXI_0_aw_bits_id    := axiBridge.io.axi.aw.id
  io.memAXI_0_aw_bits_user  := axiBridge.io.axi.aw.user
  io.memAXI_0_aw_bits_len   := axiBridge.io.axi.aw.len
  io.memAXI_0_aw_bits_size  := axiBridge.io.axi.aw.size
  io.memAXI_0_aw_bits_burst := axiBridge.io.axi.aw.burst
  io.memAXI_0_aw_bits_lock  := axiBridge.io.axi.aw.lock
  io.memAXI_0_aw_bits_cache := axiBridge.io.axi.aw.cache
  io.memAXI_0_aw_bits_qos   := axiBridge.io.axi.aw.qos

  axiBridge.io.axi.w.ready := io.memAXI_0_w_ready
  io.memAXI_0_w_valid      := axiBridge.io.axi.w.valid
  io.memAXI_0_w_bits_data  := axiBridge.io.axi.w.data
  io.memAXI_0_w_bits_strb  := axiBridge.io.axi.w.strb
  io.memAXI_0_w_bits_last  := axiBridge.io.axi.w.last

  axiBridge.io.axi.b.valid := io.memAXI_0_b_valid
  axiBridge.io.axi.b.resp  := io.memAXI_0_b_bits_resp
  axiBridge.io.axi.b.id    := io.memAXI_0_b_bits_id
  axiBridge.io.axi.b.user  := io.memAXI_0_b_bits_user
  io.memAXI_0_b_ready      := axiBridge.io.axi.b.ready

  axiBridge.io.axi.ar.ready := io.memAXI_0_ar_ready
  io.memAXI_0_ar_valid      := axiBridge.io.axi.ar.valid
  io.memAXI_0_ar_bits_addr  := axiBridge.io.axi.ar.addr
  io.memAXI_0_ar_bits_prot  := axiBridge.io.axi.ar.prot
  io.memAXI_0_ar_bits_id    := axiBridge.io.axi.ar.id
  io.memAXI_0_ar_bits_user  := axiBridge.io.axi.ar.user
  io.memAXI_0_ar_bits_len   := axiBridge.io.axi.ar.len
  io.memAXI_0_ar_bits_size  := axiBridge.io.axi.ar.size
  io.memAXI_0_ar_bits_burst := axiBridge.io.axi.ar.burst
  io.memAXI_0_ar_bits_lock  := axiBridge.io.axi.ar.lock
  io.memAXI_0_ar_bits_cache := axiBridge.io.axi.ar.cache
  io.memAXI_0_ar_bits_qos   := axiBridge.io.axi.ar.qos

  axiBridge.io.axi.r.valid := io.memAXI_0_r_valid
  axiBridge.io.axi.r.resp  := io.memAXI_0_r_bits_resp
  axiBridge.io.axi.r.data  := io.memAXI_0_r_bits_data
  axiBridge.io.axi.r.last  := io.memAXI_0_r_bits_last
  axiBridge.io.axi.r.id    := io.memAXI_0_r_bits_id
  axiBridge.io.axi.r.user  := io.memAXI_0_r_bits_use
  io.memAXI_0_r_ready      := axiBridge.io.axi.r.ready

  // axiIntcon.io.out.aw.ready := io.memAXI_0_aw_ready
  // io.memAXI_0_aw_valid      := axiIntcon.io.out.aw.valid
  // io.memAXI_0_aw_bits_addr  := axiIntcon.io.out.aw.addr
  // io.memAXI_0_aw_bits_prot  := axiIntcon.io.out.aw.prot
  // io.memAXI_0_aw_bits_id    := axiIntcon.io.out.aw.id
  // io.memAXI_0_aw_bits_user  := axiIntcon.io.out.aw.user
  // io.memAXI_0_aw_bits_len   := axiIntcon.io.out.aw.len
  // io.memAXI_0_aw_bits_size  := axiIntcon.io.out.aw.size
  // io.memAXI_0_aw_bits_burst := axiIntcon.io.out.aw.burst
  // io.memAXI_0_aw_bits_lock  := axiIntcon.io.out.aw.lock
  // io.memAXI_0_aw_bits_cache := axiIntcon.io.out.aw.cache
  // io.memAXI_0_aw_bits_qos   := axiIntcon.io.out.aw.qos

  // axiIntcon.io.out.w.ready := io.memAXI_0_w_ready
  // io.memAXI_0_w_valid      := axiIntcon.io.out.w.valid
  // io.memAXI_0_w_bits_data  := axiIntcon.io.out.w.data
  // io.memAXI_0_w_bits_strb  := axiIntcon.io.out.w.strb
  // io.memAXI_0_w_bits_last  := axiIntcon.io.out.w.last

  // axiIntcon.io.out.b.valid := io.memAXI_0_b_valid
  // axiIntcon.io.out.b.resp  := io.memAXI_0_b_bits_resp
  // axiIntcon.io.out.b.id    := io.memAXI_0_b_bits_id
  // axiIntcon.io.out.b.user  := io.memAXI_0_b_bits_user
  // io.memAXI_0_b_ready      := axiIntcon.io.out.b.ready

  // axiIntcon.io.out.ar.ready := io.memAXI_0_ar_ready
  // io.memAXI_0_ar_valid      := axiIntcon.io.out.ar.valid
  // io.memAXI_0_ar_bits_addr  := axiIntcon.io.out.ar.addr
  // io.memAXI_0_ar_bits_prot  := axiIntcon.io.out.ar.prot
  // io.memAXI_0_ar_bits_id    := axiIntcon.io.out.ar.id
  // io.memAXI_0_ar_bits_user  := axiIntcon.io.out.ar.user
  // io.memAXI_0_ar_bits_len   := axiIntcon.io.out.ar.len
  // io.memAXI_0_ar_bits_size  := axiIntcon.io.out.ar.size
  // io.memAXI_0_ar_bits_burst := axiIntcon.io.out.ar.burst
  // io.memAXI_0_ar_bits_lock  := axiIntcon.io.out.ar.lock
  // io.memAXI_0_ar_bits_cache := axiIntcon.io.out.ar.cache
  // io.memAXI_0_ar_bits_qos   := axiIntcon.io.out.ar.qos

  // axiIntcon.io.out.r.valid := io.memAXI_0_r_valid
  // axiIntcon.io.out.r.resp  := io.memAXI_0_r_bits_resp
  // axiIntcon.io.out.r.data  := io.memAXI_0_r_bits_data
  // axiIntcon.io.out.r.last  := io.memAXI_0_r_bits_last
  // axiIntcon.io.out.r.id    := io.memAXI_0_r_bits_id
  // axiIntcon.io.out.r.user  := io.memAXI_0_r_bits_use
  // io.memAXI_0_r_ready      := axiIntcon.io.out.r.ready

  protected val treeCoreL2 = Module(new TreeCoreL2(ifDiffTest))
  axiBridge.io.inst <> treeCoreL2.io.inst
  axiBridge.io.mem  <> treeCoreL2.io.mem
  io.uart           <> treeCoreL2.io.uart
  // instBridge.io.rw <> treeCoreL2.io.inst
  // memBridge.io.rw  <> treeCoreL2.io.mem
}
