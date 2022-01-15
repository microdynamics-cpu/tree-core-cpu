package treecorel2.common

import chisel3._
import chisel3.util._

trait InstConfig {
  val SoCEna = false

  val XLen          = 64
  val NWay          = 4
  val NBank         = 4
  val CacheLineSize = 64 * NBank
  val CacheLineLen  = 32
  val ICacheSize    = NWay * CacheLineSize * CacheLineLen
  val DCacheSize    = NWay * CacheLineSize * CacheLineLen
}
