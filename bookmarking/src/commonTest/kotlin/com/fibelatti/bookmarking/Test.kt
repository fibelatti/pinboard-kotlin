package com.fibelatti.bookmarking

fun randomBoolean(): Boolean = listOf(true, false).random()

fun randomInt(): Int = kotlin.random.Random.nextInt()

private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomString(): String = List(charPool.size) { kotlin.random.Random.nextInt(from = 0, until = charPool.size) }
    .map(charPool::get)
    .joinToString("")
