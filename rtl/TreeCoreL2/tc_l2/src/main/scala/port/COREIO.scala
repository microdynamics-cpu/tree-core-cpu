package treecorel2

import chisel3._
import chisel3.util._

class COREIO extends Bundle {
  val globalEn = Output(Bool())
  val fetch    = Flipped(new IFIO)
  val ld       = Flipped(new LDIO)
  val sd       = Flipped(new SDIO)
}
