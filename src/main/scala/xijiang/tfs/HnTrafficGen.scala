package xijiang.tfs

import chisel3._
import org.chipsalliance.cde.config.Parameters
import xijiang.router.{HnTx, HnRx}
import zhujiang.chi.ChannelEncodings
import zhujiang.ZJModule

class HnTrafficGen(local: Boolean)(implicit p: Parameters) extends ZJModule {
  val io = IO(new Bundle {
    val rx = Flipped(new HnTx)
    val tx = Flipped(new HnRx(local))
    val nodeId = Input(UInt(niw.W))
  })
  private val respTxGen = Module(new TrafficSimTx)
  private val dataTxGen = Module(new TrafficSimTx)
  private val snoopTxGen = Module(new TrafficSimTx)
  private val ereqTxGen = if(local) Some(Module(new TrafficSimTx)) else None

  private val reqRxGen = Module(new TrafficSimRx)
  private val respRxGen = Module(new TrafficSimRx)
  private val dataRxGen = Module(new TrafficSimRx)

  TrafficSimTx.connTfsTx(snoopTxGen, io.tx.snoop, io.nodeId, ChannelEncodings.SNP.U, clock, reset)
  TrafficSimTx.connTfsTx(respTxGen, io.tx.resp, io.nodeId, ChannelEncodings.RSP.U, clock, reset)
  TrafficSimTx.connTfsTx(dataTxGen, io.tx.data, io.nodeId, ChannelEncodings.DAT.U, clock, reset)
  if(local) TrafficSimTx.connTfsTx(ereqTxGen.get, io.tx.ereq.get, io.nodeId, ChannelEncodings.ERQ.U, clock, reset)

  TrafficSimRx.connTfsRx(respRxGen, io.rx.resp, io.nodeId, ChannelEncodings.RSP.U, clock, reset)
  TrafficSimRx.connTfsRx(dataRxGen, io.rx.data, io.nodeId, ChannelEncodings.DAT.U, clock, reset)
  TrafficSimRx.connTfsRx(reqRxGen, io.rx.req, io.nodeId, ChannelEncodings.REQ.U, clock, reset)
}
