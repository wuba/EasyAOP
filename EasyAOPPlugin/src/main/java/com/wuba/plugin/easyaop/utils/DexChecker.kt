package com.wuba.plugin.easyaop.utils

import com.android.dex.DexFormat
import com.android.dx.command.dexer.Main
import com.wuba.easy.log.green
import com.wuba.easy.log.red
import com.wuba.easy.utils.CommUtils
import com.wuba.easy.utils.createNewZip
import java.io.File
import java.util.zip.ZipFile

/**
 *
 * Created by wswenyue on 2022/1/19.
 */
class DexChecker(
    private val fileRecordIterable: Iterable<File>,
    private val temporaryDir: File,
    private val apiLevel: Int? = null
) {

    fun doVerify() {
        fileRecordIterable.onEach { inputFile ->
            val outputFile = File(temporaryDir, inputFile.name)
            val rc = when (inputFile.extension) {
                "jar" -> {
                    ZipFile(inputFile).createNewZip(
                        File(temporaryDir, "temp_jar_" + inputFile.name),
                        { entry -> !entry.name.endsWith("module-info.class") }
                    ).let { inputZipFile ->
                        val ret = doDexChecker(outputFile, inputZipFile, apiLevel)
                        inputZipFile.delete()
                        ret
                    }
                }
                else -> {
                    doDexChecker(outputFile, inputFile, apiLevel)
                }
            }
            println("${if (rc != 0) "✗".red() else "✓".green()} $inputFile")
            outputFile.deleteRecursively()
        }
    }

    // DexFormat.API_NO_EXTENDED_OPCODES
    private fun doDexChecker(
        outputFile: File,
        inputFile: File,
        api: Int? = null
    ): Int {
        val minSdk = api ?: DexFormat.API_NO_EXTENDED_OPCODES
        val args = Main.Arguments().apply {
            numThreads = CommUtils.availableCPUCount
            debug = true
            warnings = true
            emptyOk = true
            multiDex = true
            jarOutput = true
            optimize = false
            minSdkVersion = minSdk
            fileNames = arrayOf(inputFile.canonicalPath)
            outName = outputFile.canonicalPath
        }
        return try {
            Main.run(args)
        } catch (t: Throwable) {
            t.printStackTrace()
            -1
        }
    }

}