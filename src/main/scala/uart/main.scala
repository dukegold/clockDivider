package uart

object main extends App{
    val firtoolLoweringOpts = Array(
        "--lowering-options=disallowPortDeclSharing",
        "disallowExpressionInliningInPorts",
        "disallowLocalVariables",
        "exprInEventControl",
        "locationInfoStyle=none",
        "emitWireInPorts",
        "emittedLineLength=200",
        "wireSpillingNamehintTermLimit=50",
        "omitVersionComment",
        "disallowPackedStructAssignments"
    )

    val firtoolOpts = Array(
        "--disable-all-randomization",
        "--disable-annotation-unknown",
        "--emit-separate-always-blocks",
        "--emit-chisel-asserts-as-sva",
        //"--split-verilog",
        firtoolLoweringOpts.mkString(","), //lowering options are separated by comma
        "-O=release"
    )
    circt.stage.ChiselStage.emitSystemVerilogFile(new uartTx(),Array(),firtoolOpts)
    circt.stage.ChiselStage.emitSystemVerilogFile(new uartRx(),Array(),firtoolOpts)

}
