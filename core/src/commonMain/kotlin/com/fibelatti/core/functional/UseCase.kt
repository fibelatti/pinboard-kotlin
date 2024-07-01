package com.fibelatti.core.functional

import kotlinx.coroutines.flow.Flow

/**
 * A functional interface to define single responsibility use cases.
 *
 * ```
 * class Foo : UseCase<Result<Bar>> {
 *
 *     override suspend operator fun invoke(): Result<Bar> = TODO()
 * }
 *
 * val result: Result<Bar> = foo()
 * ```
 */
public fun interface UseCase<out Type> : suspend () -> Type where Type : Any?

/**
 * A functional interface to define single responsibility use cases that require parameters.
 *
 * ```
 * class Foo : UseCaseWithParams<Foo.Params, Result<Bar>> {
 *
 *     override suspend operator fun invoke(params: Post): Result<Bar> = TODO()
 * }
 *
 * val result: Result<Bar> = foo(Foo.Params(baz))
 * ```
 */
public fun interface UseCaseWithParams<in Params, out Type> : suspend (Params) -> Type where Type : Any?

/**
 * A functional interface to define single responsibility use cases that require parameters and
 * return a [Flow] of [Type].
 *
 * ```
 * class Foo : ObservableUseCaseWithParams<Foo.Params, Bar> {
 *
 *     override operator fun invoke(params: Foo.Params): Flow<Bar> = = TODO()
 * }
 *
 * foo(Foo.Params(baz)).collect { bar ->
 *     // Use bar
 * }
 * ```
 */
public fun interface ObservableUseCaseWithParams<in Params, out Type> : (Params) -> Flow<Type> where Type : Any?
