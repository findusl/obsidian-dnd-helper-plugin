package util

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.Document
import org.w3c.dom.parsing.DOMParser
import org.w3c.fetch.Request

private const val prefixLocalCorsAvoidance = "http://localhost:8010/proxy"

open class WebsiteLoader {

    /**
     * Load the website under Kassoon.com/<relativeUrl>. Do not pass the host as part of the url parameter.
     * The url parameter value usually starts with a slash.
     */
    internal suspend fun loadKassoonWebsite(relativeUrl: String): Document {
        val fullUrl = prefixLocalCorsAvoidance + relativeUrl
        console.log("Loading website $fullUrl")
        val response = window.fetch(Request(fullUrl)).await()
        val htmlText = response.text().await()
        return DOMParser().parseFromString(htmlText, "text/html")
    }
}
