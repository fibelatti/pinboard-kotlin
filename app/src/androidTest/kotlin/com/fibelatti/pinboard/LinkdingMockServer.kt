package com.fibelatti.pinboard

import java.util.UUID
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest

object LinkdingMockServer {

    val instance: MockWebServer by lazy { MockWebServer().also { it.start() } }

    fun setResponses(vararg responses: Triple<String, String, (RecordedRequest) -> MockResponse>) {
        instance.dispatcher = object : Dispatcher() {
            private val handlers = responses.toList()

            override fun dispatch(request: RecordedRequest): MockResponse {
                val requestPath = request.url.toString()
                val requestMethod = request.method
                val handler = handlers.firstOrNull { (path, method, _) ->
                    requestPath.contains(path) && requestMethod == method
                }?.third

                return handler?.invoke(request) ?: MockResponse(code = 404)
            }
        }
    }

    fun allBookmarksResponse(
        isEmpty: Boolean,
    ): Triple<String, String, (RecordedRequest) -> MockResponse> {
        return Triple("bookmarks", "GET") { request ->
            MockResponse(code = 200)
                .newBuilder()
                .setHeader("Content-Type", "application/json")
                .apply {
                    when {
                        isEmpty -> body(TestData.emptyBookmarksResponse())

                        request.url.toString().run { contains("limit=1") || contains("offset=0") } -> {
                            body(TestData.bookmarksResponse())
                        }

                        else -> body(TestData.emptyBookmarksResponse())
                    }
                }
                .build()
        }
    }

    fun addBookmarkResponse(): Triple<String, String, (RecordedRequest) -> MockResponse> {
        return Triple("bookmarks", "POST") {
            MockResponse(code = 200)
                .newBuilder()
                .setHeader("Content-Type", "application/json")
                .body(TestData.addBookmarkResponse())
                .build()
        }
    }

    object TestData {

        val TOKEN: String = UUID.randomUUID().toString()

        fun bookmarksResponse(): String = """
            {
                "count":1,
                "next":null,
                "previous":null,
                "results":[
                    {
                        "id":1,
                        "url":"https://www.google.com",
                        "title":"",
                        "description":"",
                        "notes":"",
                        "website_title":"Google",
                        "website_description":"Instrumented test",
                        "web_archive_snapshot_url":"",
                        "is_archived":false,
                        "unread":true,
                        "shared":false,
                        "tag_names":["kotlin","dev"],
                        "date_added":"2024-06-16T18:54:56.250477Z",
                        "date_modified":"2024-06-16T19:18:37.075286Z"
                    }
                ]
            }
        """.trimIndent()

        fun emptyBookmarksResponse(): String = """
            {
                "count":0,
                "next":null,
                "previous":null,
                "results":[]
            }
        """.trimIndent()

        fun addBookmarkResponse(): String = """
            {
                "id":1,
                "url":"https://www.google.com",
                "title":"Google",
                "description":"Instrumented test",
                "notes":"",
                "website_title":"Google",
                "website_description":null,
                "web_archive_snapshot_url":"",
                "is_archived":false,
                "unread":true,
                "shared":false,
                "tag_names":["kotlin","dev"],
                "date_added":"2024-06-16T20:37:53.137730Z",
                "date_modified":"2024-06-16T20:37:53.137731Z"
            }
        """
    }
}
