package com.wuba.easyaop.cfg.warp

import com.wuba.easy.log.Log
import com.wuba.easy.log.warn
import com.wuba.easy.utils.CommUtils
import com.wuba.easyaop.bean.ClassDesc
import com.wuba.easyaop.bean.IMatchable
import com.wuba.easyaop.bean.MatchableHelper
import com.wuba.easyaop.bean.MethodInfo
import com.wuba.easyaop.cfg.ProxyItem
import com.wuba.easyaop.error.EasyAOPCfgError
import com.wuba.easyaop.utils.CfgItemUtils
import com.wuba.easyaop.utils.matchOnly
import com.wuba.easyaop.utils.matchSkip

/**
 *
 * Created by wswenyue on 2021/12/20.
 */
class ProxyItemWarp(val targetClassDesc: ClassDesc, private val source: ProxyItem) {
    val proxyClassDesc: ClassDesc by lazy {
        ClassDesc.buildWithUnknownPath(source.proxyClass)
    }

    /**
     * 转换的目的是方便程序检索获取
     * 示例：用户配置如下
     * proxyItemsCfg:
     *    android.util.Log:
     *      enabled: false
     *      proxyClass: "com.XXX.XXProxy"
     *      methodMappingList:
     *          "v##s1": "v1"
     *          "v##s2": "v2"
     *
     * 转换后的结构：ProxyItemWarp
     * v->{v##s1:v1, v##s2:v2}
     */
    private val proxyMethodsMap: HashMap<String, HashMap<MethodInfo, MethodInfo>> by lazy {
        HashMap<String, HashMap<MethodInfo, MethodInfo>>().apply {
            source.methodMappingList?.filter {
                CommUtils.isNotEmpty(it.key) && CommUtils.isNotEmpty(it.value)
            }?.map {
                Pair<MethodInfo, MethodInfo>(MethodInfo.build(it.key).apply {
                    if (classDesc == null) {
                        classDesc = targetClassDesc
                    }
                }, MethodInfo.build(it.value).apply {
                    if (classDesc == null) {
                        //补充外层统一配置的classDesc
                        classDesc = proxyClassDesc
                    }
                })
            }?.onEach {
                val targetMethodInfo = it.first
                val proxyMethodInfo = it.second
                // map 是为了解决方法重名，签名描述不一样的情况
                var targetProxyTempMap = get(targetMethodInfo.methodName)
                if (targetProxyTempMap == null) {//方法名没有找到map，创建map
                    targetProxyTempMap = HashMap<MethodInfo, MethodInfo>()
                    targetProxyTempMap[targetMethodInfo] = proxyMethodInfo
                } else {
                    //之前创建过map，检查invokeMethodInfo是否有效
                    CfgItemUtils.checkInValid(targetProxyTempMap.keys.toList(), targetMethodInfo)
                    targetProxyTempMap[targetMethodInfo] = proxyMethodInfo
                }
                //put 到 map中
                put(targetMethodInfo.methodName, targetProxyTempMap)
            }
        }
    }

    /**
     * 转换后的结构：ProxyItemWarp
     * v->{v##s1:v1, v##s2:v2}
     */
    fun foundProxyMethodInfo(targetMethodInfo: MethodInfo): MethodInfo? {
        val targetProxyMethodMap = proxyMethodsMap[targetMethodInfo.methodName] ?: return null
        if (targetProxyMethodMap.isEmpty()) {
            return null
        }
        if (targetProxyMethodMap.size == 1 && CommUtils.isEmpty(targetProxyMethodMap.keys.first().methodDesc)) {
            //一个方法名下的配置，如果只有一项，并且描述是空，那么就是全匹配(模糊匹配)
            return targetProxyMethodMap.values.first()
        }
        //精准匹配
        val proxyMethodInfo = targetProxyMethodMap[targetMethodInfo]
        if (proxyMethodInfo == null) {
            //没有匹配上
            Log.warn {
                "Proxy 方法名匹配，签名描述不匹配 ==>${targetMethodInfo}"
            }
        }

        return proxyMethodInfo
    }

    private val skipClazz: Set<IMatchable>? by lazy {
        source.skipClazz?.map { MatchableHelper.create(it) }?.toSet()
    }
    private val onlyClazz: Set<IMatchable>? by lazy {
        source.onlyClazz?.map { MatchableHelper.create(it) }?.toSet()
    }


    /**
     * 判断给定的class是否匹配当前配置
     * 如果设置了白名单，仅判断白名单，不在白名单中的不匹配
     * @param visitorClazz ClassDesc
     * @return Boolean
     */
    fun isMatchCfg(visitorClazz: ClassDesc): Boolean {
        if (onlyClazz?.isNotEmpty() == true) {
            //如果设置了白名单，就只判断白名单
            return onlyClazz!!.find { match -> match.onMatch(visitorClazz.classFullName) }.let {
                if (it != null) {
                    Log.matchOnly(
                        it.pattern, visitorClazz.classFullName, ext = "Proxy(${targetClassDesc})"
                    )
                    true
                } else {
                    false
                }
            }
        }
        //没有设置白名单，看黑名单
        return skipClazz?.find { match -> match.onMatch(visitorClazz.classFullName) }.let {
            if (it != null) {
                Log.matchSkip(
                    it.pattern, visitorClazz.classFullName, ext = "Proxy(${targetClassDesc})"
                )
                //黑名单命中
                false
            } else {
                true
            }
        }
    }


    private fun checkInValid() {
        if (onlyClazz != null && onlyClazz!!.isNotEmpty() && skipClazz != null && skipClazz!!.isNotEmpty()) {
            //项目中配置了白名单和黑名单
            EasyAOPCfgError.throwError("proxyItemsCfg项目[${targetClassDesc}]配置了OnlyClazz，SkipClazz失效。请将SkipClazz移除或者注释!!!")
        }
    }


    init {
        checkInValid()
    }

}