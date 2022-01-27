package treecorel3

import chisel._
import chisel.uitl._

class CacheReq(implicit p: Parameters) extends Bundle {
  val addr = UInt(xlen.W)
  val data = UInt(xlen.W)
  val mask = UInt((xlen / 8).W)
}

class CacheResp(implicit p: Parameters) extends Bundle {
  val data = UInt(xlen.W)
}

class CacheIO(implicit val p: Parameters) extends Bundle {
  val abort = Input(Bool())
  val req   = Flipped(Valid(new CacheReq))
  val resp  = Valid(new CacheResp)
}

class CacheModuleIO(implicit val p: Parameters) extends Bundle {
  val cpu   = new CacheIO
  val axi = new AxiIO
}

trait CacheParams extends CoreParams with HasAxiParameters {
  val nWays          = p(NWays) // Not used...
  val nSets          = p(NSets)
  val bBytes         = p(CacheBlockBytes)
  val bBits          = bBytes << 3
  val blen           = log2Ceil(bBytes)
  val slen           = log2Ceil(nSets)
  val tlen           = xlen - (slen + blen)
  val nWords         = bBits / xlen
  val wBytes         = xlen / 8
  val byteOffsetBits = log2Ceil(wBytes)
  val dataBeats      = bBits / axiXDataBits
}

class MetaData(implicit val p: Parameters) extends Bundle with CacheParams {
  val tag = UInt(tlen.W)
}

class Cache(implicit val p: Parameters) extends Module with CacheParams {
  import Chisel._ // FIXME: read enable signals for memories are broken by new chisel
  val io = IO(new CacheModuleIO)
  // cache states
  val (s_IDLE :: s_READ_CACHE :: s_WRITE_CACHE :: s_WRITE_BACK :: s_WRITE_ACK ::
    s_REFILL_READY :: s_REFILL :: Nil) = Enum(7)
  val state                            = RegInit(s_IDLE)
  // memory
  val v       = RegInit(0.U(nSets.W))
  val d       = RegInit(0.U(nSets.W))
  val metaMem = SeqMem(nSets, new MetaData)
  val dataMem = Seq.fill(nWords)(SeqMem(nSets, Vec(wBytes, UInt(8.W))))

  val addr_reg = Reg(io.cpu.req.bits.addr.cloneType)
  val cpu_data = Reg(io.cpu.req.bits.data.cloneType)
  val cpu_mask = Reg(io.cpu.req.bits.mask.cloneType)

  // Counters
  require(dataBeats > 0)
  val (read_count, read_wrap_out)   = Counter(io.axi.r.fire(), dataBeats)
  val (write_count, write_wrap_out) = Counter(io.axi.w.fire(), dataBeats)

  val is_idle      = state === s_IDLE
  val is_read      = state === s_READ_CACHE
  val is_write     = state === s_WRITE_CACHE
  val is_alloc     = state === s_REFILL && read_wrap_out
  val is_alloc_reg = RegNext(is_alloc)

  val hit     = Wire(Bool())
  val wen     = is_write && (hit     || is_alloc_reg) && !io.cpu.abort || is_alloc
  val ren     = !wen     && (is_idle || is_read)      && io.cpu.req.valid
  val ren_reg = RegNext(ren)

  val addr    = io.cpu.req.bits.addr
  val idx     = addr(slen + blen - 1, blen)
  val tag_reg = addr_reg(xlen - 1, slen + blen)
  val idx_reg = addr_reg(slen + blen - 1, blen)
  val off_reg = addr_reg(blen - 1, byteOffsetBits)

  val rmeta      = metaMem.read(idx, ren)
  val rdata      = Cat((dataMem.map(_.read(idx, ren).asUInt)).reverse)
  val rdata_buf  = RegEnable(rdata, ren_reg)
  val refill_buf = Reg(Vec(dataBeats, UInt(axiXDataBits.W)))
  val read       = Mux(is_alloc_reg, refill_buf.asUInt, Mux(ren_reg, rdata, rdata_buf))

  hit := v(idx_reg) && rmeta.tag === tag_reg // NOTE: important!!!Q

  // Read Mux
  io.cpu.resp.bits.data := Vec.tabulate(nWords)(i => read((i + 1) * xlen - 1, i * xlen))(off_reg)
  io.cpu.resp.valid     := is_idle || is_read && hit || is_alloc_reg && !cpu_mask.orR

