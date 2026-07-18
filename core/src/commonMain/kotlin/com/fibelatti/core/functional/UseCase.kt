package com.fibelatti.core.functional

import kotlinx.coroutines.flow.Flow

/**
 * A functional interface to define single responsibility use cases.
 *
 * ```
 * class Foo : UseCase<Bar> {
 *
 *     override suspend operator fun invoke(): Bar = TODO()
 * }
 *
 * val result: Bar = foo()
 * ```
 */
public fun interface UseCase<out Type> : suspend () -> Type where Type : Any?

/**
 * A functional interface to define single responsibility use cases that require parameters.
 *
 * ```
 * class Foo : UseCaseWithParams<Foo.Params, Bar> {
 *
 *     override suspend operator fun invoke(params: Post): Bar = TODO()
 * }
 *
 * val result: Bar = foo(Foo.Params(baz))
 * ```
 */
public fun interface UseCaseWithParams<in Params, out Type> : suspend (Params) -> Type where Type : Any?

/**
 * A functional interface to define single responsibility use cases that return a [Result] of
 * [Type].
 *
 * ```
 * class Foo : ResultUseCase<Bar> {
 *
 *     override suspend operator fun invoke(): Result<Bar> = TODO()
 * }
 *
 * val result: Result<Bar> = foo()
 * ```
 *
 * Prefer this over `UseCase<Result<Type>>`: [Result] is a value class, and when it is supplied as
 * the type argument of a generic function type the compiler emits a bridge whose return value is
 * boxed. Keeping [Result] in the signature instead of the type argument avoids the bridge.
 */
public fun interface ResultUseCase<out Type> where Type : Any? {

    public suspend operator fun invoke(): Result<Type>
}

/**
 * A functional interface to define single responsibility use cases that require parameters and
 * return a [Result] of [Type].
 *
 * ```
 * class Foo : ResultUseCaseWithParams<Foo.Params, Bar> {
 *
 *     override suspend operator fun invoke(params: Foo.Params): Result<Bar> = TODO()
 * }
 *
 * val result: Result<Bar> = foo(Foo.Params(baz))
 * ```
 *
 * Prefer this over `UseCaseWithParams<Params, Result<Type>>`: [Result] is a value class, and when
 * it is supplied as the type argument of a generic function type the compiler emits a bridge whose
 * return value is boxed. Keeping [Result] in the signature instead of the type argument avoids the
 * bridge.
 */
public fun interface ResultUseCaseWithParams<in Params, out Type> where Type : Any? {

    public suspend operator fun invoke(params: Params): Result<Type>
}

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
