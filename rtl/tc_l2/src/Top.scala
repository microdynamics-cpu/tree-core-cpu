import treecorel2._

object Top extends App {
  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new TreeCoreL2()))
  )
}