  when(io.cpu.resp.valid) {
    addr_reg := addr
    cpu_data := io.cpu.req.bits.data
    cpu_mask := io.cpu.req.bits.mask
  }

  val wmeta = Wire(new MetaData)
  wmeta.tag := tag_reg

  val wmask = Mux(!is_alloc, (cpu_mask << Cat(off_reg, 0.U(byteOffsetBits.W))).zext, SInt(-1))
  val wdata = Mux(
    !is_alloc,
    Fill(nWords, cpu_data),
    if (refill_buf.size == 1) io.axi.r.bits.data
    else Cat(io.axi.r.bits.data, Cat(refill_buf.init.reverse))
  )
  when(wen) {
    v := v.bitSet(idx_reg, true.B)
    d := d.bitSet(idx_reg, !is_alloc)
    when(is_alloc) {
      metaMem.write(idx_reg, wmeta)
    }
    dataMem.zipWithIndex.foreach {
      case (mem, i) =>
        val data = Vec.tabulate(wBytes)(k => wdata(i * xlen + (k + 1) * 8 - 1, i * xlen + k * 8))
        mem.write(idx_reg, data, wmask((i + 1) * wBytes - 1, i * wBytes).toBools)
        mem.suggestName(s"dataMem_${i}")
    }
  }

  io.axi.ar.bits  := AxiReadAddressChannel(0.U, Cat(tag_reg, idx_reg) << blen.U, log2Up(axiXDataBits / 8).U, (dataBeats - 1).U)
  io.axi.ar.valid := false.B
  // read data
  io.axi.r.ready := state === s_REFILL
  when(io.axi.r.fire()) { refill_buf(read_count) := io.axi.r.bits.data }

  // write addr
  io.axi.aw.bits  := AxiWriteAddressChannel(0.U, Cat(rmeta.tag, idx_reg) << blen.U, log2Up(axiXDataBits / 8).U, (dataBeats - 1).U)
  io.axi.aw.valid := false.B
  // write data
  io.axi.w.bits  := AxiWriteDataChannel(Vec.tabulate(dataBeats)(i => read((i + 1) * axiXDataBits - 1, i * axiXDataBits))(write_count), None, write_wrap_out)
  io.axi.w.valid := false.B
  // write resp
  io.axi.b.ready := false.B

  // Cache FSM
  val is_dirty = v(idx_reg) && d(idx_reg)
  switch(state) {
    is(s_IDLE) {
      when(io.cpu.req.valid) {
        state := Mux(io.cpu.req.bits.mask.orR, s_WRITE_CACHE, s_READ_CACHE)
      }
    }
    is(s_READ_CACHE) {
      when(hit) {
        when(io.cpu.req.valid) {
          state := Mux(io.cpu.req.bits.mask.orR, s_WRITE_CACHE, s_READ_CACHE)
        }.otherwise {
          state := s_IDLE
        }
      }.otherwise {
        io.axi.aw.valid := is_dirty
        io.axi.ar.valid := !is_dirty
        when(io.axi.aw.fire()) {
          state := s_WRITE_BACK
        }.elsewhen(io.axi.ar.fire()) {
          state := s_REFILL
        }
      }
    }
    is(s_WRITE_CACHE) {
      when(hit || is_alloc_reg || io.cpu.abort) {
        state := s_IDLE
      }.otherwise {
        io.axi.aw.valid := is_dirty
        io.axi.ar.valid := !is_dirty
        when(io.axi.aw.fire()) {
          state := s_WRITE_BACK
        }.elsewhen(io.axi.ar.fire()) {
          state := s_REFILL
        }
      }
    }
    is(s_WRITE_BACK) {
      io.axi.w.valid := true.B
      when(write_wrap_out) {
        state := s_WRITE_ACK
      }
    }
    is(s_WRITE_ACK) {
      io.axi.b.ready := true.B
      when(io.axi.b.fire()) {
        state := s_REFILL_READY
      }
    }
    is(s_REFILL_READY) {
      io.axi.ar.valid := true.B
      when(io.axi.ar.fire()) {
        state := s_REFILL
      }
    }
    is(s_REFILL) {
      when(read_wrap_out) {
        state := Mux(cpu_mask.orR, s_WRITE_CACHE, s_IDLE)
      }
    }
  }
}
