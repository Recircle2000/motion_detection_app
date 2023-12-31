# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.4.1)

# 상대 경로로 프로젝트 디렉토리 설정
#set(pathPROJECT "C:/Users/user/AndroidStudioProjects/visionProject")
set(pathPROJECT "${CMAKE_CURRENT_SOURCE_DIR}/../../../..")

# 소스 파일 경로 설정
set(pathSOURCE "${pathPROJECT}/app/src/main/cpp/native-lib.cpp")

set(pathOPENCV "${pathPROJECT}/openCV")
set(pathLIBOPENCV_JAVA "${pathOPENCV}/native/libs/${ANDROID_ABI}/libopencv_java4.so")

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

include_directories(${pathOPENCV}/native/jni/include)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        #${pathPROJECT}/app/src/main/cpp/native-lib.cpp)
        ${pathSOURCE})


add_library(lib_opencv SHARED IMPORTED)

set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${pathLIBOPENCV_JAVA})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        lib_opencv

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})