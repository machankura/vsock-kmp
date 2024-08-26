package machankura.vsockk.vsock

import java.io.IOException
import java.net.SocketException

class ServerVSock : BaseVSock() {
    @Throws(IOException::class)
    fun accept(): VSock {
        if (isClosed) {
            throw SocketException("Socket closed")
        }
        if (!bound) throw SocketException("Socket not bound")
        val socket = VSock()
        socket.setImplementation()
        socket.getImplementation()?.let { getImplementation()!!.accept(it) }
        socket.postAccept()
        return socket
    }
}
