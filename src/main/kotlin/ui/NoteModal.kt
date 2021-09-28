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

class NoteModal(app: App, private val title: String = "Notice", private val text: String): Modal(app) {

    override fun onOpen() {
        contentEl.append {
            div(classes = "modal-title") {
                text(title)
            }
            div(classes = "modal-content") {
                p {
                    text(text)
                }
            }
            div(classes = "modal-button-container") {
                button(classes = "mod-cta") {
                    text("OK")
                    onClickFunction = {
                        close()
                    }
                    autoFocus = true
                }
            }
        }
    }
}
