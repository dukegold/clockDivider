package clkDivider
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class BasicTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "clkDivider"
  // test class body here
  it should "do something" in {
    test(new clkDivider(divisionValue = 7)).withAnnotations(Seq(VerilatorBackendAnnotation,WriteFstAnnotation)) { c =>
      c.clock.step(200)
    }
  }
}