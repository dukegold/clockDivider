package memory
import chisel3._
import chisel3.util._
class dCacheCmdBundle extends Bundle {
  val address = UInt(13.W)
  val wData   = UInt(32.W)
  val write   = Bool()
}

class dCache extends Module with RequireAsyncReset {
  val requestor = IO(Vec(8,Flipped(DecoupledIO(new dCacheCmdBundle))))

  val cmdQueuesAry = Seq.fill(8)(Module(new Queue(new dCacheCmdBundle, 4)))
  requestor.zipWithIndex.map{case(x,y) =>
    x <> cmdQueuesAry(y).io.enq
  }
  
  cmdQueuesAry(0).io.deq <> cmdOut(0)
  cmdQueuesAry(1).io := DontCare
  cmdQueuesAry(2).io := DontCare
  cmdQueuesAry(3).io := DontCare
  cmdQueuesAry(4).io := DontCare
  cmdQueuesAry(5).io := DontCare
  cmdQueuesAry(6).io := DontCare
  cmdQueuesAry(7).io := DontCare
}
