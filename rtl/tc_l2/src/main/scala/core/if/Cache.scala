package treecorel2

import chisel3._
import chisel3.util._

object CacheConfig {
  val ICacheSize = (256 * 1024)
  val DCacheSize = (256 * 1024)
  val LineSize   = 256
  val NWay       = 4
  val NBAN       = 4
  val IdLen      = 1
}

class CACHEREQIO extends Bundle {
  val addr = UInt(64.W)
  val data = UInt(64.W)
  val mask = UInt((64 / 8).W)
  val op   = UInt(1.W) // 0: rd   1: wr
}

class CACHERESPIO extends Bundle {
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
}

class MEMREQIO extends Bundle {
  val addr = UInt(64.W)
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
  val len  = UInt(2.W) // 0: 1(64bits)    1: 2   2: 4  3: 8
  val id   = UInt(CacheConfig.IdLen.W)
}

class MEMRESPIO extends Bundle {
  val data = UInt(64.W)
  val cmd  = UInt(4.W)
  val id   = UInt(CacheConfig.IdLen.W)
}

class WAYINIO(val tagWidth: Int, val idxWidth: Int, val offsetWidth: Int) extends Bundle {
  val wt = Valid(new Bundle {
    val tag    = UInt(tagWidth.W)
    val idx    = UInt(idxWidth.W)
    val offset = UInt(offsetWidth.W)
    val v      = UInt(1.W)
    val d      = UInt(1.W)
    val mask   = UInt(((CacheConfig.LineSize / CacheConfig.NBank) / 8).W)
    val data   = UInt(64.W)
    val op     = UInt(1.W) // must 1
  })
  val rd = Valid(new Bundle {
    val idx = UInt(idxWidth.W)
    val op  = UInt(1.W) // must 0
  })
}

class WAYOUTIO(val tagWidth: Int) extends Bundle {
  val tag  = UInt(tagWidth.W)
  val v    = UInt(1.W)
  val d    = UInt(1.W)
  val data = Vec(CacheConfig.NBank, UInt((CacheConfig.LineSize / CacheConfig.NBank).W))
}

class Way(val tagWidth: Int, val idxWidth: Int, val offsetWidth: Int) extends Module {
  val io = IO(new Bundle {
    val fence_invalid = Input(Bool())
    val in            = Flipped(new WAYINIO(tagWidth, idxWidth, offsetWidth))
    val out           = Valid(new WAYOUTIO(tagWidth))
  })

  val tag   = UInt(tagWidth.W) // tag
  val v     = UInt(1.W) // valid
  val dirty = UInt(1.W)
  val depth = math.pow(2, idxWidth).toInt

  val tagTable   = SyncReadMem(depth, tag)
  val vTable     = RegInit(VecInit(Seq.fill(depth)(0.U(1.W))))
  val dirtyTable = RegInit(VecInit(Seq.fill(depth)(0.U(1.W))))
  // nBank * (n * 8bit)
  val bankn = List.fill(CacheConfig.NBank)(SyncReadMem(depth, Vec((CacheConfig.LineSize / CacheConfig.NBank) / 8, UInt(8.W))))

  val result = WireInit(0.U.asTypeOf(new WAYOUTIO(tagWidth)))

  // read logic
  // TODO: check data order in simulator
  //            tag,v,   d,    data
  result := Cat(
    List(tagTable.read(io.in.rd.bits.idx, io.in.rd.valid).asUInt()) ++ Seq(RegNext(vTable(io.in.rd.bits.idx), 0.U(1.W)), RegNext(dirtyTable(io.in.rd.bits.idx), 0.U(1.W))) ++
      bankn.map(_.read(io.in.rd.bits.idx, io.in.rd.valid).asUInt())
  ).asTypeOf(new WAYOUTIO(tagWidth))

  io.out.bits  := result
  io.out.valid := RegNext(io.in.rd.valid, 0.U)
  dontTouch(io.out.valid)

  // write logic
  // write bank data
  val bank_sel = WireInit(io.in.wt.bits.offset(log2Ceil(64 / 8) + log2Ceil(CacheConfig.NBank) - 1, log2Ceil(64 / 8)))
  val wdata    = io.in.wt.bits.data.asTypeOf(Vec(CacheConfig.LineSize / CacheConfig.NBank / 8, UInt(8.W)))
  when(io.in.wt.fire()) {
    when(io.in.wt.bits.op === 1.U) {
      // write tag,v
      tagTable.write(io.in.wt.bits.idx, io.in.wt.bits.tag)
      vTable(io.in.wt.bits.idx) := io.in.wt.bits.v
      // write d
      dirtyTable(io.in.wt.bits.idx) := io.in.wt.bits.d
      // write data
      bankn.zipWithIndex.foreach((a) => {
        val (bank, i) = a
        when(bank_sel === i.U) {
          bank.write(io.in.wt.bits.idx, wdata, io.in.wt.bits.mask.asBools())
        }
      })
    }
  }.elsewhen(io.fence_invalid) {
    vTable := VecInit(Seq.fill(depth)(0.U(1.W)))
  }
}

class CACHE2CPUIO extends Bundle {
  val req  = Flipped(Decoupled(new CACHEREQIO))
  val resp = Valid(new CACHERESPIO)
}

class CACHE2MEMIO extends Bundle {
  val req  = Decoupled(new MEMREQIO)
  val resp = Flipped(Decoupled(new MEMRESPIO))
}

class Cache extends Module {}
