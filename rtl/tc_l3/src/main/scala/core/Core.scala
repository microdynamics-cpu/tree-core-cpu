package treecorel3

import chisel._
import chisel.uitl._

class Core(implicit val p: Parameters) extends Module {
  val dpath = Module(new DataPath)
  val ctrl  = Module(new Control)
}
