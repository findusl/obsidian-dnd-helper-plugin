package util

import org.w3c.dom.Element

fun Element.isH2(): Boolean = "H2".equals(tagName, ignoreCase = true)

fun Element.isParagraph(): Boolean = "P".equals(tagName, ignoreCase = true)
