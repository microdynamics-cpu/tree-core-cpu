package treecorel2

import chisel3._
import chisel3.util._

object AXI4Bridge {
  // Burst types
  protected val AXI_BURST_TYPE_FIXED = "b00".U(2.W)
  protected val AXI_BURST_TYPE_INCR  = "b01".U(2.W)
  protected val AXI_BURST_TYPE_WRAP  = "b10".U(2.W)

// Access permissions
  protected val AXI_PROT_UNPRIVILEGED_ACCESS = "b000".U(3.W)
  protected val AXI_PROT_PRIVILEGED_ACCESS   = "b001".U(3.W)
  protected val AXI_PROT_SECURE_ACCESS       = "b000".U(3.W)
  protected val AXI_PROT_NON_SECURE_ACCESS   = "b010".U(3.W)
  protected val AXI_PROT_DATA_ACCESS         = "b000".U(3.W)
  protected val AXI_PROT_INSTRUCTION_ACCESS  = "b100".U(3.W)
// Memory types (AR)
  protected val AXI_ARCACHE_DEVICE_NON_BUFFERABLE                 = "b0000".U(4.W)
  protected val AXI_ARCACHE_DEVICE_BUFFERABLE                     = "b0001".U(4.W)
  protected val AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE   = "b0010".U(4.W)
  protected val AXI_ARCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE       = "b0011".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_NO_ALLOCATE             = "b1010".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_READ_ALLOCATE           = "b1110".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_WRITE_ALLOCATE          = "b1010".U(4.W)
  protected val AXI_ARCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE = "b1110".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_NO_ALLOCATE                = "b1011".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_READ_ALLOCATE              = "b1111".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_WRITE_ALLOCATE             = "b1011".U(4.W)
  protected val AXI_ARCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE    = "b1111".U(4.W)
// Memory types (AW)
  protected val AXI_AWCACHE_DEVICE_NON_BUFFERABLE                 = "b0000".U(4.W)
  protected val AXI_AWCACHE_DEVICE_BUFFERABLE                     = "b0001".U(4.W)
  protected val AXI_AWCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE   = "b0010".U(4.W)
  protected val AXI_AWCACHE_NORMAL_NON_CACHEABLE_BUFFERABLE       = "b0011".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_NO_ALLOCATE             = "b0110".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_READ_ALLOCATE           = "b0110".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_WRITE_ALLOCATE          = "b1110".U(4.W)
  protected val AXI_AWCACHE_WRITE_THROUGH_READ_AND_WRITE_ALLOCATE = "b1110".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_NO_ALLOCATE                = "b0111".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_READ_ALLOCATE              = "b0111".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_WRITE_ALLOCATE             = "b1111".U(4.W)
  protected val AXI_AWCACHE_WRITE_BACK_READ_AND_WRITE_ALLOCATE    = "b1111".U(4.W)

  protected val AXI_SIZE_BYTES_1   = "b000".U(3.W)
  protected val AXI_SIZE_BYTES_2   = "b001".U(3.W)
  protected val AXI_SIZE_BYTES_4   = "b010".U(3.W)
  protected val AXI_SIZE_BYTES_8   = "b011".U(3.W)
  protected val AXI_SIZE_BYTES_16  = "b100".U(3.W)
  protected val AXI_SIZE_BYTES_32  = "b101".U(3.W)
  protected val AXI_SIZE_BYTES_64  = "b110".U(3.W)
  protected val AXI_SIZE_BYTES_128 = "b111".U(3.W)

  protected val SIZE_B = "b00".U(2.W)
  protected val SIZE_H = "b01".U(2.W)
  protected val SIZE_W = "b10".U(2.W)
  protected val SIZE_D = "b11".U(2.W)
}

