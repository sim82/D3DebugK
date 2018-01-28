package d3debug

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousByteChannel
import java.nio.channels.WritableByteChannel
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

internal class AsyncSyncByteChannelAdapter(private val asyncChannel: AsynchronousByteChannel) : WritableByteChannel {
    private var outstandingWrite: Future<Int>? = null

    @Synchronized
    fun checkOutstandingWrite() {
        val ow = outstandingWrite;
        if (ow == null) {
            return;
        }

        if (!ow.isDone) {
            try {
                ow.get()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }

            outstandingWrite = null
        }

    }

    @Throws(IOException::class)
    override fun write(byteBuffer: ByteBuffer): Int {
        checkOutstandingWrite();
        outstandingWrite = asyncChannel.write(byteBuffer)
        return byteBuffer.limit()
    }

    override fun isOpen(): Boolean {
        return asyncChannel.isOpen
    }

    @Throws(IOException::class)
    override fun close() {
        asyncChannel.close()
    }
}