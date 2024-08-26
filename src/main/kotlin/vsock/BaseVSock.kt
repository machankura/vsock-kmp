package machankura.vsockk.vsock

import machankura.vsockk.VSockImpl
import java.io.Closeable
import java.io.IOException
import java.net.SocketException

abstract class BaseVSock : Closeable {
    protected val closeLock: Any = Any()

    protected var isClosed: Boolean = false
    protected var created: Boolean = false
    protected var bound: Boolean = false
    private var implementation: VSockImpl? = null

    @Throws(SocketException::class)
    private fun createImplementation() {
        implementation = VSockImpl()
        implementation!!.create()
        created = true
    }

    @Throws(SocketException::class)
    fun getImplementation(): VSockImpl? {
        if (!created) createImplementation()
        return implementation
    }

    @Throws(SocketException::class)
    fun setImplementation(): VSockImpl {
        if (implementation == null) {
            implementation = VSockImpl()
        }
        return implementation!!
    }

    @get:Throws(IOException::class)
    val localCid: Int
        get() = getImplementation()!!.getLocalCid()

    @JvmOverloads
    @Throws(IOException::class)
    fun bind(address: VSockAddress?, backlog: Int = DEFAULT_BACKLOG) {
        var backlog = backlog
        if (isClosed) {
            throw SocketException("Socket closed thrown in Base Vsock")
        }
        if (bound) {
            throw SocketException("Socket already bound")
        }
        if (backlog <= 0) {
            backlog = DEFAULT_BACKLOG
        }
        getImplementation()!!.bind(address)
        getImplementation()!!.listen(backlog)
        bound = true
    }

    @Synchronized
    @Throws(IOException::class)
    override fun close() {
        synchronized(closeLock) {
            if (isClosed) return
            if (created) getImplementation()!!.close()
            isClosed = true
        }
    }

    companion object {
        private const val DEFAULT_BACKLOG = 42
    }
}
