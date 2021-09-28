package treecorel2

import treecorel2.common.ConstVal

object TopMain extends App {
  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(
      chisel3.stage.ChiselGeneratorAnnotation(() => new SimTop(true))
    )
  )

  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(
      chisel3.stage.ChiselGeneratorAnnotation(() => new SoCTop(false)),
      firrtl.stage.RunFirrtlTransformAnnotation(new AddModulePrefix()),
      ModulePrefixAnnotation("ysyx_210324_")
    )
  )

}
