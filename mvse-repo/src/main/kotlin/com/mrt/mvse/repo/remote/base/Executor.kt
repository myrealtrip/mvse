package com.mrt.mvse.repo.remote.base

/**
 * Created by jaehochoe on 2019-09-25.
 */
interface Executor {
    suspend fun <T> execute(remoteRequest: RemoteRequest<Convertible<T>>): RemoteResponse<T>
}