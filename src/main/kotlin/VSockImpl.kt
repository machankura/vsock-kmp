package machankura.vsockk

import machankura.vsockk.vsock.VSockAddress
import java.net.SocketException
import java.util.Locale
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class VSockImpl {

    //When building directly on the library call this
    fun initialize() {
        // Load the native library (libvsock-kmp.so or vsock-kmp.dll)
        // System.loadLibrary("vsock-kmp") //don't know why this one does not work even when it's in the right path
        //But the long road
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())

        val libName = when {
            osName.contains("win") -> "vsock-kmp.dll"
            osName.contains("nix") || osName.contains("nux") || osName.contains("mac") -> "libvsock-kmp.so"
            else -> throw UnsupportedOperationException("Unsupported operating system: $osName")
        }

        val path = System.getProperty("user.dir") + "/build/cmake-build/libs/" + libName

        try {
            System.load(path)
        } catch (e: UnsatisfiedLinkError) {
            throw RuntimeException("Failed to load native library: $path", e)
        }
    }

    //When using the Jar file as a lib, we need to extract the native libs out
    fun load()
    {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val libName = when {
            osName.contains("win") -> "vsock-kmp.dll"
            osName.contains("nix") || osName.contains("nux") || osName.contains("mac") -> "libvsock-kmp.so"
            else -> throw UnsupportedOperationException("Unsupported operating system: $osName")
        }

        // Load the library from the JAR file
        val libPath = "/cmake-build/libs/$libName"
        val inputStream: InputStream? = this::class.java.getResourceAsStream(libPath)

        if (inputStream == null) {
            throw UnsatisfiedLinkError("Native library $libName not found in JAR")
        }

        // Create a temporary directory to store the native library
        val tempDir = Files.createTempDirectory("nativeLibs").toFile()
        tempDir.deleteOnExit()

        // Store the lib - temporary file
        val tempLibFile = File(tempDir, libName)
        tempLibFile.deleteOnExit()

        // Do the copying
        inputStream.use { input ->
            tempLibFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Load the library
        try {
            System.load(tempLibFile.absolutePath)
        } catch (e: UnsatisfiedLinkError) {
            throw RuntimeException("Failed to load extracted native library: ${tempLibFile.absolutePath}", e)
        }
    }


    // File descriptor for the socket
    var fd: Int = -1

    /**
     * Create a VSOCK socket.
     * Throws SocketException if socket creation fails.
     */
    @Throws(SocketException::class)
    fun create() {
        socketCreate()
    }

    /**
     * Connect to a VSOCK address.
     * @param address VSockAddress to connect to.
     * Throws Exception if connection fails.
     */
    @Throws(Exception::class)
    fun connect(address: VSockAddress) {
        nativeConnect(address)
    }

    /**
     * Close the VSOCK socket.
     * Throws Exception if close operation fails.
     */
    @Throws(Exception::class)
    fun close() {
        nativeClose()
    }

    /**
     * Write data to the VSOCK socket.
     * @param b Byte array of data to write.
     * @param offset Starting offset in the data.
     * @param len Number of bytes to write.
     * Throws Exception if write operation fails.
     */
    @Throws(Exception::class)
    fun write(b: ByteArray, offset: Int, len: Int) {
        nativeWrite(b, offset, len)
    }

    /**
     * Read data from the VSOCK socket.
     * @param b Byte array buffer to read data into.
     * @param offset Starting offset in the buffer.
     * @param len Number of bytes to read.
     * @return Number of bytes read.
     * Throws Exception if read operation fails.
     */
    @Throws(Exception::class)
    fun read(b: ByteArray, offset: Int, len: Int): Int {
        return nativeRead(b, offset, len)
    }

    /**
     * Bind the VSOCK socket to an address.
     * @param addr VSockAddress to bind to.
     * Throws Exception if bind operation fails.
     */
    @Throws(Exception::class)
    fun bind(addr: VSockAddress) {
        nativeBind(addr)
    }

    /**
     * Listen for incoming connections on the VSOCK socket.
     * @param backlog Maximum number of pending connections.
     * Throws Exception if listen operation fails.
     */
    @Throws(Exception::class)
    fun listen(backlog: Int) {
        nativeListen(backlog)
    }

    /**
     * Accept an incoming connection on the VSOCK socket.
     * @param peerVSock VSockImpl object to store the accepted connection.
     * Throws Exception if accept operation fails.
     */
    @Throws(Exception::class)
    fun accept(peerVSock: VSockImpl) {
        nativeAccept(peerVSock)
    }

    /**
     * Get the local CID (Context Identifier) for the VSOCK socket.
     * @return The local CID.
     * Throws Exception if the operation fails.
     */
    @Throws(Exception::class)
    fun getLocalCid(): Int {
        return nativeGetLocalCid()
    }

    // Native method declarations
    private external fun socketCreate()
    private external fun nativeConnect(address: VSockAddress)
    private external fun nativeClose()
    private external fun nativeWrite(b: ByteArray, offset: Int, len: Int)
    private external fun nativeRead(b: ByteArray, offset: Int, len: Int): Int
    private external fun nativeBind(addr: VSockAddress)
    private external fun nativeListen(backlog: Int)
    private external fun nativeAccept(peerVSock: VSockImpl)
    private external fun nativeGetLocalCid(): Int
}
