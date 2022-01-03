package treecorel2

import chisel3._
import chisel3.util._

class IMMIO extends Bundle {
  val I = UInt(64.W)
  val B = UInt(64.W)
  val S = UInt(64.W)
  val U = UInt(64.W)
  val J = UInt(64.W)
}
