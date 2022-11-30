package com.wuba.easyaop.bean

import com.wuba.easy.utils.CommUtils

/**
 *
 * Created by wswenyue on 2021/12/17.
 */
class MethodInfo(
    val methodName: String,
    val methodDesc: String? = null,
    var classDesc: ClassDesc? = null
) {
    companion object {
        private const val defSeparator = "##"
        fun build(methodStr: String, separator: String = defSeparator): MethodInfo {
            if (CommUtils.isEmpty(methodStr)) {
                throw RuntimeException("method String is empty!!!")
            }
            //com/wuba/PermissionCheckerProxy##b2##(Landroid/content/Context;I)V
            if (!methodStr.contains(separator)) {
                return MethodInfo(methodName = methodStr)
            }
            val mList = methodStr.split(separator)
            when (mList.size) {
                3 -> {
                    val classPath = mList[0]
                    val methodName = mList[1]
                    val methodDesc = mList[2]
                    if (CommUtils.isEmpty(classPath)
                        || CommUtils.isEmpty(methodName)
                        || CommUtils.isEmpty(methodDesc)
                    ) {
                        throw RuntimeException("classPath or methodName or methodDesc is empty!!!")
                    }
                    return MethodInfo(
                        methodName = methodName,
                        classDesc = ClassDesc.buildWithUnknownPath(classPath),
                        methodDesc = methodDesc
                    )
                }
                2 -> {
                    val methodName = mList[0]
                    val methodDesc = mList[1]
                    if (CommUtils.isEmpty(methodName) || CommUtils.isEmpty(methodDesc)) {
                        throw RuntimeException("methodName or methodDesc is empty!!!")
                    }
                    return MethodInfo(
                        methodName = methodName,
                        methodDesc = methodDesc
                    )
                }
                else -> {
                    throw RuntimeException("method string format error!!!==>${methodStr}")
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MethodInfo

        if (methodName != other.methodName) return false
        if (classDesc != other.classDesc) return false
        if (methodDesc != other.methodDesc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = methodName.hashCode()
        result = 31 * result + (classDesc?.hashCode() ?: 0)
        result = 31 * result + (methodDesc?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        if (classDesc != null) {
            return "${classDesc}${defSeparator}${methodName}${defSeparator}${methodDesc}"
        }
        return "${methodName}${defSeparator}${methodDesc}"
    }


}