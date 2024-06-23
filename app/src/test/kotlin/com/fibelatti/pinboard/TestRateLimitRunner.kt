package com.fibelatti.pinboard

import com.fibelatti.bookmarking.core.network.RateLimitRunner

class TestRateLimitRunner : RateLimitRunner {

    override suspend fun <T> run(body: suspend () -> T): T = body()

    override suspend fun <T> run(throttleTime: Long, body: suspend () -> T): T = body()
}
