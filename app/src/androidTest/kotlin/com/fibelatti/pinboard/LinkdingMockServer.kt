package com.fibelatti.pinboard

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.UUID

object LinkdingMockServer {

    val instance by lazy { MockWebServer() }

    fun setResponses(vararg responses: Triple<String, String, (RecordedRequest) -> MockResponse>) {
        instance.dispatcher = object : Dispatcher() {
            private val handlers = responses.toList()

            override fun dispatch(request: RecordedRequest): MockResponse {
                val requestPath = request.path.orEmpty()
                val requestMethod = request.method.orEmpty()
                val handler = handlers.firstOrNull { (path, method, _) ->
                    requestPath.contains(path) && requestMethod == method
                }?.third

                return handler?.invoke(request) ?: MockResponse().setResponseCode(404)
            }
        }
    }

    fun allBookmarksResponse(
        isEmpty: Boolean,
    ): Triple<String, String, (RecordedRequest) -> MockResponse> {
        return Triple("bookmarks", "GET") { request ->
            MockResponse().setResponseCode(200).apply {
                when {
                    isEmpty -> setBody(TestData.emptyBookmarksResponse())
                    request.requestUrl.toString().run { contains("limit=1") || contains("offset=0") } -> {
                        setBody(TestData.bookmarksResponse())
                    }

                    else -> setBody(TestData.emptyBookmarksResponse())
                }
            }
        }
    }

    fun addBookmarkResponse(): Triple<String, String, (RecordedRequest) -> MockResponse> {
        return Triple("bookmarks", "POST") {
            MockResponse().setResponseCode(200)
                .setBody(TestData.addBookmarkResponse())
        }
    }

    object TestData {

        val TOKEN = UUID.randomUUID().toString()

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
