`:core` Module
=====

This module hosts common Kotlin code that can be used by any other module. The code is mostly functional and/or aims to
complement the standard library. Some good examples are:

* The [Either](./src/commonMain/kotlin/com/fibelatti/core/functional/Either.kt) class to represent a result type
* A standard [screen state](./src/commonMain/kotlin/com/fibelatti/core/functional/ScreenState.kt) definition
* A standard [use case](./src/commonMain/kotlin/com/fibelatti/core/functional/UseCase.kt) definition
* A standard [mapper](./src/commonMain/kotlin/com/fibelatti/core/functional/Mapper.kt) definition
