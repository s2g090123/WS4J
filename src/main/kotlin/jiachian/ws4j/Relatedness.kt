package jiachian.ws4j

import jiachian.ws4j.util.WS4JConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Relatedness {
    var score: Double
    private var trace: StringBuffer
    private var error: StringBuffer

    constructor(score: Double) {
        this.score = score
        this.trace = StringBuffer()
        this.error = StringBuffer()
    }

    constructor(score: Double, trace: String?, error: String?) {
        this.score = score
        this.trace = StringBuffer(trace ?: "")
        this.error = StringBuffer(error ?: "")
        if (WS4JConfiguration.getInstance().useTrace() && this.trace.toString() != "") {
            val strs = trace?.split("\\R".toRegex())?.dropLastWhile { it.isEmpty() } ?: emptyList()
            for (str in strs) LOGGER.info(str)
        }
        if (WS4JConfiguration.getInstance().useTrace() && this.error.toString() != "") {
            LOGGER.error(error)
        }
    }

    fun getTrace(): String {
        return trace.toString()
    }

    fun appendTrace(trace: String?) {
        this.trace.append(trace)
    }

    fun getError(): String {
        return error.toString()
    }

    fun appendError(error: String?) {
        this.error.append(error)
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Relatedness::class.java)
    }
}