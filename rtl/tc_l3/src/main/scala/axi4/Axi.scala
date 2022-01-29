package treecorel3

import chisel._
import chisel.uitl._

trait HasAxiParameters {
  implicit val p: Parameters
  val axiExternal    = p(AxiKey)
  val axiXDataBits   = axiExternal.dataBits
  val axiWStrobeBits = axiXDataBits / 8
  val axiXAddrBits   = axiExternal.addrBits
  val axiWIdBits     = axiExternal.idBits
  val axiRIdBits     = axiExternal.idBits
  val axiXIdBits     = max(axiWIdBits, axiRIdBits)
  val axiXUserBits   = 1
  val axiAWUserBits  = axiXUserBits
  val axiWUserBits   = axiXUserBits
  val axiBUserBits   = axiXUserBits
  val axiARUserBits  = axiXUserBits
  val axiRUserBits   = axiXUserBits
  val axiXLenBits    = 8
  val axiXSizeBits   = 3
  val axiXBurstBits  = 2
  val axiXCacheBits  = 4
  val axiXProtBits   = 3
  val axiXQosBits    = 4
  val axiXRegionBits = 4
  val axiXRespBits   = 2

  def bytesToXSize(bytes: UInt) = MuxLookup(
    bytes,
    UInt("b111"),
    Array(UInt(1) -> UInt(0), UInt(2) -> UInt(1), UInt(4) -> UInt(2), UInt(8) -> UInt(3), UInt(16) -> UInt(4), UInt(32) -> UInt(5), UInt(64) -> UInt(6), UInt(128) -> UInt(7))
  )
}

abstract class AxiModule(implicit val p: Parameters) extends Module with HasAxiParameters
abstract class AxiBundle(implicit val p: Parameters) extends Bundle with HasAxiParameters

abstract class AxiChannel(implicit p: Parameters) extends AxiBundle()(p)
abstract class AxiMasterToSlaveChannel(implicit p: Parameters) extends AxiChannel()(p)
abstract class AxiSlaveToMasterChannel(implicit p: Parameters) extends AxiChannel()(p)

trait HasAxiMetadata extends HasAxiParameters {
  val addr   = UInt(width = axiXAddrBits)
  val len    = UInt(width = axiXLenBits)
  val size   = UInt(width = axiXSizeBits)
  val burst  = UInt(width = axiXBurstBits)
  val lock   = Bool()
  val cache  = UInt(width = axiXCacheBits)
  val prot   = UInt(width = axiXProtBits)
  val qos    = UInt(width = axiXQosBits)
  val region = UInt(width = axiXRegionBits)
}

trait HasAxiData extends HasAxiParameters {
  val data = UInt(width = axiXDataBits)
  val last = Bool()
}

class AxiReadIO(implicit val p: Parameters) extends Bundle {
  val ar = Decoupled(new AxiReadAddressChannel)
  val r  = Decoupled(new AxiReadDataChannel).flip
}

class AxiWriteIO(implicit val p: Parameters) extends Bundle {
  val aw = Decoupled(new AxiWriteAddressChannel)
  val w  = Decoupled(new AxiWriteDataChannel)
  val b  = Decoupled(new AxiWriteResponseChannel).flip
}

class AxiIO(implicit val p: Parameters) extends Bundle {
  val aw = Decoupled(new AxiWriteAddressChannel)
  val w  = Decoupled(new AxiWriteDataChannel)
  val b  = Decoupled(new AxiWriteResponseChannel).flip
  val ar = Decoupled(new AxiReadAddressChannel)
  val r  = Decoupled(new AxiReadDataChannel).flip
}

class AxiAddressChannel(implicit p: Parameters) extends AxiMasterToSlaveChannel()(p) with HasAxiMetadata

class AxiResponseChannel(implicit p: Parameters) extends AxiSlaveToMasterChannel()(p) {
  val resp = UInt(width = axiXRespBits)
}

