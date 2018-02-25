/*
 * Copyright (c) 2018 Simon A. Berger
 * Licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

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
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.Map
import kotlin.collections.mapTo
import kotlin.collections.set


typealias ScriptInfoReplyHandler = (Map<Int, String>) -> Unit
typealias ScriptGetReplyHandler = (Array<String>) -> Unit
typealias AddBreakpointReplyHandler = (Int) -> Unit
typealias ExecuteReplyHandler = (String, Boolean) -> Unit

typealias EventWatchpointHandler = (Int, Int, Int, ArrayList<String>) -> Unit


class DebugConnection @Throws(IOException::class)
constructor() {


    private var socket = AsynchronousSocketChannel.open()
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
                val ib = headerBuffer.asIntBuffer() ?: return

                val nextMessageSize = ib.get()
                println("nextMessageSize=$nextMessageSize")

                ByteBuffer.allocate(nextMessageSize)?.let {
                    doReadMessage(it)
                }
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
    fun addBreakpoint(scriptId: Int, line: Int, h: AddBreakpointReplyHandler) {
        withDebugRequest { debugRequest ->
            debugRequest.initAddBreakpoint().let {
                it.id = scriptId
                it.line = line
            }
            addBreakpointReplyHandlers[nextToken] = h
        }
    }

    private val executeReplyHandlers = HashMap<Long, ExecuteReplyHandler>()
    fun execute(script: String, h: ExecuteReplyHandler) {
        withDebugRequest { debugRequest ->
            debugRequest.initExecute().let {
                it.setScript(script)
                it.immediate = true
            }
            executeReplyHandlers[nextToken] = h
        }

    }

    private inline fun withDebugRequest(h: (Game.DebugRequest.Builder) -> Unit) {
        val b = RequestBuilder()
        val debugRequest = b.initBuilder(Game.DebugRequest.factory)!!
        h(debugRequest)
        debugRequest.token = nextToken++
        b.write(socket)
    }

    fun subscribeEventWatchpoint(h: EventWatchpointHandler) {
        eventWatchpointHandlers.add(h)
    }


    private fun handleMessage(messageBuffer: ByteBuffer) {
        try {
            println("messageBuffer=$messageBuffer")
            val messageReader = Serialize.read(messageBuffer)

            val debugReply = messageReader.getRoot(Game.DebugReply.factory) ?: return

            when (debugReply.which()) {
                Game.DebugReply.Which.SCRIPT_INFO -> handleScriptInfoReply(debugReply)
                Game.DebugReply.Which.SCRIPT_GET -> handleScriptGet(debugReply)
                Game.DebugReply.Which.ADD_BREAKPOINT -> handleAddBreakpoint(debugReply)
                Game.DebugReply.Which.EVENT_WATCHPOINT -> handleEventWatchpoint(debugReply)
                Game.DebugReply.Which.EXECUTE -> handleExecute(debugReply)
                else -> System.err.println("unhandled DebugReply")
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun runOnUiThread(op: () -> Unit) {
        if (com.sun.glass.ui.Application.isEventThread()) {
            op()
        } else {
            javafx.application.Platform.runLater(op)
        }
    }

    private fun handleAddBreakpoint(debugReply: Game.DebugReply.Reader) {
        val addBreakpoint = debugReply.addBreakpoint
        val handler = addBreakpointReplyHandlers.remove(debugReply.token) ?: return
        runOnUiThread {
            handler(addBreakpoint.breakpointId)
        }
    }


    private fun handleScriptGet(debugReply: Game.DebugReply.Reader) {
        val scriptGet = debugReply.scriptGet
        val handler = scriptGetReplyHandlers.remove(debugReply.token) ?: return


        val sourceLines = scriptGet.sourceLines
        val sourcecode = Array(sourceLines.size()) { i ->
            sourceLines[i].toString()
        }
        runOnUiThread {
            handler(sourcecode)
        }
    }

    private fun handleScriptInfoReply(debugReply: Game.DebugReply.Reader) {
        val scriptInfo = debugReply.scriptInfo
        val handler = scriptInfoReplyHandlers.remove(debugReply.token) ?: return

        val m = HashMap<Int, String>()

        for (info in scriptInfo) {
            val id = info.id
            val name = info.sourceName
            m[id] = name.toString()
        }
        runOnUiThread {
            handler(m)
        }
    }

    private fun handleExecute(debugReply: Game.DebugReply.Reader) {

        val handler = executeReplyHandlers.remove(debugReply.token) ?: return

        val execute = debugReply.execute;

        runOnUiThread {
            handler(execute.consoleOutput.toString(), execute.error)
        }

    }

    private fun handleEventWatchpoint(debugReply: Game.DebugReply.Reader) {
        val eventWatchpoint = debugReply.eventWatchpoint

        runOnUiThread {
            for (h in eventWatchpointHandlers) {
                val localNames = ArrayList<String>();
                localNames.ensureCapacity(eventWatchpoint.localNames.size())

                eventWatchpoint.localNames.mapTo(localNames) {
                    it.toString()
                }
                h(eventWatchpoint.watchpointId, eventWatchpoint.scriptId, eventWatchpoint.line, localNames)

            }
        }
    }

    val eventWatchpointHandlers = HashSet<EventWatchpointHandler>()

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