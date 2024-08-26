package machankura.vsockk.vsock

import java.net.SocketAddress
import java.util.Objects

class VSockAddress(val cid: Int, val port: Int) : SocketAddress() {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as VSockAddress
        return cid == that.cid &&
                port == that.port
    }

    override fun hashCode(): Int {
        return Objects.hash(cid, port)
    }

    override fun toString(): String {
        return "VSockAddress{" +
                "cid=" + cid +
                ", port=" + port +
                '}'
    }

    companion object {
        const val VMADDR_CID_ANY: Int = -1
        const val VMADDR_CID_HYPERVISOR: Int = 0
        const val VMADDR_CID_RESERVED: Int = 1
        const val VMADDR_CID_HOST: Int = 2
        const val VMADDR_CID_PARENT: Int = 3

        const val VMADDR_PORT_ANY: Int = -1
    }
}