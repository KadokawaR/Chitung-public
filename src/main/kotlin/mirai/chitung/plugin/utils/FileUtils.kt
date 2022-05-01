package mirai.chitung.plugin.utils

import mirai.chitung.plugin.JavaPluginMain
import java.io.File
import java.io.InputStream

fun String.readTextFromFile(): String = File(this).bufferedReader(Charsets.UTF_8).readText()

fun String.readLinesFromFiles(): List<String> = File(this).bufferedReader(Charsets.UTF_8).readLines()

fun String.readTextFromResource(): String? =
    JavaPluginMain::class.java.getResourceAsStream(this)?.bufferedReader(Charsets.UTF_8)?.readText()

fun String.readLinesFromResource(): List<String>? =
    JavaPluginMain::class.java.getResourceAsStream(this)?.bufferedReader(Charsets.UTF_8)?.readLines()

fun String.getResourceByStream(): InputStream? = JavaPluginMain::class.java.getResourceAsStream(this)