class AxiWriteAddressChannel(implicit p: Parameters) extends AxiAddressChannel()(p) {
  val id   = UInt(width = axiWIdBits)
  val user = UInt(width = axiAWUserBits)
}

class AxiWriteDataChannel(implicit p: Parameters) extends AxiMasterToSlaveChannel()(p) with HasAxiData {
  val id   = UInt(width = axiWIdBits)
  val strb = UInt(width = axiWStrobeBits)
  val user = UInt(width = axiWUserBits)
}

class AxiWriteResponseChannel(implicit p: Parameters) extends AxiResponseChannel()(p) {
  val id   = UInt(width = axiWIdBits)
  val user = UInt(width = axiBUserBits)
}

class AxiReadAddressChannel(implicit p: Parameters) extends AxiAddressChannel()(p) {
  val id   = UInt(width = axiRIdBits)
  val user = UInt(width = axiARUserBits)
}

class AxiReadDataChannel(implicit p: Parameters) extends AxiResponseChannel()(p) with HasAxiData {
  val id   = UInt(width = axiRIdBits)
  val user = UInt(width = axiRUserBits)
}

object AxiConstants {
  def BURST_FIXED = UInt("b00")
  def BURST_INCR  = UInt("b01")
  def BURST_WRAP  = UInt("b10")

  def RESP_OKAY   = UInt("b00")
  def RESP_EXOKAY = UInt("b01")
  def RESP_SLVERR = UInt("b10")
  def RESP_DECERR = UInt("b11")

  def CACHE_DEVICE_NOBUF         = UInt("b0000")
  def CACHE_DEVICE_BUF           = UInt("b0001")
  def CACHE_NORMAL_NOCACHE_NOBUF = UInt("b0010")
  def CACHE_NORMAL_NOCACHE_BUF   = UInt("b0011")

  def AXPROT(instruction: Bool, nonsecure: Bool, privileged: Bool): UInt =
    Cat(instruction, nonsecure, privileged)

  def AXPROT(instruction: Boolean, nonsecure: Boolean, privileged: Boolean): UInt =
    AXPROT(Bool(instruction), Bool(nonsecure), Bool(privileged))
}

import AxiConstants._

object AxiWriteAddressChannel {
  def apply(id: UInt, addr: UInt, size: UInt, len: UInt = UInt(0), burst: UInt = BURST_INCR)(implicit p: Parameters) = {
    val aw = Wire(new AxiWriteAddressChannel)
    aw.id     := id
    aw.addr   := addr
    aw.len    := len
    aw.size   := size
    aw.burst  := burst
    aw.lock   := Bool(false)
    aw.cache  := CACHE_DEVICE_NOBUF
    aw.prot   := AXPROT(false, false, false)
    aw.qos    := UInt("b0000")
    aw.region := UInt("b0000")
    aw.user   := UInt(0)
    aw
  }
}

object AxiReadAddressChannel {
  def apply(id: UInt, addr: UInt, size: UInt, len: UInt = UInt(0), burst: UInt = BURST_INCR)(implicit p: Parameters) = {
    val ar = Wire(new AxiReadAddressChannel)
    ar.id     := id
    ar.addr   := addr
    ar.len    := len
    ar.size   := size
    ar.burst  := burst
    ar.lock   := Bool(false)
    ar.cache  := CACHE_DEVICE_NOBUF
    ar.prot   := AXPROT(false, false, false)
    ar.qos    := UInt(0)
    ar.region := UInt(0)
    ar.user   := UInt(0)
    ar
  }
}

object AxiWriteDataChannel {
  def apply(data: UInt, strb: Option[UInt] = None, last: Bool = Bool(true), id: UInt = UInt(0))(implicit p: Parameters): AxiWriteDataChannel = {
    val w = Wire(new AxiWriteDataChannel)
    w.strb := strb.getOrElse(Fill(w.axiWStrobeBits, UInt(1, 1)))
    w.data := data
    w.last := last
    w.id   := id
    w.user := UInt(0)
    w
  }
}

