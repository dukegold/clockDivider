package uart
import chisel3.{UInt, _}
import chisel3.util._

class uartRx extends Module{
  val data = IO(Output(UInt(8.W)))
  val valid = IO(Output(UInt(1.W)))
  val baudSel = IO(Input(UInt(2.W)))
  val rxIn = IO(Input(UInt(1.W)))
  object State extends ChiselEnum {
    val sIDLE, sSTARTDET, sDATADET, sPARDET, sSTOPDET = Value
  }
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

  // Rx FSM
  withClock(clkMux.asClock) {
    //Sample the Input data
    val sampCounter = Counter(2)
    val rxInSampReg = RegInit(UInt(3.W), 7.U)
    rxInSampReg := Cat(rxInSampReg(1, 0), rxIn)
    val majBit = Wire(Bool())
    majBit := PopCount(rxInSampReg.asBools) > 2.U
    val dataReg = RegInit(UInt(8.W), 0.U)
    val parity = WireInit(UInt(10.W), dataReg.xorR)
    val parReg = RegInit(UInt(1.W), 0.U)
    val errFlag = RegInit(UInt(1.W), 0.U)
    val fsmState = RegInit(State.sIDLE)
    val bitCounter = Counter(11)

    valid := 0.U
    parity := dataReg.xorR
    switch(fsmState) {
      is(State.sIDLE) {
        when(rxInSampReg(0) === 0.U) {
          fsmState := State.sSTARTDET
          sampCounter.value := 2.U
          bitCounter.value := 0.U
          dataReg := 0.U
        }
      }
      is(State.sSTARTDET) {
        sampCounter.inc()
        when(sampCounter.value === 0.U){
          when(majBit === false.B){
            fsmState := State.sDATADET
          }
            .otherwise{
              fsmState := State.sIDLE
            }
        }
      }
      is(State.sDATADET) {
        sampCounter.inc()
        when(sampCounter.value === 0.U){
          when(majBit){
            dataReg := dataReg | ((1.U)<<bitCounter.value).asUInt
          }
          bitCounter.inc()
          when(bitCounter.value === 7.U){
            fsmState := State.sPARDET
          }
        }
      }
      is(State.sPARDET) {
        sampCounter.inc()
        when(sampCounter.value === 0.U) {
          parReg := majBit
          fsmState := State.sPARDET
        }
      }
      is(State.sSTOPDET) {
        sampCounter.inc()
        when(sampCounter.value === 0.U) {
          when(majBit =/= 1.U){
            errFlag := 1.U
          } otherwise {
            valid := 1.U
          }
          fsmState := State.sIDLE
        }
      }
    }
    data := dataReg
  }
}
