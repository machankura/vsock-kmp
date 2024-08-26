package machankura.vsockk.vsock

import java.io.Closeable
import java.io.IOException
import java.net.SocketException

class VSock : BaseVSock, Closeable {
    private var connected = false

    @get:Throws(IOException::class)
    @get:Synchronized
    var outputStream: VSockOutputStream? = null
        get() {
            if (isClosed) {
                throw SocketException("VSock is closed thrown in Vsock")
            }
            if (field == null) {
                field = getImplementation()?.let { VSockOutputStream(it) }
            }
            return field
        }
        private set

    @get:Throws(IOException::class)
    @get:Synchronized
    var inputStream: VSockInputStream? = null
        get() {
            if (isClosed) {
                getImplementation()!!.create()
                isClosed = false;
                //throw SocketException("VSock is closed thrown in Vsock")
            }
            if (field == null) {
                field = getImplementation()?.let { VSockInputStream(it) }
            }
            return field
        }
        private set

    constructor()

    constructor(address: VSockAddress?) {
        try {
            getImplementation()!!.connect(address)
        } catch (e: Exception) {
            try {
                close()
            } catch (ce: Exception) {
                e.addSuppressed(ce)
            }
            throw IllegalStateException(e.message, e)
        }
    }

    @Throws(SocketException::class)
    fun connect(address: VSockAddress?) {
        if (isClosed) {
            throw SocketException("Socket closed")
        }
        if (connected) {
            throw SocketException("Socket already connected")
        }
        getImplementation()!!.connect(address)
        connected = true
        bound = true
    }

    fun postAccept() {
        created = true
        bound = true
        connected = true
    }
}
