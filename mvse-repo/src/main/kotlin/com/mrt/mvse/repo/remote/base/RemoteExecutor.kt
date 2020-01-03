package com.mrt.mvse.repo.remote.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Created by jaehochoe on 2019-09-25.
 */
const val IO_EXCEPTION = 5000

abstract class RemoteExecutor : Executor {
    override suspend fun <T> execute(remoteRequest: RemoteRequest<Convertible<T>>): RemoteResponse<T> {
        return withContext(Dispatchers.IO) {
            try {
                val c = remoteRequest.execute()
                convert(c)
            } catch (e: IOException) {
                RemoteResponse<T>(e.message, e, IO_EXCEPTION, null)
            }
        }
    }

    protected abstract fun <T> convert(c: Convertible<T>?): RemoteResponse<T>
}