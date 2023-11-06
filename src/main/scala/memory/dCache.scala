package memory
import chisel3._
import chisel3.util._
class dCacheCmdBundle extends Bundle {
  val address = UInt(13.W)
  val wData   = UInt(32.W)
  val write   = Bool()
}

class dCache (val NUM_PORTS: Int = 8) extends Module with RequireAsyncReset {
  val requestor = IO(Vec(NUM_PORTS,Flipped(DecoupledIO(new dCacheCmdBundle))))
  val rData = IO(Output(Vec(NUM_PORTS,ValidIO(UInt(32.W)))))
  val cmdQueuesInstAry = Seq.fill(NUM_PORTS)(Module(new Queue(new dCacheCmdBundle, 4)))
  val cmdArbiterInst = Module(new RRArbiter(new dCacheCmdBundle,NUM_PORTS))

  requestor.zipWithIndex.map { case (x, y) =>
    x <> cmdQueuesInstAry(y).io.enq
  }
  cmdQueuesInstAry.zipWithIndex.map { case (queue, num) =>
    cmdArbiterInst.io.in(num) <> queue.io.deq
  }
  val arbChosenPipe = RegNext(cmdArbiterInst.io.chosen)

  val mem = SyncReadMem(8192,UInt(32.W))
  val memDataOut = Wire(UInt(32.W))
  //Single Port SRAM
  memDataOut := DontCare
  when(cmdArbiterInst.io.out.valid) {
    val rdwrPort = mem(cmdArbiterInst.io.out.bits.address)
    when(cmdArbiterInst.io.out.bits.write) {
      rdwrPort := cmdArbiterInst.io.out.bits.wData
    }
      .otherwise {
        memDataOut := rdwrPort
      }
  }
  cmdArbiterInst.io.out.ready := 1.U
  rData := DontCare
  rData(0).bits <> memDataOut
  rData(1).bits <> memDataOut
  rData(2).bits <> memDataOut
  rData(3).bits <> memDataOut
  rData(4).bits <> memDataOut
  rData(5).bits <> memDataOut
  rData(6).bits <> memDataOut
  rData(7).bits <> memDataOut
  rData(arbChosenPipe).valid := 1.U
}
