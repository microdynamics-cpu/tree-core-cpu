package top

import sim._

object TopMain extends App {
  if (true) {
    (new chisel3.stage.ChiselStage).execute(
      args,
      Seq(
        chisel3.stage.ChiselGeneratorAnnotation(() => new SimTop())
      )
    )
  } else {
    (new chisel3.stage.ChiselStage).execute(
      args,
      Seq(
        chisel3.stage.ChiselGeneratorAnnotation(() => new SoCTop()),
        firrtl.stage.RunFirrtlTransformAnnotation(new AddModulePrefix()),
        ModulePrefixAnnotation("ysyx_210324_")
      )
    )
  }
}
