package machankura.vsockk.vsock

import machankura.vsockk.VSockImpl
import java.io.IOException
import java.io.OutputStream

class VSockOutputStream internal constructor(private val vSock: VSockImpl) : OutputStream() {
    private val temp = ByteArray(1)

    @Throws(IOException::class)
    override fun write(b: Int) {
        temp[0] = b.toByte()
        this.write(temp, 0, 1)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        vSock.write(b, off, len)
    }

    @Throws(IOException::class)
    override fun close() {
        vSock.close()
        super.close()
    }
}
