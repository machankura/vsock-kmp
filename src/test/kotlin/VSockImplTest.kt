package machankura.vsockk

import machankura.vsockk.vsock.VSockAddress
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.SocketException

class VSockImplTest {

    private lateinit var vSockImpl: VSockImpl

    @Before
    fun setUp() {
        vSockImpl = VSockImpl()
    }

    @After
    fun tearDown() {
        try {
            vSockImpl.close()
        } catch (e: Exception) {
            // Ignore if already closed
        }
    }

    @Test
    fun testSocketCreate() {
        try {
            vSockImpl.create()
            assertTrue("Socket creation failed", vSockImpl.fd != -1)
        } catch (e: SocketException) {
            fail("Exception during socket creation: ${e.message}")
        }
    }

    @Test
    fun testBind() {
        try {
            vSockImpl.create()
            val address = VSockAddress(VSockAddress.VMADDR_CID_ANY, 1234)
            vSockImpl.bind(address)
        } catch (e: Exception) {
            fail("Exception during socket bind: ${e.message}")
        }
    }

    @Test
    fun testListen() {
        try {
            vSockImpl.create()
            val address = VSockAddress(VSockAddress.VMADDR_CID_ANY, 1234)
            vSockImpl.bind(address)
            vSockImpl.listen(5)
        } catch (e: Exception) {
            fail("Exception during socket listen: ${e.message}")
        }
    }

    @Test
    fun testAccept() {
        try {
            vSockImpl.create()
            val address = VSockAddress(VSockAddress.VMADDR_CID_ANY, 1234)
            vSockImpl.bind(address)
            vSockImpl.listen(5)

            val peerVSock = VSockImpl()
            vSockImpl.accept(peerVSock)
            assertTrue("Peer socket accept failed", peerVSock.fd != -1)
        } catch (e: Exception) {
            fail("Exception during socket accept: ${e.message}")
        }
    }

    @Test
    fun testConnect() {
        try {
            vSockImpl.create()
            val address = VSockAddress(VSockAddress.VMADDR_CID_ANY, 1234) // using fixed cid will only pass in an environment such as AWS Nitro Enclave
            vSockImpl.connect(address)
        } catch (e: Exception) {
            fail("Exception during socket connect: ${e.message}")
        }
    }

    @Test
    fun testWriteRead() {
        try {
            vSockImpl.create()
            val address = VSockAddress(VSockAddress.VMADDR_CID_ANY, 1234) // using fixed cid will only pass in an environment such as AWS Nitro Enclave
            vSockImpl.connect(address)

            val dataToWrite = "Hello, VSock!".toByteArray()
            vSockImpl.write(dataToWrite, 0, dataToWrite.size)

            val buffer = ByteArray(1024)
            val bytesRead = vSockImpl.read(buffer, 0, buffer.size)
            assertTrue("Read failed, no data read", bytesRead > 0)
        } catch (e: Exception) {
            fail("Exception during socket write/read: ${e.message}")
        }
    }

    @Test
    fun testClose() {
        try {
            vSockImpl.create()
            vSockImpl.close()
            assertEquals("Socket close failed", -1, vSockImpl.fd)
        } catch (e: Exception) {
            fail("Exception during socket close: ${e.message}")
        }
    }

    @Test
    fun testGetLocalCid() {
        try {
            vSockImpl.create()
            val localCid = vSockImpl.getLocalCid()
            assertTrue("Failed to get local CID", localCid >= 0)
        } catch (e: Exception) {
            fail("Exception during getLocalCid: ${e.message}")
        }
    }
}
