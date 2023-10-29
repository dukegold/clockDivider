package clkDivider

import chisel3._
import circt.stage.ChiselStage
object main extends App {

    circt.stage.ChiselStage.emitSystemVerilog(new clkDivider(divisionValue = 4))
}
