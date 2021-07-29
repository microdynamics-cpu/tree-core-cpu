import treecorel2._

// import org.scalatest._
// import chiseltest._
// import chisel3._

// import chisel3._
// import chisel3.tester._
// import chisel3.experimental.BundleLiterals._

// import utest._

// class TopTest extends FlatSpec with ChiselScalatestTester with Matchers {
//   behavior of "Testers2"

//   //定义一个测试案例
//   it should "test sequential circuits" in {
//     //定义被测的模块
//     test(new Module {
//       val io = IO(new Bundle {
//         val in = Input(UInt(8.W))
//         val out = Output(UInt(8.W))
//       })
//       io.out := RegNext(io.in, 0.U)
//     }) { c =>
//       //添加激励、查看端口值
//       c.io.in.poke(0.U)
//       c.clock.step()
//       c.io.out.expect(0.U)
//       c.io.in.poke(42.U)
//       c.clock.step()
//       c.io.out.expect(42.U)
//     }
//   }
// }

object Top extends App {
  (new chisel3.stage.ChiselStage).execute(
    args,
    Seq(chisel3.stage.ChiselGeneratorAnnotation(() => new TreeCoreL2()))
  )
}
