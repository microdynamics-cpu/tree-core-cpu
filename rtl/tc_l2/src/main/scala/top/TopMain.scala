package treecorel2

import treecorel2.common.ConstVal

object TopMain extends App {
  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(
      chisel3.stage.ChiselGeneratorAnnotation(() => new SimTop(ifDiffTest = true, ifSoC = false))
    )
  )

  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(
      chisel3.stage.ChiselGeneratorAnnotation(() => new SoCTop(ifDiffTest = false, ifSoC = true)),
      firrtl.stage.RunFirrtlTransformAnnotation(new AddModulePrefix()),
      ModulePrefixAnnotation("ysyx_210324_")
    )
  )

}
