package treecorel2

import org.scalatest._
import chiseltest._
import chisel3._

class PCRegisterTest extends FlatSpec with ChiselScalatestTester with Matchers {
  behavior of "MyModule"
  // test class body here
  it should "do something" in {
  // test case body here
      test(new PCRegister) { dut =>
      // test body here
      dut.instAddr.expect(0.U)
      dut.clock.step()
      dut.instAddr.expect(4.U)
    }
  }
}