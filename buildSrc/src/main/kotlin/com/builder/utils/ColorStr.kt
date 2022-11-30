package com.builder.utils

/**
 *
 * Created by wswenyue on 2021/12/24.
 */
enum class ColorStr(private val ansiBegin: String) {
    Red("\u001b[31m"),
    Green("\u001b[32m"),
    Yellow("\u001b[33m"),
    Blue("\u001b[34m"),
    Magenta("\u001b[35m"),
    Cyan("\u001b[36m"),
    White("\u001b[37m");

    private val ansiEnd = "\u001B[0m"
    fun build(msg: String): AnsiColorLabel {
        return AnsiColorLabel(msg, ansiBegin, ansiEnd)
    }
}

fun String.redLabel() = ColorStr.Red.build(this)
fun String.red() = ColorStr.Red.build(this).toString()
fun String.greenLabel() = ColorStr.Green.build(this)
fun String.green() = ColorStr.Green.build(this).toString()
fun String.yellowLabel() = ColorStr.Yellow.build(this)
fun String.yellow() = ColorStr.Yellow.build(this).toString()
fun Iterable<Any>.toAnsiLabels() = AnsiGroupLabel.buildWithIterable(this)


interface IAnsiLabel {
    override fun toString(): String
    fun originSource(): String
}

class StringLabel(private val source: String) : IAnsiLabel {
    override fun toString(): String {
        return source
    }

    override fun originSource(): String {
        return source
    }

}

class AnsiColorLabel(
    private val source: String,
    private val begin: String,
    private val end: String
) : IAnsiLabel {
    override fun toString(): String {
        return begin + source + end
    }

    override fun originSource(): String {
        return source
    }
}

class AnsiGroupLabel(val labelList: List<IAnsiLabel>) : IAnsiLabel {
    companion object {
        fun buildWithIterable(iterable: Iterable<Any>): AnsiGroupLabel {
            return AnsiGroupLabel(
                iterable.map {
                    if (it is IAnsiLabel)
                        it
                    else
                        StringLabel(it.toString())
                }.toList()
            )
        }

        fun build(vararg items: Any): AnsiGroupLabel {
            return AnsiGroupLabel(items.map {
                if (it is IAnsiLabel)
                    it
                else
                    StringLabel(it.toString())
            }.toList())
        }
    }

    override fun toString(): String {
        val content: StringBuilder = StringBuilder()
        labelList.onEach {
            content.append(it.toString())
        }
        return content.toString()
    }

    override fun originSource(): String {
        val content: StringBuilder = StringBuilder()
        labelList.onEach {
            content.append(it.originSource())
        }
        return content.toString()
    }
}

private fun doLabelFlatten(label: IAnsiLabel, base: ColorStr? = null): Iterable<IAnsiLabel> {
    return when (label) {
        is AnsiGroupLabel -> {
            label.labelList.map { doLabelFlatten(it, base) }.flatten()
        }
        is StringLabel -> {
            if (base != null) {
                listOf(base.build(label.toString()))
            } else {
                listOf(label)
            }
        }
        else -> {
            listOf(label)
        }
    }
}


fun IAnsiLabel.labelFlatten(base: ColorStr? = null): Iterable<IAnsiLabel> {
    return doLabelFlatten(this, base)
}

