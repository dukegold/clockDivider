package clkDivider
import math._
import chisel3._
import chisel3.util._
class clkDivider(divisionValue: Int) extends Module{
  require(divisionValue >= 2)
  val io = IO(new Bundle {
    val clkout = Output(UInt(1.W))
  })
  val clkCounter = Counter(divisionValue)
  clkCounter.inc()
  io.clkout := RegNext(clkCounter.value <= (divisionValue/2).U)
}
