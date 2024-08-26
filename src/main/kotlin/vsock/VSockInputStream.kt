package machankura.vsockk.vsock

import machankura.vsockk.VSockImpl
import java.io.IOException
import java.io.InputStream

class VSockInputStream(private val vSock: VSockImpl) : InputStream() {
    private lateinit var temp: ByteArray

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return vSock.read(b, off, len)
    }

    @Throws(IOException::class)
    override fun read(): Int {
        temp = ByteArray(1)
        val n = read(temp, 0, 1)
        if (n <= 0) {
            return -1
        }
        return temp[0].toInt()
    }

    @Throws(IOException::class)
    override fun close() {
        vSock.close()
        super.close()
    }
}
