#include <jni.h>
#include "VSockImpl.h" // Include the generated JNI header
#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <linux/vm_sockets.h>
#include <unistd.h>
#include <errno.h>

#define JVM_IO_INTR (-2)
#define BUFFER_LEN 65536
#define min(a, b) ((a) < (b) ? (a) : (b))

extern "C" {

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_socketCreate(JNIEnv *env, jobject thisObj) {
int fd = socket(AF_VSOCK, SOCK_STREAM, 0);
if (fd < 0) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Failed to create socket");
return;
}

// Store the socket descriptor in the Java object's field
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
env->SetIntField(thisObj, fdField, fd);
}

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_connect(JNIEnv *env, jobject thisObj, jobject addr) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return;
}

// Get the cid and port from the VSockAddress object
jclass VSockAddressClass = env->GetObjectClass(addr);
jfieldID cidField = env->GetFieldID(VSockAddressClass, "cid", "I");
jfieldID portField = env->GetFieldID(VSockAddressClass, "port", "I");

struct sockaddr_vm sock_addr;
std::memset(&sock_addr, 0, sizeof(struct sockaddr_vm));
sock_addr.svm_family = AF_VSOCK;
sock_addr.svm_port = env->GetIntField(addr, portField);
sock_addr.svm_cid = env->GetIntField(addr, cidField);

int status = ::connect(fd, (struct sockaddr *)&sock_addr, sizeof(struct sockaddr_vm));

if (status != 0) {
env->ThrowNew(env->FindClass("java/net/ConnectException"),
("Connect failed with error no: " + std::to_string(errno)).c_str());
}
}

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_close(JNIEnv *env, jobject thisObj) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
return;  // Socket is already closed
}

int status = ::close(fd);
if (status != 0) {
env->ThrowNew(env->FindClass("java/net/SocketException"),
("Close failed with error no: " + std::to_string(errno)).c_str());
}

// Mark the socket as closed
env->SetIntField(thisObj, fdField, -1);
}

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_write(JNIEnv *env, jobject thisObj, jbyteArray b, jint offset, jint len) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return;
}

char BUF[BUFFER_LEN];
while (len > 0) {
int chunkLen = min(BUFFER_LEN, len);
env->GetByteArrayRegion(b, offset, chunkLen, (jbyte *)BUF);

int n = (int)::send(fd, BUF, chunkLen, 0);
if (n <= 0) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Write failed");
return;
}

len -= chunkLen;
offset += chunkLen;
}
}

JNIEXPORT jint JNICALL
        Java_machankura_vsockk_native_VSockImpl_read(JNIEnv *env, jobject thisObj, jbyteArray b, jint offset, jint len) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return -1;
}

char *bufP = (char *)malloc((size_t)len);
int nread = (int)::recv(fd, bufP, len, 0);

if (nread < 0) {
env->ThrowNew(env->FindClass("java/net/SocketException"),
("Read failed with error no: " + std::to_string(errno)).c_str());
free(bufP);
return -1;
}

env->SetByteArrayRegion(b, offset, nread, (jbyte *)bufP);
free(bufP);
return nread;
}

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_bind(JNIEnv *env, jobject thisObj, jobject addr) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return;
}

// Get the cid and port from the VSockAddress object
jclass VSockAddressClass = env->GetObjectClass(addr);
jfieldID cidField = env->GetFieldID(VSockAddressClass, "cid", "I");
jfieldID portField = env->GetFieldID(VSockAddressClass, "port", "I");

struct sockaddr_vm sock_addr;
std::memset(&sock_addr, 0, sizeof(struct sockaddr_vm));
sock_addr.svm_family = AF_VSOCK;
sock_addr.svm_port = env->GetIntField(addr, portField);
sock_addr.svm_cid = env->GetIntField(addr, cidField);

int status = ::bind(fd, (struct sockaddr *)&sock_addr, sizeof(struct sockaddr_vm));

if (status != 0) {
env->ThrowNew(env->FindClass("java/net/BindException"),
("Bind failed with error no: " + std::to_string(errno)).c_str());
}
}

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_listen(JNIEnv *env, jobject thisObj, jint backlog) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return;
}

int status = ::listen(fd, backlog);

if (status != 0) {
env->ThrowNew(env->FindClass("java/net/SocketException"),
("Listen failed with error no: " + std::to_string(errno)).c_str());
}
}

JNIEXPORT void JNICALL
Java_machankura_vsockk_native_VSockImpl_accept(JNIEnv *env, jobject thisObj, jobject connectionVSock) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return;
}

struct sockaddr_vm peer_addr;
socklen_t peer_addr_size = sizeof(struct sockaddr_vm);
int peer_fd = ::accept(fd, (struct sockaddr *)&peer_addr, &peer_addr_size);

if (peer_fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"),
("Accept failed with error no: " + std::to_string(errno)).c_str());
return;
}

// Set the peer_fd in the Java connectionVSock object
jclass VSockImplClass = env->GetObjectClass(connectionVSock);
jfieldID peerFdField = env->GetFieldID(VSockImplClass, "fd", "I");
env->SetIntField(connectionVSock, peerFdField, peer_fd);
}

JNIEXPORT jint JNICALL
        Java_machankura_vsockk_native_VSockImpl_getLocalCid(JNIEnv *env, jobject thisObj) {
jclass thisClass = env->GetObjectClass(thisObj);
jfieldID fdField = env->GetFieldID(thisClass, "fd", "I");
int fd = env->GetIntField(thisObj, fdField);

if (fd == -1) {
env->ThrowNew(env->FindClass("java/net/SocketException"), "Socket is closed");
return -1;
}

unsigned int cid;
ioctl(fd, IOCTL_VM_SOCKETS_GET_LOCAL_CID, &cid);
return (jint)cid;
}

} // extern "C"
