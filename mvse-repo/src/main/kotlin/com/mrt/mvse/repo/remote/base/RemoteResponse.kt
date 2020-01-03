package com.mrt.mvse.repo.remote.base

/**
 * Created by jaehochoe on 2019-09-25.
 */
data class RemoteResponse<T>(val message: String? = null,
                        val error: Throwable? = null,
                        val code: Int = 0,
                        val v: T?,
                        val m: Meta? = null) : RemoteData<T>() {

    override val data: T
        get() = v!!

    override val isSuccess: Boolean
        get() = v != null && error == null

    override val meta: Meta?
        get() = m
}


fun <T> RemoteResponse<T>.onComplete(block: (RemoteResponse<T>) -> Unit): RemoteResponse<T> {
    block(this)
    return this
}

fun <T> RemoteResponse<T>.onSuccess(block: (T) -> Unit): RemoteResponse<T> {
    if (isSuccess) block(data)
    return this
}

fun <T> RemoteResponse<T>.onFail(block: (RemoteResponse<T>) -> Unit): RemoteResponse<T> {
    if (!isSuccess) block(this)
    return this
}