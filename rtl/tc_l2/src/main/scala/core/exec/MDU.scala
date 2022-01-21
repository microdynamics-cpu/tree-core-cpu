// package treecorel2

// import chisel3._
// import chisel3.util._

// object MDUOpType {
//   def mul    = "b0000".U
//   def mulh   = "b0001".U
//   def mulhsu = "b0010".U
//   def mulhu  = "b0011".U
//   def mulw   = "b0100".U

//   def div   = "b1000".U
//   def divu  = "b1001".U
//   def divuw = "b1010".U
//   def divw  = "b1011".U
//   def rem   = "b1100".U
//   def remu  = "b1101".U
//   def remuw = "b1110".U
//   def remw  = "b1111".U

//   def nop = "b1001".U

//   def isMul(op:     UInt) = !op(3)
//   def isDiv(op:     UInt) = op(3) && !(!op(2) && !op(1) && op(0))
//   def isLhsSign(op: UInt) = false.B
//   def isRhsSign(op: UInt) = false.B
//   def isDivSign(op: UInt) = false.B
//   def isHiRem(op:   UInt) = false.B
//   def isW(op:       UInt) = false.B
// }

// class MDU extends Module {
//   val io = IO(new Bundle {
//     val isa   = Input(new ISAIO)
//     val src1  = Input(UInt(64.W))
//     val src2  = Input(UInt(64.W))
//     val res   = Output(UInt(64.W))
//     val valid = Output(Bool())
//   })

//   protected val mduOp = RegInit(MDUOpType.nop)
//   when(io.isa.MUL) {
//     mduOp := MDUOpType.mul
//   }.elsewhen(io.isa.MULH) {
//     mduOp := MDUOpType.mulh
//   }.elsewhen(io.isa.MULHSU) {
//     mduOp := MDUOpType.mulhsu
//   }.elsewhen(io.isa.MULHU) {
//     mduOp := MDUOpType.mulhu
//   }.elsewhen(io.isa.DIV) {
//     mduOp := MDUOpType.div
//   }.elsewhen(io.isa.DIVU) {
//     mduOp := MDUOpType.divu
//   }.elsewhen(io.isa.REM) {
//     mduOp := MDUOpType.rem
//   }.elsewhen(io.isa.REMU) {
//     mduOp := MDUOpType.remu
//   }.elsewhen(io.isa.MULW) {
//     mduOp := MDUOpType.mulw
//   }.elsewhen(io.isa.DIVW) {
//     mduOp := MDUOpType.divw
//   }.elsewhen(io.isa.DIVUW) {
//     mduOp := MDUOpType.divuw
//   }.elsewhen(io.isa.REMW) {
//     mduOp := MDUOpType.remw
//   }.elsewhen(io.isa.REMUW) {
//     mduOp := MDUOpType.remuw
//   }

//   protected val isLhsSign = MDUOpType.isLhsSign(mduOp)
//   protected val isRhsSign = MDUOpType.isRhsSign(mduOp)
//   protected val isMul     = MDUOpType.isMul(mduOp)
//   protected val isDiv     = MDUOpType.isDiv(mduOp)
//   protected val isHiRem   = MDUOpType.isHiRem(mduOp)

//   protected val multiplier = Module(new Multiplier(64))

//   protected val isSrc1Neg = isLhsSign && io.src1(63)
//   protected val isSrc2Neg = isRhsSign && io.src2(63)
//   protected val isAnsNeg  = isSrc1Neg ^ isSrc2Neg
//   protected val src1      = Mux(isSrc1Neg, -io.src1, io.src1)
//   protected val src2      = Mux(isSrc2Neg, -io.src2, io.src2)
//   multiplier.io.en    := isMul
//   multiplier.io.flush := false.B
//   multiplier.io.src1  := src1
//   multiplier.io.src2  := src2

//   protected val mulRes = Mux(isAnsNeg, -multiplier.io.res, multiplier.io.res)

//   protected val divider       = Module(new Divider(64))
//   protected val fillOnes      = Fill(64, 1.U)
//   protected val srcMin        = 1.U << 64
//   protected val isDivOverflow = isLhsSign && src1 === srcMin && src2 === fillOnes
//   protected val isRemNeg      = isLhsSign && (io.src1(63) ^ divider.io.rem(63))
//   protected val divQuo        = Mux(isAnsNeg, -divider.io.quo, divider.io.quo)
//   protected val divAnsQuo     = Mux(divider.io.divZero, fillOnes, Mux(isDivOverflow, srcMin, divQuo))
//   protected val divRem        = Mux(isRemNeg, -divider.io.rem, divider.io.rem)
//   protected val divAnsRem     = Mux(divider.io.divZero, io.src1, Mux(isDivOverflow, 0.U, divRem))
//   protected val divRes        = Mux(isHiRem, divAnsRem, divAnsQuo)
//   divider.io.en       := isDiv
//   divider.io.flush    := false.B
//   divider.io.divident := src1
//   divider.io.divisor  := src2

//   io.valid := Mux(isMul, multiplier.io.done, Mux(isDiv, divider.io.done, true.B))
//   io.res   := Mux(isMul, mulRes, Mux(isDiv, divRes, 0.U(64.W)))
// }
