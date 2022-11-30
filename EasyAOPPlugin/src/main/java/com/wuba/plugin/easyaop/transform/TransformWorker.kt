package com.wuba.plugin.easyaop.transform

import com.android.build.api.transform.*
import com.wuba.easy.log.Log
import com.wuba.easy.log.debug
import com.wuba.easy.log.error
import com.wuba.easy.log.info
import com.wuba.easy.utils.CommUtils
import com.wuba.easy.utils.copyToFile
import com.wuba.easy.utils.touch
import com.wuba.easyaop.asm.base.IClassHandler
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.plugin.easyaop.EasyAOPConstant
import com.wuba.plugin.easyaop.utils.DexChecker
import com.wuba.plugin.easyaop.utils.getApiLevel
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.parallel.InputStreamSupplier
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.*
import java.util.concurrent.*
import java.util.jar.JarFile
import java.util.zip.ZipFile

internal class TransformWorker(
    private val project: Project,
    private val delegate: TransformInvocation,
    private val handler: IClassHandler,
    private val verifyEnabled: Boolean = false,
    private val jarFilter: (File) -> Boolean = { false }
) : TransformInvocation {
    private val tag = "TransformWorker"
    private val mOutputRecord = CopyOnWriteArrayList<File>()
    private val mResModifyRecord = CopyOnWriteArraySet<ClassDesc.SourceRes>()

    fun doTransform() {
        mOutputRecord.clear()
        mResModifyRecord.clear()
        if (!isIncremental) {
            //全量编译
            outputProvider?.deleteAll()
        }
        runTransform()
        if (verifyEnabled) {
            doVerify()
        }
        handleResModifyRecord()
        mOutputRecord.clear()
        mResModifyRecord.clear()
    }

    private fun runInExecutor(block: (ExecutorService) -> Iterable<Future<*>>) {
        val executor = ThreadPoolExecutor(
            CommUtils.availableCPUCount, CommUtils.availableCPUCount * 2,
            10L, TimeUnit.SECONDS,
            LinkedBlockingQueue()
        )
        try {
            block(executor).forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.HOURS)
        }
    }

    private fun runTransform() = runInExecutor(this::transform)

    private fun transform(executor: ExecutorService) = inputs.map {
        it.jarInputs.map { jarInput ->
            Log.info(tag) { "Transform JarInput ==>${jarInput.file.absolutePath}" }
            executor.submit {
                jarInput.transform()
            }
        } + it.directoryInputs.map { dirInput ->
            Log.info(tag) { "Transform DirectoryInput ==>${dirInput.file.absolutePath}" }
            dirInput.transform(executor)
        }.flatten()
    }.flatten()

    private fun JarInput.transform() {
        Log.info(tag) { "(${Thread.currentThread().name})Exe JarInput Transform==>${file.absolutePath}" }
        file?.let { jarInFile ->
            if (isIncremental) {
                //增量
                when (status as Status) {
                    Status.REMOVED -> {
                        jarInFile.delete()
                        null
                    }
                    Status.CHANGED, Status.ADDED -> {
                        jarInFile
                    }
                    Status.NOTCHANGED -> {
                        null
                    }
                }
            } else {
                //全量
                jarInFile
            }
        }?.let { jarInFile ->
            outputProvider?.run {
                val jarOut = getContentLocation(name, contentTypes, scopes, Format.JAR)
                mOutputRecord.add(jarOut)
                JarFile(jarInFile).use {
                    it.transform(jarInFile, jarOut)
                }
            }
        }
    }


    private fun filterIncrementalDirChangedFiles(
        entry: Map.Entry<File?, Status?>,
        inputDir: File,
        outputDir: File
    ): Boolean {
        if (entry.key == null || entry.value == null) {
            return false
        }
        val inputFile = entry.key as File
        when (entry.value as Status) {
            Status.REMOVED -> {
                toOutputFile(outputDir, inputDir, inputFile)?.run {
                    FileUtils.deleteQuietly(this)
                }
                return false
            }
            Status.ADDED,
            Status.CHANGED -> {
                return true

            }
            Status.NOTCHANGED -> {
                return false
            }
        }
    }

    private fun DirectoryInput.transform(executor: ExecutorService): Iterable<Future<*>> {
        if (outputProvider == null) {
            return emptyList()
        }
        val outputDir =
            (outputProvider as TransformOutputProvider).getContentLocation(
                name,
                contentTypes,
                scopes,
                Format.DIRECTORY
            )
        mOutputRecord.add(outputDir)
        val inputDir = file

        val targetFiles = if (isIncremental) {
            if (changedFiles.isEmpty()) {
                Collections.emptyList<File>()
            } else {
                changedFiles.filter {
                    filterIncrementalDirChangedFiles(
                        it,
                        inputDir,
                        outputDir
                    )
                }.map { it.key }.toList()
            }

        } else {
            file.walk().toList()
        }

        return targetFiles.filter { it.isFile }
            .map { inFile ->
                executor.submit {
                    Log.info(tag) { "(t${Thread.currentThread().name})Exe DirectoryInput child file Transform==>${inFile.absolutePath}" }
                    toOutputFile(outputDir, inputDir, inFile)?.let { outputFile ->
                        when (inFile.extension.toLowerCase(Locale.getDefault())) {
                            "class" -> {
                                inFile.inputStream().use {
                                    it.transform(
                                        ClassDesc.buildWithSourceClassFile(
                                            inFile,
                                            inputDir
                                        )
                                    ).copyToFile(outputFile)

                                }
                                //返回值解决报错，无意义
//                                true
                            }
                            else -> {
                                inFile.copyTo(outputFile, true)
                                //返回值解决报错，无意义
//                                true
                            }
                        }
                    }
                }
            }
    }


    private fun ZipFile.transform(
        jarInputFile: File,
        jarOutputFile: File
    ) = jarOutputFile.touch().outputStream().buffered().use { output ->
        if (!jarFilter(jarInputFile)) {
            jarInputFile.inputStream().use {
                it.copyTo(output)
            }
        } else {
            transform(jarInputFile, jarOutputFile, output)
        }

    }

    private fun ZipFile.transform(
        jarInputFile: File,
        jarOutputFile: File,
        output: OutputStream
    ) {
        val entries = mutableSetOf<String>()
        val creator = ParallelScatterZipCreator(
            ThreadPoolExecutor(
                CommUtils.availableCPUCount,
                CommUtils.availableCPUCount,
                0L,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue<Runnable>(),
                Executors.defaultThreadFactory(),
                RejectedExecutionHandler { r, _ -> r?.run() }
            )
        )

        entries().asSequence().forEach { entry ->
            if (!entries.contains(entry.name)) {
                val zae = ZipArchiveEntry(entry)
                val stream = InputStreamSupplier {
                    when (entry.name.substringAfterLast('.', "")) {
                        "class" -> getInputStream(entry).use { src ->
                            try {
                                src.transform(
                                    ClassDesc.buildWithJarClassFile(
                                        entry.name,
                                        jarInputFile,
                                        jarOutputFile
                                    )
                                ).inputStream()
                            } catch (e: Throwable) {
                                Log.error(tag) { "Broken class: ${this.name}!/${entry.name} \n=>:${e.printStackTrace()}" }
                                getInputStream(entry)
                            }
                        }
                        else -> getInputStream(entry)
                    }
                }

                creator.addArchiveEntry(zae, stream)
                entries.add(entry.name)
            } else {
                Log.error(tag) { "Duplicated jar entry: ${this.name}!/${entry.name}" }
            }
        }

        ZipArchiveOutputStream(output).use(creator::writeTo)
    }

    private fun InputStream.transform(desc: ClassDesc): ByteArray {
        return readBytes().transform(desc)
    }

    private fun ByteArray.transform(desc: ClassDesc): ByteArray {
        val target = handler.doHandle(desc, this)
        if (target != null) {
            mResModifyRecord.add(desc.sourceRes)
            return target
        }
        return this
    }


    private fun relativePossiblyNonExistingPath(file: File, dir: File): String {
        val path = dir.toURI().relativize(file.toURI()).path
        return if (File.separatorChar != '/') {
            path.replace('/', File.separatorChar)
        } else {
            path
        }
    }

    private fun toOutputFile(outputDir: File?, inputDir: File?, inputFile: File?): File? {
        if (outputDir == null || inputDir == null || inputFile == null) {
            return null
        }
        val file = File(outputDir, relativePossiblyNonExistingPath(inputFile, inputDir))
        val parentFile = file.parentFile
        try {
            Files.createDirectories(parentFile.toPath())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }


    private fun doVerify() {
        val begin = System.currentTimeMillis()
        Log.debug(tag) { "============doVerify=======begin=====" }
        val api = project.getApiLevel()
        DexChecker(mOutputRecord, context.temporaryDir, api).doVerify()
        val duration = CommUtils.durationTimeFormat(
            System.currentTimeMillis() - begin
        )
        Log.debug(tag) { "============doVerify=======end===耗时:${duration}==" }
    }

    private fun handleResModifyRecord() {
        //jar modify to record file
        mResModifyRecord.filter { it.type == ClassDesc.SourceResType.JAR }
            .map { it.inName + EasyAOPConstant.defSeparator + it.toPath }
            .onEach {
                Log.getFileLogger(EasyAOPConstant.jarModifyFileLogPath)
                    .print(it, isOnlyPrintMsg = true)
            }
    }


    override fun getContext(): Context {
        return delegate.context
    }

    override fun getInputs(): MutableCollection<TransformInput> {
        return delegate.inputs
    }

    override fun getReferencedInputs(): MutableCollection<TransformInput> {
        return delegate.referencedInputs
    }

    override fun getSecondaryInputs(): MutableCollection<SecondaryInput> {
        return delegate.secondaryInputs
    }

    override fun getOutputProvider(): TransformOutputProvider? {
        return delegate.outputProvider
    }

    override fun isIncremental(): Boolean {
        return delegate.isIncremental
    }

}