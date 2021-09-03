package ui

import dependencies.App
import dependencies.Modal
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class InputModal(app: App): Modal(app) {

    private var textHTMLInputElement: HTMLInputElement? = null
    private var continuation: CancellableContinuation<String>? = null

    suspend fun openForResult(): String {
        open()
        return suspendCancellableCoroutine { continuation = it  }
    }

    override fun onOpen() {
        contentEl.append {
            div(classes = "modal-title") {
                text("Import Website")
            }
            div(classes = "modal-content") {
                p {
                    text("Currently the tool only supports kassoon.com and needs a CORS proxy running.")
                }
                div(classes = "document-search") {
                    textHTMLInputElement = input(type = InputType.url, classes = "document-search-input") {
                        placeholder = "https://www.kassoon.com/dnd/town-generator/10/518707/"
                        this.onKeyDownFunction = {
                            if (it is KeyboardEvent && it.key == "Enter") {
                                successHandler()
                            }
                        }
                        autoFocus = true
                    }
                }
            }
            div(classes = "modal-button-container") {
                button(classes = "mod-cta") {
                    text("OK")
                    onClickFunction = {
                        successHandler()
                    }
                }
                button(classes = "mod-cta") {
                    text("Cancel")
                    onClickFunction = {
                        this@InputModal.close()
                    }
                }
            }
        }
    }

    private fun successHandler() {
        continuation?.resume(textHTMLInputElement?.value ?: "")
        continuation = null
        this@InputModal.close()
    }

    override fun onClose() {
        textHTMLInputElement = null
        continuation?.cancel()
        continuation = null
    }

}
