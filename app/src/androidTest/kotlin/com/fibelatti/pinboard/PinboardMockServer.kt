package com.fibelatti.pinboard

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest

object PinboardMockServer {

    val instance: MockWebServer by lazy { MockWebServer().also { it.start() } }

    fun setResponses(vararg responses: Pair<String, (RecordedRequest) -> MockResponse>) {
        instance.dispatcher = object : Dispatcher() {
            private val handlers = responses.toList()

            override fun dispatch(request: RecordedRequest): MockResponse {
                val requestPath = request.url.toString()
                val handler = handlers.firstOrNull { (path, _) -> requestPath.contains(path) }?.second

                return handler?.invoke(request) ?: MockResponse(code = 404)
            }
        }
    }

    fun updateResponse(
        updateTimestamp: String,
    ): Pair<String, (RecordedRequest) -> MockResponse> {
        return "/posts/update" to {
            MockResponse(code = 200)
                .newBuilder()
                .setHeader("Content-Type", "application/json")
                .body(TestData.updateResponse(timestamp = updateTimestamp))
                .build()
        }
    }

    fun allBookmarksResponse(
        isEmpty: Boolean,
    ): Pair<String, (RecordedRequest) -> MockResponse> {
        return "/posts/all" to { request ->
            MockResponse(code = 200)
                .newBuilder()
                .setHeader("Content-Type", "application/json")
                .apply {
                    when {
                        isEmpty -> body(TestData.emptyBookmarksResponse())
                        request.url.toString().contains("start=0") -> body(TestData.allBookmarksResponse())
                        else -> body(TestData.emptyBookmarksResponse())
                    }
                }
                .build()
        }
    }

    fun addBookmarkResponse(): Pair<String, (RecordedRequest) -> MockResponse> {
        return "posts/add" to {
            MockResponse(code = 200)
                .newBuilder()
                .setHeader("Content-Type", "application/json")
                .body(TestData.genericResponseDone())
                .build()
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
