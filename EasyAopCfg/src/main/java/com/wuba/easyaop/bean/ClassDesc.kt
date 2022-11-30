package com.wuba.easyaop.bean

import com.wuba.easy.utils.CommUtils
import java.io.File

/**
 *
 * Created by wswenyue on 2021/12/17.
 */
class ClassDesc
private constructor(
    val sourceRes: SourceRes,
    /**
     *  eg:com/wuba/plugin/easyaop/bean/ClassDesc.class
     */
    val classFullPath: String,
    /**
     * eg:com/wuba/plugin/easyaop/bean/ClassDesc
     */
    val classPath: String,
    /**
     * eg: com.wuba.easyaop.bean.ClassDesc.class
     */
    val classFullName: String,
    /**
     * eg: com.wuba.easyaop.bean.ClassDesc
     */
    val className: String
) {


    companion object {

        @JvmStatic
        fun buildWithSourceClassFile(classFile: File, rootDir: File): ClassDesc {
            val classFullPath =
                classFile.absolutePath.replace(rootDir.absolutePath + File.separator, "")
            val classPath = classFullPath.substringBeforeLast(".class", "")
            val classFullName = classFullPath.replace(File.separator, ".")
            val className = classPath.replace(File.separator, ".")

            if (CommUtils.isEmpty(classFullPath) ||
                CommUtils.isEmpty(classPath) ||
                CommUtils.isEmpty(classFullName) ||
                CommUtils.isEmpty(className)
            ) {
                throw RuntimeException("ClassDesc# buildWithSourceClassFile Failure==>${classFile.absolutePath}")
            }

            return ClassDesc(
                sourceRes = SourceRes(
                    type = SourceResType.SOURCE,
                    inPath = classFile.absolutePath,
                    inName = classFile.name,
                    //源码类型in和to一致
                    toPath = classFile.absolutePath,
                    toName = classFile.name
                ),
                classFullPath = classFullPath,
                classPath = classPath,
                classFullName = classFullName,
                className = className
            )
        }

        @JvmStatic
        fun buildWithJarClassFile(
            classFullPath: String,
            jarInFile: File,
            jarOutFile: File
        ): ClassDesc {
            val classPath = classFullPath.substringBeforeLast(".class", "")
            val classFullName = classFullPath.replace(File.separator, ".")
            val className = classPath.replace(File.separator, ".")

            if (CommUtils.isEmpty(classFullPath) ||
                CommUtils.isEmpty(classPath) ||
                CommUtils.isEmpty(classFullName) ||
                CommUtils.isEmpty(className)
            ) {
                throw RuntimeException("ClassDesc# buildWithJarClassFile Failure==>${classFullPath}")
            }
            return ClassDesc(
                sourceRes = SourceRes(
                    type = SourceResType.JAR,
                    inPath = jarInFile.absolutePath,
                    inName = jarInFile.name,
                    toPath = jarOutFile.absolutePath,
                    toName = jarOutFile.name
                ),
                classFullPath = classFullPath,
                classPath = classPath,
                classFullName = classFullName,
                className = className
            )
        }

        @JvmStatic
        fun buildWithUnknownPath(path: String): ClassDesc {
            if (CommUtils.isEmpty(path)) {
                throw RuntimeException("ClassDesc# buildWithUnknownPath path is empty!!!")
            }
            val classFullPath: String?
            val classPath: String?
            val classFullName: String?
            val className: String?

            if (path.contains(File.separator)) {
                if (path.endsWith(".class")) {
                    classFullPath = path
                    classPath = classFullPath.substringBeforeLast(".class", "")
                } else {
                    classPath = path
                    classFullPath = "${classPath}.class"
                }
                classFullName = classFullPath.replace(File.separator, ".")
                className = classPath.replace(File.separator, ".")

            } else {
                if (path.endsWith(".class")) {
                    classFullName = path
                    className = classFullName.substringBeforeLast(".class", "")
                } else {
                    className = path
                    classFullName = "${className}.class"
                }

                classPath = className.replace(".", File.separator)
                classFullPath = "${classPath}.class"
            }

            if (CommUtils.isEmpty(classFullPath) ||
                CommUtils.isEmpty(classPath) ||
                CommUtils.isEmpty(classFullName) ||
                CommUtils.isEmpty(className)
            ) {
                throw RuntimeException("ClassDesc# buildWithUnknownPath Failure==>${path}")
            }
            return ClassDesc(
                sourceRes = SourceRes(type = SourceResType.UNKNOWN),
                classFullPath = classFullPath,
                classPath = classPath,
                classFullName = classFullName,
                className = className
            )
        }

    }

    enum class SourceResType {
        SOURCE,
        JAR,
        UNKNOWN
    }

    data class SourceRes(
        val type: SourceResType,
        val inPath: String? = null,
        val inName: String? = null,
        val toPath: String? = null,
        val toName: String? = null
    ) {

        override fun toString(): String {
            return when (type) {
                SourceResType.JAR -> {
                    "(${type.name}::$inName=>$toName)"
                }
                SourceResType.SOURCE -> {
                    "(${type.name}::$inName)"
                }
                SourceResType.UNKNOWN -> {
                    type.name
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SourceRes

            if (type != other.type) return false
            if (inPath != other.inPath) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + (inPath?.hashCode() ?: 0)
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassDesc

        if (CommUtils.isEmpty(classFullName) || CommUtils.isEmpty(other.classFullName)) {
            return false
        }

        if (classFullName != other.classFullName) return false

        return true
    }

    override fun hashCode(): Int {
        return classFullName.hashCode()
    }

    override fun toString(): String {
        return classFullName
    }

}