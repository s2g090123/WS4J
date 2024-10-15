package jiachian.ws4j.util

class WS4JConfiguration private constructor() {
    private var trace = false
    private var cache = false
    private var memoryDB = false
    private var stem = false
    private var leskNormalize = false
    private var mfs = false

    fun useCache(): Boolean {
        return cache
    }

    fun setCache(cache: Boolean) {
        this.cache = cache
    }

    fun setTrace(trace: Boolean) {
        this.trace = trace
    }

    fun useTrace(): Boolean {
        return trace
    }

    fun useMemoryDB(): Boolean {
        return memoryDB
    }

    fun setMemoryDB(memoryDB: Boolean) {
        this.memoryDB = memoryDB
    }

    fun useStem(): Boolean {
        return stem
    }

    fun setStem(stem: Boolean) {
        this.stem = stem
    }

    fun useLeskNormalizer(): Boolean {
        return leskNormalize
    }

    fun setLeskNormalize(leskNormalize: Boolean) {
        this.leskNormalize = leskNormalize
    }

    fun useMFS(): Boolean {
        return mfs
    }

    fun setMFS(mfs: Boolean) {
        this.mfs = mfs
    }

    companion object {
        private val instance = WS4JConfiguration()

        fun getInstance(): WS4JConfiguration = instance
    }
}
