package machankura.vsockk

import machankura.vsockk.vsock.VSockAddress
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.SocketException
import kotlin.test.fail
import org.junit.jupiter.api.*

//This tests the native functions
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)  // ordered testing necessary
class VSockImplTest {

    private lateinit var vSockImpl: VSockImpl
    private var peerVSock: VSockImpl? = null
    private var localCid: Int? = null // Store the local CID obtained from testGetLocalCid, only way we can test connect and read,write

    @BeforeAll
    fun setUpAll() {
        vSockImpl = VSockImpl()
        vSockImpl.initialize()
        vSockImpl.create()  // Create the socket once at the beginning
    }

    @AfterAll
    fun tearDownAll() {
        try {
            vSockImpl.close()
            peerVSock?.close()
        } catch (e: Exception) {
            // Ignore
        }
    }

    @Test
    @Order(1) // First test to run
    fun testSocketCreate() {
        try {
            // The socket is created in @BeforeAll; no need to create it ahain
            assertTrue(vSockImpl.fd != -1, "Socket creation failed")
        } catch (e: SocketException) {
            fail("Exception during socket creation: ${e.message}")
        }
    }

    @Test
    @Order(2) // Run after testSocketCreate
    fun testBind() {
        try {
            // Reuse the socket created in setUpAll
            val address = VSockAddress(VSockAddress.VMADDR_CID_ANY, 1234)
            vSockImpl.bind(address)
        } catch (e: Exception) {
            fail("Exception during socket bind: ${e.message}")
        }
    }

    @Test
    @Order(3) // Run after testBind
    fun testListen() {
        try {
            vSockImpl.listen(0)
        } catch (e: Exception) {
            fail("Exception during socket listen: ${e.message}")
        }
    }

    @Test
    @Order(4) // Run after testAccept
    fun testGetLocalCid() {
        try {
            val cid = vSockImpl.getLocalCid()
            assertTrue(cid != 0, "Failed to get local CID") //How do I check this if it can be a +/- number, verify it can never be 0
            localCid = cid
        } catch (e: Exception) {
            fail("Exception during getLocalCid: ${e.message}")
        }
    }

    /* // Difficult to test these three without host plus vm setup
    @Test
    @Order(5) // Run after testGetLocalCid
    fun testConnect() {
        try {
            requireNotNull(localCid) { "Local CID is null. Ensure testGetLocalCid() passed." }

            // Use the CID obtained from testGetLocalCid
            val address = VSockAddress(localCid!!, 1234)
            vSockImpl.connect(address)
        } catch (e: Exception) {
            fail("Exception during socket connect: ${e.message}")
        }
    }

    @Test
    @Order(6)
    fun testAccept() {
        try {
            peerVSock = VSockImpl()

            // We need a separate client connection
            val clientThread = Thread {
                try {
                    val clientSocket = VSockImpl()
                    clientSocket.create()
                    val localCid = vSockImpl.getLocalCid()
                    val address = VSockAddress(localCid, 1234)
                    clientSocket.connect(address)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            clientThread.start()

            // All that in an attempt to change the fd
            vSockImpl.accept(peerVSock!!)
            assertTrue(peerVSock!!.fd != -1, "Peer socket accept failed")

            clientThread.join()

        } catch (e: Exception) {
            fail("Exception during socket accept: ${e.message}")
        }
    }

    @Test
    @Order(7) // Run after testConnect
    fun testWriteRead() {
        try {
            val dataToWrite = "Hello, from the VSock!".toByteArray()
            vSockImpl.write(dataToWrite, 0, dataToWrite.size)

            val buffer = ByteArray(1024)
            val bytesRead = vSockImpl.read(buffer, 0, buffer.size)
            assertTrue(bytesRead > 0, "Read failed, no data read")
        } catch (e: Exception) {
            fail("Exception during socket write/read: ${e.message}")
        }
    }
*/
    @Test
    @Order(7) // Run after testWriteRead
    fun testClose() {
        try {
            vSockImpl.close()
            assertEquals(-1, vSockImpl.fd, "Socket close failed")
        } catch (e: Exception) {
            fail("Exception during socket close: ${e.message}")
        }
    }
}