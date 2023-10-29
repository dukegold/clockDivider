package uart
import chisel3._
import chisel3.util._

class uartTx extends Module{
  val data = IO(Input(UInt(8.W)))
  val valid = IO(Input(UInt(1.W)))
  val baudSel = IO(Input(UInt(2.W)))
  val txOut = IO(Output(UInt(1.W)))
  val txBusy = IO(Output(UInt(1.W)))
  val dataReg = RegInit(UInt(8.W), 0.U)
  object State extends ChiselEnum {
    val sIDLE, sTX = Value
  }
  val parity = WireInit(UInt(10.W),dataReg.xorR)
  val dataFrame = Wire(UInt(11.W))
  val validReg = RegInit(UInt(1.W),0.U)
  val validClr = Wire(UInt(1.W))
  //clock Dividers
  val clkDiv2 = RegInit(false.B)
  val clkDiv4 = Wire(Bool())
  val clkDiv8 = Wire(Bool())

  val clkMux  = Wire(Bool())
  clkDiv2 := ~clkDiv2

  withClock(clkDiv2.asClock) {
    val clkDiv4Reg = RegInit(false.B)
    clkDiv4Reg := ~clkDiv4Reg
    clkDiv4 := clkDiv4Reg
  }
  withClock(clkDiv4.asClock) {
    val clkDiv8Reg = RegInit(false.B)
    clkDiv8Reg := ~clkDiv8Reg
    clkDiv8 := clkDiv8Reg
  }
  clkMux := Mux(baudSel === 3.U,clkDiv8,
            Mux(baudSel === 2.U,clkDiv4,
            Mux(baudSel === 1.U,clkDiv2,
                clock.asBool))).asBool
  //Latch the valid signal until this frame is sent. Also drives the busy signal.
  when(validReg === 0.U){
    validReg := valid
  }.otherwise{
    when(validClr === 1.U) {
      validReg := 0.U
    }
  }
  txBusy := validReg
  // Build the DataFrame
  // 10: Stop bit
  // 9:  Parity bits
  // 8:1 Data bits

  // Start bit
  dataFrame := Cat(1.U(1.W),parity,dataReg,0.U(1.W))

  // Tx FSM
  withClock(clkMux.asClock) {
    val fsmState = RegInit(State.sIDLE)
    val bitCounter = Counter(11)
    txOut := 1.U
    validClr := 0.U
    // Latch the data input on the first valid rise edge and calculate parity
    when(valid === 1.U && fsmState === State.sIDLE && txBusy =/= 1.U) {
      dataReg := data
    }
    parity := dataReg.xorR
    switch(fsmState) {
      is(State.sIDLE) {
        validClr := 1.U
        txOut := 1.U
        when(valid === 1.U) {
          fsmState := State.sTX
        }
      }
      is(State.sTX) {
        txOut := dataFrame(bitCounter.value)
        when(bitCounter.value < 10.U) {
          fsmState := State.sTX
          bitCounter.inc()
        }.otherwise {
          bitCounter.value := 0.U
          fsmState := State.sIDLE
        }
      }
    }
  }
}
