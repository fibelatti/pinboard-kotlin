package com.fibelatti.core

public actual fun randomUUID(): String = java.util.UUID.randomUUID().toString()
