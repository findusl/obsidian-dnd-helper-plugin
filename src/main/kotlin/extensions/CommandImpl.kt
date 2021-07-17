package extensions

import Command

data class CommandImpl (
    override var id: String,
    override var name: String,
    override var icon: String? = null,
    override var mobileOnly: Boolean? = null,
    override var callback: (() -> Unit)? = null,
    override var checkCallback: ((checking: Boolean) -> Boolean)? = null
): Command
