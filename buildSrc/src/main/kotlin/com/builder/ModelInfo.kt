package com.builder

/**
 *
 * Created by wswenyue on 2022/2/9.
 */
data class ModelInfo(
    val name: String,
    val resUri: String,
    var modelType: ModelType = ModelType.SOURCE
) {
    val projectName: String by lazy {
        if (name.startsWith(":")) {
            name
        } else {
            ":${name}"
        }
    }

}
