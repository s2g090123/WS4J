package jiachian.lexical_db.item

enum class Link(val id: String, val symbol: String? = null) {
    ANTONYM("Antonym", "!"),
    ATTRIBUTE("Attribute", "="),
    CAUSE("Cause", ">"),
    ENTAILMENT("Entailment", "*"),
    HYPERNYM("Hypernym", "@"),
    HYPONYM("Hyponym", "~"),
    HOLONYM("Holonym"),
    HOLONYM_MEMBER("Member Holonym", "#m"),
    HOLONYM_SUBSTANCE("Substance Holonym", "#s"),
    HOLONYM_PART("Part Holonym", "#p"),
    MERONYM("Meronym"),
    MERONYM_MEMBER("Member Meronym", "%m"),
    MERONYM_SUBSTANCE("Substance Meronym", "%s"),
    MERONYM_PART("Part Meronym", "%p"),
    SIMILAR_TO("Similar To", "&"),
    SYNSET("Synset Words");

    override fun toString(): String {
        return id
    }
}