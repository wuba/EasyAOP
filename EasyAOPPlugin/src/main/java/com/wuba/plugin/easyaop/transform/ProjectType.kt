package com.wuba.plugin.easyaop.transform

/**
 *
 * Created by wswenyue on 2021/12/18.
 */
enum class ProjectType {
    /**
     * Application: com.android.application
     * 或者 dynamic-feature:  com.android.dynamic-feature
     */
    APP,

    /**
     * Library
     * com.android.library
     */
    Library,
    None
}