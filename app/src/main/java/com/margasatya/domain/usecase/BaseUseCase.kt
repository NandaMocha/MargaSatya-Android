package com.margasatya.domain.usecase

import com.margasatya.core.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Base Use Case following Single Responsibility Principle
 * Each use case should do one thing and do it well
 */
abstract class BaseUseCase<in Params, out Type>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(params: Params): Resource<Type> {
        return try {
            withContext(dispatcher) {
                execute(params)
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(params: Params): Resource<Type>
}

/**
 * For use cases that don't need parameters
 */
abstract class NoParamsUseCase<out Type>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Resource<Type> {
        return try {
            withContext(dispatcher) {
                execute()
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    @Throws(RuntimeException::class)
    protected abstract suspend fun execute(): Resource<Type>
}
