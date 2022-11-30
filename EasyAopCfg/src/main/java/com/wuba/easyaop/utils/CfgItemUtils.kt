package com.wuba.easyaop.utils

import com.wuba.easy.log.Log
import com.wuba.easy.log.warn
import com.wuba.easy.utils.CommUtils
import com.wuba.easyaop.bean.MethodInfo

/**
 *
 * Created by wswenyue on 2021/12/26.
 */
object CfgItemUtils {

    /**
     *  从 sourceMethodsMap 中获取匹配的MethodInfo
     * @param sourceMethodsMap HashMap<String, ArrayList<MethodInfo>>
     * @param methodName String
     * @param methodDesc String
     * @return MethodInfo?
     */
    fun getMatchedMethodInfo(
        sourceMethodsMap: HashMap<String, ArrayList<MethodInfo>>,
        methodName: String?,
        methodDesc: String?
    ): MethodInfo? {
        val info = sourceMethodsMap[methodName] ?: return null
        if (info.size == 1 && CommUtils.isEmpty(info[0].methodDesc)) {
            //全匹配
            return info[0]
        }
        //精准匹配
        info.onEach {
            if (it.methodDesc == methodDesc) {
                return it
            }
        }
        //没有匹配上
        Log.warn {
            "方法名匹配，签名不匹配 ==>${methodName}##${methodDesc}"
        }
        return null
    }


    fun methodInfoListToMap(methodItems: Iterable<String>?): HashMap<String, ArrayList<MethodInfo>> {
        return HashMap<String, ArrayList<MethodInfo>>().apply {
            methodItems?.filter { CommUtils.isNotEmpty(it) }
                ?.map {
                    MethodInfo.build(it)
                }?.onEach {
                    var methods = get(it.methodName)
                    if (methods == null) {
                        methods = ArrayList<MethodInfo>()
                        methods.add(it)
                    } else {
                        checkInValid(methods, it)
                        methods.add(it)
                    }
                    put(it.methodName, methods)
                }
        }
    }

    fun checkInValid(methods: List<MethodInfo>, info: MethodInfo) {
        if (CommUtils.isEmpty(info.methodDesc)) {//methods 已经有值了
            throw RuntimeException("同一个方法如果配置了方法描述，所有该方法都需要配置方法描述，不能省略!!!==>${info}")
        }
        methods.onEach {
            if (CommUtils.isEmpty(it.methodDesc)) {
                throw RuntimeException("同一个方法如果配置了方法描述，所有该方法都需要配置方法描述，不能省略!!!==>${it}")
            }
            if (it.methodDesc == info.methodDesc) {
                throw RuntimeException("同一个方法描述不能重复!!!==>${info}")
            }
        }
    }
}