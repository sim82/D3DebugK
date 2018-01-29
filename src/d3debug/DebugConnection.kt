package d3debug

import d3cp.Game
import org.capnproto.FromPointerBuilder
import org.capnproto.MessageBuilder
import org.capnproto.Serialize
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.AsynchronousByteChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.HashMap


typealias ScriptInfoReplyHandler = (Map<Int, String>) -> Unit
typealias ScriptGetReplyHandler = (Array<String>) -> Unit
typealias AddBreakpointReplyHandler = (Int) -> Unit

class DebugConnection @Throws(IOException::class)
constructor() {


    private var socket: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    private val headerBuffer: ByteBuffer = ByteBuffer.allocate(4)
    private var nextToken: Long = 1

    init {
        socket.connect(InetSocketAddress("localhost", 8094), null, object : CompletionHandler<Void?, Void?> {

            override fun completed(aVoid: Void?, bVoid: Void?) {
                connected()
            }

            override fun failed(throwable: Throwable, asynchronousSocketChannel: Void?) {

            }
        })

    }

    private fun connected() {
        headerBuffer.order(ByteOrder.nativeOrder())
        doReadHeader()
    }

    private fun doReadHeader() {
        headerBuffer.rewind()

        socket.read<AsynchronousSocketChannel>(headerBuffer, socket, object : CompletionHandler<Int, AsynchronousSocketChannel> {
            override fun completed(size: Int?, socket: AsynchronousSocketChannel) {
                headerBuffer.rewind()
                val ib = headerBuffer.asIntBuffer()

                val nextMessageSize = ib.get()
                println("nextMessageSize=$nextMessageSize")

                val messageBuffer = ByteBuffer.allocate(nextMessageSize)
                doReadMessage(messageBuffer)
            }

            override fun failed(throwable: Throwable, asynchronousSocketChannel: AsynchronousSocketChannel) {

            }
        })
    }

    private fun doReadMessage(messageBuffer: ByteBuffer) {
        socket.read<AsynchronousSocketChannel>(messageBuffer, socket, object : CompletionHandler<Int, AsynchronousSocketChannel> {
            override fun completed(integer: Int?, asynchronousSocketChannel: AsynchronousSocketChannel) {
                messageBuffer.rewind()
                handleMessage(messageBuffer)
                doReadHeader()
            }

            override fun failed(throwable: Throwable, asynchronousSocketChannel: AsynchronousSocketChannel) {

            }
        })
    }

    private var scriptInfoReplyHandlers = HashMap<Long, ScriptInfoReplyHandler>()
    internal fun scriptInfoRequest(h: ScriptInfoReplyHandler) {
        withDebugRequest { debugRequest ->

            debugRequest.initScriptInfo()
            scriptInfoReplyHandlers[nextToken] = h
        }
    }


    private var scriptGetReplyHandlers = HashMap<Long, ScriptGetReplyHandler>()
    fun scriptGetRequest(id: Int, h: ScriptGetReplyHandler) {
        withDebugRequest { debugRequest ->

            debugRequest.initScriptGet().let {
                it.id = id
            }

            scriptGetReplyHandlers[nextToken] = h
        }
    }


    private val addBreakpointReplyHandlers = HashMap<Long, AddBreakpointReplyHandler>()
    fun addBreakpoint( scriptId : Int, line : Int, h : AddBreakpointReplyHandler )
    {
        withDebugRequest { debugRequest ->
            debugRequest.initAddBreakpoint() .let {
                it.id = scriptId
                it.line = line
            }
            addBreakpointReplyHandlers[nextToken] = h
        }
    }

    private inline fun withDebugRequest(h : (Game.DebugRequest.Builder) -> Unit) {
        val b = RequestBuilder()
        val debugRequest = b.initBuilder(Game.DebugRequest.factory)!!
        h(debugRequest)
        debugRequest.token = nextToken++
        b.write(socket)
    }


    private fun handleMessage(messageBuffer: ByteBuffer) {
        try {
            println("messageBuffer=$messageBuffer")
            val messageReader = Serialize.read(messageBuffer)

            val debugReply = messageReader.getRoot(Game.DebugReply.factory) ?: return

            val which = debugReply.which()

            when (debugReply.which()) {
                Game.DebugReply.Which.SCRIPT_INFO -> handleScriptInfoReply(debugReply)
                Game.DebugReply.Which.SCRIPT_GET -> handleScriptGet(debugReply)
                Game.DebugReply.Which.ADD_BREAKPOINT -> handleAddBreakpoint(debugReply)
                else -> System.err.println("unhandled DebugReply")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun handleAddBreakpoint(debugReply: Game.DebugReply.Reader) {
        val addBreakpoint = debugReply.addBreakpoint
        val handler = addBreakpointReplyHandlers.remove(debugReply.token) ?: return
        handler(addBreakpoint.breakpointId)
    }


    private fun handleScriptGet(debugReply: Game.DebugReply.Reader) {
        val scriptGet = debugReply.scriptGet
        val handler = scriptGetReplyHandlers.remove(debugReply.token) ?: return


        val sourceLines = scriptGet.sourceLines
        val sourcecode = Array(sourceLines.size()) { i ->
            sourceLines[i].toString()
        }
        handler(sourcecode)
    }

    private fun handleScriptInfoReply(debugReply: Game.DebugReply.Reader) {
        val scriptInfo = debugReply.scriptInfo
        val handler = scriptInfoReplyHandlers.remove(debugReply.token) ?: return

        val m = HashMap<Int, String>()

        val size = scriptInfo.size()
        for (info in scriptInfo) {
            val id = info.id
            val name = info.sourceName
            m[id] = name.toString()
        }
        handler(m)
    }


    internal inner class RequestBuilder {
        private val messageBuilder: MessageBuilder = MessageBuilder()

        fun <TMessage> initBuilder(factory: FromPointerBuilder<TMessage>): TMessage {
            return messageBuilder.initRoot(factory)
        }

        fun write(channel: AsynchronousByteChannel?) {
            val headerBuf = ByteBuffer.allocate(4)
            headerBuf.order(ByteOrder.LITTLE_ENDIAN)
            val intBuf = headerBuf.asIntBuffer()
            intBuf.put(Serialize.computeSerializedSizeInWords(messageBuilder).toInt() * 8)
            channel!!.write(headerBuf)
            try {
                Serialize.write(AsyncSyncByteChannelAdapter(channel), messageBuilder)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}