class AXI4Bridge extends Module with InstConfig {
  val io = IO(new Bundle {
    val rw_valid_i:   Bool = Input(Bool())
    val rw_req_i:     Bool = Input(Bool())
    val data_write_i: UInt = Input(UInt(BusWidth.W))
    val rw_addr_i:    UInt = Input(UInt(AxiDataWidth.W))
    val rw_size_i:    UInt = Input(UInt(AxiSizeLen.W))

    val rw_ready_o:  Bool = Output(Bool())
    val data_read_o: UInt = Output(UInt(BusWidth.W))
    val rw_resp_o:   UInt = Output(UInt(AxiRespLen.W))

    // write addr
    val axi_aw_ready_i: Bool = Input(Bool())

    val axi_aw_valid_o:  Bool = Output(Bool())
    val axi_aw_addr_o:   UInt = Output(UInt(AxiAddrWidth.W))
    val axi_aw_prot_o:   UInt = Output(UInt(AxiProtLen.W))
    val axi_aw_id_o:     UInt = Output(UInt(AxiIdLen.W))
    val axi_aw_user_o:   UInt = Output(UInt(AxiUserLen.W))
    val axi_aw_len_o:    UInt = Output(UInt(8.W))
    val axi_aw_size_o:   UInt = Output(UInt(3.W))
    val axi_aw_burst_o:  UInt = Output(UInt(2.W))
    val axi_aw_lock_o:   Bool = Output(Bool())
    val axi_aw_cache_o:  UInt = Output(UInt(4.W))
    val axi_aw_qos_o:    UInt = Output(UInt(4.W))
    val axi_aw_region_o: UInt = Output(UInt(4.W))

    // write data
    val axi_w_ready_i: Bool = Input(Bool())

    val axi_w_valid_o: Bool = Output(Bool())
    val axi_w_data_o:  UInt = Output(UInt(AxiDataWidth.W))
    val axi_w_strb_o:  UInt = Output(UInt((AxiDataWidth / 8).W))
    val axi_w_last_o:  Bool = Output(Bool())
    val axi_w_user_o:  UInt = Output(UInt(AxiUserLen.W))

    // write resp
    val axi_b_valid_i: Bool = Input(Bool())
    val axi_b_resp_i:  UInt = Input(UInt(AxiRespLen.W))
    val axi_b_id_i:    UInt = Input(UInt(AxiIdLen.W))
    val axi_b_user_i:  UInt = Input(UInt(AxiUserLen.W))

    val axi_b_ready_o: Bool = Output(Bool())

    // read addr
    val axi_ar_ready_i: Bool = Input(Bool())

    val axi_ar_valid_o:  Bool = Output(Bool())
    val axi_ar_addr_o:   UInt = Output(UInt(AxiAddrWidth.W))
    val axi_ar_prot_o:   UInt = Output(UInt(AxiProtLen.W))
    val axi_ar_id_o:     UInt = Output(UInt(AxiIdLen.W))
    val axi_ar_user_o:   UInt = Output(UInt(AxiUserLen.W))
    val axi_ar_len_o:    UInt = Output(UInt(8.W))
    val axi_ar_size_o:   UInt = Output(UInt(3.W))
    val axi_ar_burst_o:  UInt = Output(UInt(2.W))
    val axi_ar_lock_o:   Bool = Output(Bool())
    val axi_ar_cache_o:  UInt = Output(UInt(4.W))
    val axi_ar_qos_o:    UInt = Output(UInt(4.W))
    val axi_ar_region_o: UInt = Output(UInt(4.W))

    // read data
    val axi_r_valid_i: Bool = Input(Bool())
    val axi_r_resp_i:  UInt = Input(UInt(AxiRespLen.W))
    val axi_r_data_i:  UInt = Input(UInt(AxiDataWidth.W))
    val axi_r_last_i:  Bool = Input(Bool())
    val axi_r_id_i:    UInt = Input(UInt(AxiIdLen.W))
    val axi_r_user_i:  UInt = Input(UInt(AxiUserLen.W))

    val axi_r_ready_o: Bool = Output(Bool())
  })

  protected val w_trans = WireDefault(io.rw_req_i === AxiReqWt.U)
  protected val r_trans = WireDefault(io.rw_req_i === AxiReqRd.U)
  protected val w_valid = WireDefault(io.rw_valid_i & w_trans)
  protected val r_valid = WireDefault(io.rw_valid_i & r_trans)

  // handshake
  protected val aw_hs = WireDefault(io.axi_aw_ready_i & io.axi_aw_valid_o)
  protected val w_hs  = WireDefault(io.axi_w_ready_i & io.axi_w_valid_o)
  protected val b_hs  = WireDefault(io.axi_b_ready_o & io.axi_b_valid_i)
  protected val ar_hs = WireDefault(io.axi_ar_ready_i & io.axi_ar_valid_o)
  protected val r_hs  = WireDefault(io.axi_r_ready_o & io.axi_r_valid_i)

  protected val w_done     = WireDefault(w_hs & io.axi_w_last_o)
  protected val r_done     = WireDefault(r_hs & io.axi_r_last_i)
  protected val trans_done = Mux(w_trans, b_hs, r_done)

