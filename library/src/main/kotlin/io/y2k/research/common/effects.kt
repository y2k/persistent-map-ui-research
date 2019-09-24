package io.y2k.research.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.AndroidClientEngine
import io.ktor.client.engine.android.AndroidEngineConfig
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get

interface Eff<T> {
    suspend operator fun invoke(): T
}

class FinishWithStore<T, D>(val a: Eff<T>, val f: (D, Result<T>) -> D) : Eff<Unit> {
    lateinit var store: Stateful<D>
    override suspend fun invoke() {
        val x = runCatching { a() }
        store.update { db -> f(db, x) }
    }
}

fun <T, D> Eff<T>.updateStore(f: (D, Result<T>) -> D) =
    FinishWithStore(this, f)

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
