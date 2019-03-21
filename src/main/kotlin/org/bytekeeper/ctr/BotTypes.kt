package org.bytekeeper.ctr

enum class BotType(val extension: String) {
    JAVA_MIRROR("jar"),
    JAVA_JNI("jar"),
    AI_MODULE("dll"),
    JYTHON("jython"),
    EXE("exe")
}