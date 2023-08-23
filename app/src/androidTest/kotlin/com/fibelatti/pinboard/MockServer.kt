package com.fibelatti.pinboard

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

object MockServer {

    val instance = MockWebServer()

    fun loginResponses(updateTimestamp: String) {
        setResponses(
            "/posts/update" to {
                MockResponse().setResponseCode(200)
                    .setBody(TestData.updateResponse(timestamp = updateTimestamp))
            },
            "/posts/all" to { request ->
                MockResponse().setResponseCode(200).apply {
                    if (request.requestUrl.toString().contains("start=0")) {
                        setBody(TestData.allBookmarksResponse())
                    } else {
                        setBody(TestData.emptyBookmarksResponse())
                    }
                }
            },
        )
    }

    fun addBookmarkResponses(updateTimestamp: String) {
        setResponses(
            "/posts/update" to {
                MockResponse().setResponseCode(200)
                    .setBody(TestData.updateResponse(timestamp = updateTimestamp))
            },
            "/posts/all" to {
                MockResponse().setResponseCode(200)
                    .setBody(TestData.emptyBookmarksResponse())
            },
            "posts/add" to {
                MockResponse().setResponseCode(200)
                    .setBody(TestData.addBookmarkResponse())
            },
        )
    }

    private fun setResponses(vararg responses: Pair<String, (RecordedRequest) -> MockResponse>) {
        instance.dispatcher = object : Dispatcher() {
            private val handlers = responses.toList()

            override fun dispatch(request: RecordedRequest): MockResponse {
                val requestPath = request.path.orEmpty()
                val handler = handlers.firstOrNull { (path, _) -> requestPath.contains(path) }?.second

                return handler?.invoke(request) ?: MockResponse().setResponseCode(404)
            }
        }
    }

    object TestData {

        const val TOKEN = "instrumented:1000"

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

        fun addBookmarkResponse(): String = """
            {
                "result_code": "done"
            }
        """
    }
}