object AxiReadDataChannel {
  def apply(
    id:   UInt,
    data: UInt,
    last: Bool = Bool(true),
    resp: UInt = UInt(0)
  )(
    implicit p: Parameters
  ) = {
    val r = Wire(new AxiReadDataChannel)
    r.id   := id
    r.data := data
    r.last := last
    r.resp := resp
    r.user := UInt(0)
    r
  }
}

object AxiWriteResponseChannel {
  def apply(id: UInt, resp: UInt = UInt(0))(implicit p: Parameters) = {
    val b = Wire(new AxiWriteResponseChannel)
    b.id   := id
    b.resp := resp
    b.user := UInt(0)
    b
  }
}

class AxiArbiterIO(arbN: Int)(implicit p: Parameters) extends Bundle {
  val master = Vec(arbN, new AxiIO).flip
  val slave  = new AxiIO
  override def cloneType =
    new AxiArbiterIO(arbN).asInstanceOf[this.type]
}

/** Arbitrate among arbN masters requesting to a single slave */
class AxiArbiter(val arbN: Int)(implicit p: Parameters) extends AxiModule {
  val io = new AxiArbiterIO(arbN)

  if (arbN > 1) {
    val arbIdBits = log2Up(arbN)

    val ar_arb = Module(new RRArbiter(new AxiReadAddressChannel, arbN))
    val aw_arb = Module(new RRArbiter(new AxiWriteAddressChannel, arbN))

    val slave_r_arb_id = io.slave.r.bits.id(arbIdBits - 1, 0)
    val slave_b_arb_id = io.slave.b.bits.id(arbIdBits - 1, 0)

    val w_chosen = Reg(UInt(width = arbIdBits))
    val w_done   = Reg(init = Bool(true))

    when(aw_arb.io.out.fire()) {
      w_chosen := aw_arb.io.chosen
      w_done   := Bool(false)
    }

    when(io.slave.w.fire() && io.slave.w.bits.last) {
      w_done := Bool(true)
    }

    for (i <- 0 until arbN) {
      val m_ar = io.master(i).ar
      val m_aw = io.master(i).aw
      val m_r  = io.master(i).r
      val m_b  = io.master(i).b
      val a_ar = ar_arb.io.in(i)
      val a_aw = aw_arb.io.in(i)
      val m_w  = io.master(i).w

      a_ar         <> m_ar
      a_ar.bits.id := Cat(m_ar.bits.id, UInt(i, arbIdBits))

      a_aw         <> m_aw
      a_aw.bits.id := Cat(m_aw.bits.id, UInt(i, arbIdBits))

      m_r.valid   := io.slave.r.valid && slave_r_arb_id === UInt(i)
      m_r.bits    := io.slave.r.bits
      m_r.bits.id := io.slave.r.bits.id >> UInt(arbIdBits)

      m_b.valid   := io.slave.b.valid && slave_b_arb_id === UInt(i)
      m_b.bits    := io.slave.b.bits
      m_b.bits.id := io.slave.b.bits.id >> UInt(arbIdBits)

      m_w.ready := io.slave.w.ready && w_chosen === UInt(i) && !w_done
    }

    io.slave.r.ready := io.master(slave_r_arb_id).r.ready
    io.slave.b.ready := io.master(slave_b_arb_id).b.ready

    io.slave.w.bits  := io.master(w_chosen).w.bits
    io.slave.w.valid := io.master(w_chosen).w.valid && !w_done

    io.slave.ar <> ar_arb.io.out

    io.slave.aw.bits    <> aw_arb.io.out.bits
    io.slave.aw.valid   := aw_arb.io.out.valid && w_done
    aw_arb.io.out.ready := io.slave.aw.ready   && w_done

  } else {
    io.slave <> io.master.head
  }
}
