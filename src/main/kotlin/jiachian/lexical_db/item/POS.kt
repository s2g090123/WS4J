package jiachian.lexical_db.item

enum class POS(val id: String, val tag: Char) {
    NOUN("noun", 'n'),
    VERB("verb", 'v'),
    ADJECTIVE("adjective", 'a'),
    ADVERB("adverb", 'r');

    override fun toString(): String {
        return tag.toString()
    }

    companion object {
        private const val NOUN_TAG = 'n'
        private const val VERB_TAG = 'v'
        private const val ADJECTIVE_TAG = 'a'
        private const val ADVERB_TAG = 'r'

        fun getPOS(tag: Char): POS? {
            return when (tag) {
                NOUN_TAG -> NOUN
                VERB_TAG -> VERB
                ADJECTIVE_TAG -> ADJECTIVE
                ADVERB_TAG -> ADVERB
                else -> null
            }
        }
    }
}