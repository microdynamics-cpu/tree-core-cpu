package treecorel2.common

import chisel3._
import chisel3.util._

trait InstConfig {
  val SoCEna        = false
  val CacheEna      = false
  val XLen          = 64
  val NWay          = 4
  val NBank         = 4
  val NSet          = 32
  val CacheLineSize = 64 * NBank
  val ICacheSize    = NWay * NSet * CacheLineSize
  val DCacheSize    = NWay * NSet * CacheLineSize
}
