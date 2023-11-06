package memory
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
class BasicTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "dCache"
  // test class body here
  it should "do something" in {
    test(new dCache(NUM_PORTS = 8)).withAnnotations(Seq(VerilatorBackendAnnotation,WriteFstAnnotation)) { c =>
      c.clock.step(20)
      c.requestor(0).valid.poke(true.B)
      c.requestor(0).bits.write.poke(true.B)
      c.requestor(0).bits.wData.poke(0x3423dfa.U)
      c.requestor(0).bits.address.poke(0x2.U)
      c.clock.step(1)
      c.requestor(0).valid.poke(false.B)
      c.clock.step(2)
      c.requestor(1).valid.poke(true.B)
      c.requestor(1).bits.write.poke(false.B)
      c.requestor(1).bits.address.poke(0x2.U)
      c.clock.step(1)
      c.requestor(1).valid.poke(false.B)
      c.clock.step(4)
    }
  }
}