  // FSM for read/write
  protected val wtIDLE :: wtADDR :: wtWRITE :: wtRESP :: Nil = Enum(2)
  protected val rdIDLE :: rdADDR :: rdREAD :: Nil            = Enum(2)

  protected val wtStateReg = RegInit(wtIDLE)
  protected val rdStateReg = RegInit(rdIDLE)

  protected val w_state_idle  = WireDefault(wtStateReg === wtIDLE)
  protected val r_state_idle  = WireDefault(rdStateReg === rdIDLE)
  protected val w_state_addr  = WireDefault(wtStateReg === wtADDR)
  protected val r_state_addr  = WireDefault(rdStateReg === rdADDR)
  protected val w_state_write = WireDefault(wtStateReg === wtWRITE)
  protected val w_state_resp  = WireDefault(wtStateReg === wtRESP)
  protected val r_state_read  = WireDefault(rdStateReg === rdREAD)

  switch(wtStateReg) {
    is(wtIDLE) {
      when(w_valid) {
        wtStateReg := wtADDR
      }
    }
    is(wtADDR) {
      when(w_valid && aw_hs) {
        wtStateReg := wtWRITE
      }
    }
    is(wtWRITE) {
      when(w_valid && w_done) {
        wtStateReg := wtRESP
      }
    }
    is(wtRESP) {
      when(w_valid && b_hs) {
        wtStateReg := wtIDLE
      }
    }
  }

  switch(rdStateReg) {
    is(rdIDLE) {
      when(r_valid) {
        rdStateReg := rdADDR
      }
    }
    is(rdADDR) {
      when(r_valid && ar_hs) {
        rdStateReg := rdREAD
      }
    }
    is(rdREAD) {
      when(r_valid && r_done) {
        rdStateReg := rdIDLE
      }
    }
  }

  // ------------------Number of transmission------------------
  protected val len         = RegInit(0.U(8.W))
  protected val len_reset   = WireDefault(this.reset.asBool() || (w_trans && w_state_idle) || (r_trans && r_state_idle))
  protected val len_incr_en = WireDefault((len =/= axi_len) && (w_hs || r_hs))

  when(len_reset) {
    len := 0.U;
  }.elsewhen(len_incr_en) {
    len := len + 1.U
  }

// ------------------Process Data------------------
  protected val ALIGNED_WIDTH = 3 // eval: log2(AxiDataWidth / 8)
  protected val OFFSET_WIDTH  = 6 // eval: log2(AxiDataWidth)
  protected val AXI_SIZE      = 3.U // eval: log2(AxiDataWidth / 8)
  protected val MASK_WIDTH    = AxiDataWidth * 2
  protected val TRANS_LEN     = BusWidth / AxiDataWidth
  protected val BLOCK_TRANS   = Mux((TRANS_LEN > 1).asBool(), true.B, false.B)

  protected val aligned  = WireDefault(BLOCK_TRANS || io.rw_addr_i(ALIGNED_WIDTH - 1, 0) === 0.U)
  protected val size_b   = WireDefault(io.rw_size_i === AXI4Bridge.SIZE_B)
  protected val size_h   = WireDefault(io.rw_size_i === AXI4Bridge.SIZE_H)
  protected val size_w   = WireDefault(io.rw_size_i === AXI4Bridge.SIZE_W)
  protected val size_d   = WireDefault(io.rw_size_i === AXI4Bridge.SIZE_D)
  protected val addr_op1 = WireDefault(Cat(4.U - Fill(ALIGNED_WIDTH, 0.U), io.rw_addr_i(ALIGNED_WIDTH - 1, 0)))
  protected val addr_op2 = WireDefault(
    (Fill(4, size_b) & "b0".U(4.W))
      | (Fill(4, size_h) & "b1".U(4.W))
      | (Fill(4, size_w) & "b11".U(4.W))
      | (Fill(4, size_d) & "b111".U(4.W))
  )

  protected val addr_end = WireDefault(addr_op1 + addr_op2)
  protected val overstep = WireDefault(addr_end(3, ALIGNED_WIDTH) =/= 0.U)

  protected val axi_len  = Mux(aligned.asBool(), (TRANS_LEN - 1).U, Cat(Fill(7, "b0".U(1.W)), overstep))
  protected val axi_size = AXI_SIZE(2, 0);

