package nl.hannahsten.texifyidea.util

import com.intellij.openapi.components.Service
import com.intellij.openapi.observable.util.lockOrSkip
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.ConcurrentHashMap
import com.jetbrains.rd.util.concurrentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

abstract class ProjectCacheService(val project: Project, private val coroutineScope: CoroutineScope) {

    class CacheValueTimed<T>(
        val value: T,
        val timestamp: Long = System.currentTimeMillis(),
    ) {
        fun isExpired(expirationInMs: Long): Boolean {
            return System.currentTimeMillis() - timestamp >= expirationInMs
        }
    }

    interface TypedKey<T>

    private class PlainTypedKey<T> : TypedKey<T>
    private data class TypedKeyFromFunction<T>(val kClass: KClass<*>) : TypedKey<T>

    companion object {
        fun <T> createKey(): TypedKey<T> {
            return PlainTypedKey()
        }

        private fun <T> createKeyFromFunction(f: suspend (Project) -> T?): TypedKey<T> {
            return TypedKeyFromFunction(f::class)
        }
    }

    protected val caches: MutableMap<Any, CacheValueTimed<*>> = concurrentMapOf()
    protected val computingState = ConcurrentHashMap<Any, AtomicBoolean>()

    /**
     * Gets a cached value by its key, or null if it does not exist.
     */
    fun <T> getTimed(key: TypedKey<T>): CacheValueTimed<T>? {
        val cachedValue = caches[key] ?: return null
        @Suppress("UNCHECKED_CAST")
        return cachedValue as CacheValueTimed<T>
    }

    fun <T> put(key: TypedKey<T>, value: T) {
        caches[key] = CacheValueTimed(value)
    }

    private fun getComputingState(key: Any): AtomicBoolean {
        return computingState.computeIfAbsent(key) { AtomicBoolean(false) }
    }

    fun <T> getOrComputeNow(key: TypedKey<T>, expirationInMs: Long = 1000L, f: (Project) -> T): T {
        val cachedValue = getTimed(key)
        if (cachedValue != null && !cachedValue.isExpired(expirationInMs)) {
            return cachedValue.value
        }
        val result = f(project)
        put(key, result)
        return result
    }

    fun <T> getOrComputeNow(expirationInMs: Long = 1000L, f: (Project) -> T): T {
        return getOrComputeNow(createKeyFromFunction(f), expirationInMs, f)
    }

    private fun <T> getCachedValueOrNull(key: TypedKey<T>, expirationInMs: Long): CacheValueTimed<T>? {
        val cachedValue = getTimed(key) ?: return null
        if (cachedValue.isExpired(expirationInMs)) return null
        return cachedValue
    }

    fun <T : Any> getOrNull(key: TypedKey<T>, expirationInMs: Long = 1000L): T? {
        return getCachedValueOrNull(key, expirationInMs)?.value
    }

    fun <S, T : S & Any> getOrComputeLater(
        key: TypedKey<T>,
        expirationInMs: Long = 1000L,
        instantResult: S, suspendComputation: suspend (Project) -> T?
    ): S {
        val cachedValue = getCachedValueOrNull(key, expirationInMs)
        if (cachedValue != null) {
            return cachedValue.value
        }
        scheduleCompute(key, suspendComputation)
        return instantResult // Return the instant result while computation is in progress
    }

    fun <T : Any> getOrComputeLater(expirationInMs: Long = 1000L, instantResult: T, f: suspend (Project) -> T?): T {
        return getOrComputeLater(createKeyFromFunction(f), expirationInMs, instantResult, f)
    }

    fun <T : Any> getOrComputeLater(expirationInMs: Long = 1000L, f: suspend (Project) -> T?): T? {
        return getOrComputeLater(createKeyFromFunction(f), expirationInMs, null, f)
    }

    fun <T : Any> getOrComputeLater(key: TypedKey<T>, expirationInMs: Long = 1000L, f: suspend (Project) -> T?): T? {
        return getOrComputeLater(key, expirationInMs, null, f)
    }

    fun <T : Any> scheduleCompute(
        key: TypedKey<T>, f: suspend (Project) -> T?
    ) {
        coroutineScope.launch {
            computeAndUpdate(key, f)
        }
    }

    suspend fun <T : Any> computeAndUpdate(
        key: TypedKey<T>, f: suspend (Project) -> T?
    ) {
        val computing = getComputingState(key)
        computing.lockOrSkip {
            val result = f(project)
            if (result != null) put(key, result)
        }
    }
}

@Service(Service.Level.PROJECT)
class TexifyProjectCacheService(project: Project, coroutineScope: CoroutineScope) : ProjectCacheService(project, coroutineScope) {

    companion object {
        fun getInstance(project: Project): TexifyProjectCacheService {
            return project.getService(TexifyProjectCacheService::class.java)
        }

        fun <T> getOrCompute(
            project: Project, expirationInMs: Long = 1000L, f: (Project) -> T
        ): T {
            return getInstance(project).getOrComputeNow(expirationInMs, f)
        }

        fun <T> getOrCompute(
            project: Project, key: TypedKey<T>, expirationInMs: Long = 1000L, f: (Project) -> T
        ): T {
            return getInstance(project).getOrComputeNow(key, expirationInMs, f)
        }
    }
}
