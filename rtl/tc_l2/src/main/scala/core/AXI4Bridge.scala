package treecorel2

import chisel3._

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
}
