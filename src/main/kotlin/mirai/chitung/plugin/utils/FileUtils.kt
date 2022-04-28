package mirai.chitung.plugin.utils

import mirai.chitung.plugin.JavaPluginMain
import java.io.File

fun String.fromFileReadText():String = File(this).bufferedReader(Charsets.UTF_8).readText()

fun String.fromFileReadLines():List<String> = File(this).bufferedReader(Charsets.UTF_8).readLines()

fun String.fromResourceReadText():String? = JavaPluginMain::class.java.getResourceAsStream(this)?.bufferedReader(Charsets.UTF_8)?.readText()