  protected val axi_addr         = Cat(io.rw_addr_i(AxiAddrWidth - 1, ALIGNED_WIDTH), Fill(ALIGNED_WIDTH, "b0".U(1.W)))
  protected val aligned_offset_l = Cat(OFFSET_WIDTH.U - Fill(ALIGNED_WIDTH, "b0".U(1.W)), io.rw_addr_i(ALIGNED_WIDTH - 1, 0)) << 3
  protected val aligned_offset_h = BusWidth.U - aligned_offset_l
  protected val mask             = 0.U(AxiDataWidth.W)
  // protected val mask              = (
  //                                   (Fill(MASK_WIDTH, size_b) & Cat(MASK_WIDTH.U - Fill(8,  "b0".U(1.W)), 8'hff))
  //                                 | (Fill(MASK_WIDTH, size_h) & Cat(MASK_WIDTH.U - Fill(16, "b0".U(1.W)), 16'hffff))
  //                                 | (Fill(MASK_WIDTH, size_w) & Cat(MASK_WIDTH.U - Fill(32, "b0".U(1.W)), 32'hffffffff))
  //                                 | (Fill(MASK_WIDTH, size_d) & Cat(MASK_WIDTH.U - Fill(64, "b0".U(1.W)), 64'hffffffff_ffffffff))
  //                                 ) << aligned_offset_l

  protected val mask_l = mask(AxiDataWidth - 1, 0)
  protected val mask_h = mask(MASK_WIDTH - 1, AxiDataWidth)

  protected val axi_id   = Fill(AxiIdLen, "b0".U(1.W))
  protected val axi_user = Fill(AxiUserLen, "b0".U(1.W))

  protected val rw_ready = RegInit(false.B)

  protected val rw_ready_nxt = WireDefault(trans_done)
  protected val rw_ready_en  = WireDefault(trans_done | rw_ready)

  when(rw_ready_en) {
    rw_ready := rw_ready_nxt
  }
  io.rw_ready_o := rw_ready

  protected val rw_resp     = RegInit(0.U(2.W))
  protected val rw_resp_nxt = Mux(w_trans, io.axi_b_resp_i, io.axi_r_resp_i)
  protected val resp_en     = WireDefault(trans_done)

  when(resp_en) {
    rw_resp := rw_resp_nxt
  }
  io.rw_resp_o := rw_resp

  // ------------------Write Transaction------------------

  // ------------------Read Transaction------------------

  // Read address channel signals
  io.axi_ar_valid_o := r_state_addr
  io.axi_ar_addr_o  := axi_addr
  io.axi_ar_prot_o  := AXI4Bridge.AXI_PROT_UNPRIVILEGED_ACCESS | AXI4Bridge.AXI_PROT_SECURE_ACCESS | AXI4Bridge.AXI_PROT_DATA_ACCESS
  io.axi_ar_id_o    := axi_id
  io.axi_ar_user_o  := axi_user
  io.axi_ar_len_o   := axi_len
  io.axi_ar_size_o  := axi_size
  io.axi_ar_burst_o := AXI4Bridge.AXI_BURST_TYPE_INCR
  io.axi_ar_lock_o  := "b0".U(1.W)
  io.axi_ar_cache_o := AXI4Bridge.AXI_ARCACHE_NORMAL_NON_CACHEABLE_NON_BUFFERABLE
  io.axi_ar_qos_o   := "h0".U(4.W)

  // Read data channel signals
  io.axi_r_ready_o := r_state_read

  protected val axi_r_data_l = WireDefault((io.axi_r_data_i & mask_l) >> aligned_offset_l)
  protected val axi_r_data_h = WireDefault((io.axi_r_data_i & mask_h) << aligned_offset_h)

  protected val dataReadReg = RegInit(0.U(AxiDataWidth.W))
  io.data_read_o := dataReadReg
  for (i <- 0 until TRANS_LEN) {
    when(io.axi_r_ready_o && io.axi_r_valid_i) {
      when(~aligned && overstep) {
        when(len(0) =/= 0.U) {
          io.data_read_o(AxiDataWidth - 1, 0) := io.data_read_o(AxiDataWidth - 1, 0) | axi_r_data_h
        }.otherwise {
          io.data_read_o(AxiDataWidth - 1, 0) := axi_r_data_l
        }
      }.elsewhen(len === i.U) {
        io.data_read_o((i + 1) * AxiDataWidth, i * AxiDataWidth) := axi_r_data_l
      }
    }
  }
}
