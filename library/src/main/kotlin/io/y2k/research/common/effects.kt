package io.y2k.research.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.AndroidClientEngine
import io.ktor.client.engine.android.AndroidEngineConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get

interface Eff<T> {
    suspend operator fun invoke(): T
}

class ComposeEffect<T, D>(val a: Eff<T>, val f: (D, Result<T>) -> D) : Eff<Unit> {
    lateinit var store: Stateful<D>
    override suspend fun invoke() {
        val x = runCatching { a() }
        store.replace { db -> f(db, x) }
    }
}

fun <T, D> Eff<T>.updateStore(f: (D, Result<T>) -> D) =
    ComposeEffect(this, f)

fun <T, D> Eff<T>.updateStoreSafe(f: (D, T) -> D) =
    updateStore { db: D, r -> f(db, r.getOrThrow()) }

class LoadFromWeb(val request: HttpRequestBuilder) : Eff<String> {
    override suspend fun invoke(): String {
        val client = HttpClient(AndroidClientEngine(AndroidEngineConfig()))
        request.url.parameters.append("appid", apiKey)
        return client.get(request)
    }

    companion object {
        lateinit var apiKey: String
    }
}
