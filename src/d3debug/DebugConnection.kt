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

        socket!!.read<AsynchronousSocketChannel>(headerBuffer, socket, object : CompletionHandler<Int, AsynchronousSocketChannel> {
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
        socket!!.read<AsynchronousSocketChannel>(messageBuffer, socket, object : CompletionHandler<Int, AsynchronousSocketChannel> {
            override fun completed(integer: Int?, asynchronousSocketChannel: AsynchronousSocketChannel) {
                messageBuffer.rewind()
                handleMessage(messageBuffer)
                doReadHeader()
            }

            override fun failed(throwable: Throwable, asynchronousSocketChannel: AsynchronousSocketChannel) {

            }
        })
    }


    internal fun scriptInfoRequest(h: ScriptInfoReplyHandler) {
        val b = RequestBuilder()
        val debugRequest = b.initBuilder(Game.DebugRequest.factory)

        debugRequest.initScriptInfo();
        scriptInfoReplyHandlers[nextToken] = h
        debugRequest.token = nextToken++

        b.write(socket)
    }


    var scriptInfoReplyHandlers = HashMap<Long, ScriptInfoReplyHandler>()


    fun scriptGetRequest(id: Int, h: ScriptGetReplyHandler) {
        val b = RequestBuilder()
        val debugRequest = b.initBuilder(Game.DebugRequest.factory)

        val scriptGet = debugRequest.initScriptGet()
        scriptGet.id = id

        scriptGetReplyHandlers[nextToken] = h
        debugRequest.token = nextToken++
        b.write(socket)
    }

    var scriptGetReplyHandlers = HashMap<Long, ScriptGetReplyHandler>()

    private fun handleMessage(messageBuffer: ByteBuffer) = try {
        println("messageBuffer=$messageBuffer")
        val messageReader = Serialize.read(messageBuffer)

        val debugReply = messageReader.getRoot(Game.DebugReply.factory)
        val which = debugReply.which()

        when (debugReply.which()) {
            Game.DebugReply.Which.SCRIPT_INFO -> handleScriptInfoReply(debugReply)
            Game.DebugReply.Which.SCRIPT_GET -> handleScriptGet(debugReply)
            else -> System.err.println("unhandled DebugReply")
        }

    } catch (e: IOException) {
        e.printStackTrace()
    }

    private fun handleScriptGet(debugReply: Game.DebugReply.Reader) {
        val scriptGet = debugReply.scriptGet

        val sourceLines = scriptGet.sourceLines
        val sourcecode = Array<String>(sourceLines.size()) { i ->
            sourceLines[i].toString()
        }

        val handler = scriptGetReplyHandlers[debugReply.token]
        if (handler != null) {
            handler(sourcecode)
        }
    }

    private fun handleScriptInfoReply(debugReply: Game.DebugReply.Reader) {
        val scriptInfo = debugReply.getScriptInfo()
        val m = HashMap<Int, String>()

        val size = scriptInfo.size()
        for (info in scriptInfo) {
            val id = info.id
            val name = info.sourceName
            m[id] = name.toString()
        }

        val handler = scriptInfoReplyHandlers[debugReply.token];
        if (handler != null) {
            handler(m)
        }
    }


    internal inner class RequestBuilder {
        private val messageBuilder: MessageBuilder

        init {
            messageBuilder = MessageBuilder()
        }

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