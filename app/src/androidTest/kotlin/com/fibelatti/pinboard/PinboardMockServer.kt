package com.fibelatti.pinboard

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

object PinboardMockServer {

    val instance: MockWebServer by lazy { MockWebServer() }

    fun setResponses(vararg responses: Pair<String, (RecordedRequest) -> MockResponse>) {
        instance.dispatcher = object : Dispatcher() {
            private val handlers = responses.toList()

            override fun dispatch(request: RecordedRequest): MockResponse {
                val requestPath = request.path.orEmpty()
                val handler = handlers.firstOrNull { (path, _) -> requestPath.contains(path) }?.second

                return handler?.invoke(request) ?: MockResponse().setResponseCode(404)
            }
        }
    }

    fun updateResponse(
        updateTimestamp: String,
    ): Pair<String, (RecordedRequest) -> MockResponse> {
        return "/posts/update" to {
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(TestData.updateResponse(timestamp = updateTimestamp))
        }
    }

    fun allBookmarksResponse(
        isEmpty: Boolean,
    ): Pair<String, (RecordedRequest) -> MockResponse> {
        return "/posts/all" to { request ->
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .apply {
                    when {
                        isEmpty -> setBody(TestData.emptyBookmarksResponse())
                        request.requestUrl.toString().contains("start=0") -> setBody(TestData.allBookmarksResponse())
                        else -> setBody(TestData.emptyBookmarksResponse())
                    }
                }
        }
    }

    fun addBookmarkResponse(): Pair<String, (RecordedRequest) -> MockResponse> {
        return "posts/add" to {
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(TestData.genericResponseDone())
        }
    }

    object TestData {

        const val TOKEN: String = "instrumented:1000"

        fun updateResponse(timestamp: String): String = """
            {
                "update_time":"$timestamp"
            }
        """.trimIndent()

        fun allBookmarksResponse(): String = """
            [
                {
                    "href":"https:\/\/www.google.com",
                    "description":"Google",
                    "extended":"Instrumented test",
                    "meta":"d0ec3d2af45baa4365121cacab6166d3",
                    "hash":"8ffdefbdec956b595d257f0aaeefd623",
                    "time":"2023-08-05T11:51:33Z",
                    "shared":"no",
                    "toread":"yes",
                    "tags":"android dev"
                }
            ]
        """.trimIndent()

        fun emptyBookmarksResponse(): String = "[]"

        fun genericResponseDone(): String = """{"result_code":"done"}"""
    }
}
