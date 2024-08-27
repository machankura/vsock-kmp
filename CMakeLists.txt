cmake_minimum_required(VERSION 3.20)

# Project name and version
project(vsock-kmp VERSION 1.0)

# Detect the platform
if (WIN32)
    set(LIBRARY_OUTPUT_NAME vsock-kmp)
    set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS TRUE)
elseif (UNIX)
    set(LIBRARY_OUTPUT_NAME vsock-kmp)
    set(CMAKE_SHARED_LIBRARY_PREFIX "")
endif()

# Include directories for JNI headers
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Set the output directory for the compiled library
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/libs)

# Define the library target
add_library(${LIBRARY_OUTPUT_NAME} SHARED src/main/cpp/VSockImpl.cpp)
