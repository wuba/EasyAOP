package com.wuba.easy.utils

import com.wuba.easy.log.green
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 *
 * Created by wswenyue on 2021/12/21.
 */

/**
 * create a file
 * @receiver File
 * @return File
 */
fun File.touch(): File {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}


/**
 * copyToFile
 * @receiver InputStream
 * @param file File
 * @return Long
 */
fun InputStream.copyToFile(file: File): Long = file.touch().outputStream().use { this.copyTo(it) }

/**
 * copyToFile
 * @receiver ByteArray
 * @param file File
 * @return Long
 */
fun ByteArray.copyToFile(file: File): Long = this.inputStream().use { it.copyToFile(file) }

/**
 * check a file is InValidFile
 * @receiver File
 * @return Boolean
 */
fun File.isInValidFile(): Boolean {
    if (!exists()) {
        return true
    }
    if (!isFile) {
        return true
    }
    return false
}

/**
 * 给定各类条件，使用当前zip，创建新的zip
 * @receiver ZipFile
 * @param outZipFile File
 * @param itemFilter Function1<ZipEntry, Boolean> 可以用于删除zip中entry
 * @param replaceItemsMap HashMap<String, File>? 可以用于替换zip中文件
 * @return File 新ZipFile
 */
fun ZipFile.createNewZip(
    outZipFile: File,
    itemFilter: (ZipEntry) -> Boolean = { true },
    replaceItemsMap: HashMap<String, File>? = null
): File {
    if (outZipFile.exists()) {
        throw  RuntimeException("outZip file exists!!! ==>${outZipFile.absolutePath}")
    }

    if (!outZipFile.parentFile.exists()) {
        outZipFile.parentFile.mkdirs()
    }
    ZipOutputStream(FileOutputStream(outZipFile)).use { zipOut ->
        entries().toList().onEach { entry ->
            val name = entry.name
            if (itemFilter(entry)) {
//                println("itemFilter accept ==>$name")
                if (replaceItemsMap != null && replaceItemsMap.containsKey(name)) {
                    println("Zip Replace==>${name}".green())
                    FileInputStream(replaceItemsMap[name] as File)
                } else {
//                    println("Zip Using==>${name}")
                    getInputStream(entry)
                }.use { dataStream ->
                    val newEntry = ZipEntry(name)
                    zipOut.putNextEntry(newEntry)
                    dataStream.copyTo(zipOut)
                    zipOut.closeEntry()
                }
            } else {
                println("itemFilter Not accept ==>$name".green())
            }

        }
    }
    return outZipFile
}
