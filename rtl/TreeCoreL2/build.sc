import mill._
import mill.scalalib._
import mill.scalalib.scalafmt.ScalafmtModule
import mill.scalalib.TestModule.Utest
import mill.bsp._

object tc_l2 extends ScalaModule with ScalafmtModule { m =>
  override def scalaVersion = "2.12.13"
  override def scalacOptions = Seq(
    "-Xsource:2.11",
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    // Enables autoclonetype2 in 3.4.x (on by default in 3.5.0)
    // "-P:chiselplugin:useBundlePlugin"
  )
  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::chisel3:3.5.0",
  )
  override def scalacPluginIvyDeps = Agg(
    ivy"edu.berkeley.cs:::chisel3-plugin:3.5.0",
    ivy"org.scalamacros:::paradise:2.1.1"
  )
  object test extends Tests with Utest {
    override def ivyDeps = m.ivyDeps() ++ Agg(
      ivy"com.lihaoyi::utest:0.7.10",
      ivy"edu.berkeley.cs::chiseltest:0.3.3",
    )
  }

   override def moduleDeps = super.moduleDeps ++ Seq(difftest)
}

object difftest extends ScalaModule {
  override def scalaVersion = "2.12.13"
  override def millSourcePath = os.pwd / "dependency" / "difftest"
  override def ivyDeps = Agg(
    ivy"edu.berkeley.cs::chisel3:3.5.0"
  )
}