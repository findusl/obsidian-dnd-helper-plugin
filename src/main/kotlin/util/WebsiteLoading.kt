package util

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import org.w3c.dom.Document
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.url.URL
import org.w3c.fetch.Request

private const val prefixLocalCorsAvoidance = "http://localhost:8010/proxy"

open class WebsiteLoader {

    /**
     * Pass full URL, the host is removed to call the CORS avoidance proxy.
     */
    internal suspend fun loadWebsite(fullUrl: String): Document {
        val proxiedUrl = URL(fullUrl)
        console.log("Url before proxy $proxiedUrl")
        proxiedUrl.protocol = "http"
        proxiedUrl.host = "localhost:8010"
        proxiedUrl.pathname = "/proxy" + proxiedUrl.pathname
        return loadWebsiteProxied(proxiedUrl.toString())
    }

    /**
     * Load the website under the given <relativeUrl> with the host defined by the current proxy.
     * Do not pass the host as part of the url parameter. The url parameter value usually starts with a slash.
     */
    internal suspend fun loadRelativeWebsite(relativeUrl: String): Document {
        val proxiedURL = prefixLocalCorsAvoidance + relativeUrl
        return loadWebsiteProxied(proxiedURL)
    }

    private suspend fun loadWebsiteProxied(proxiedUrl: String): Document {
        console.log("Loading website $proxiedUrl")
        val response = window.fetch(Request(proxiedUrl)).await()
        val htmlText = response.text().await()
        return DOMParser().parseFromString(htmlText, "text/html")
    }
}
