package com.mrt.mvse.repo.remote.base

/**
 * Created by jaehochoe on 2019-09-25.
 */
data class RemoteResponse<T>(
    val message: String? = null,
    val error: Throwable? = null,
    val code: Int = 0,
    val v: T?,
    val m: Meta? = null
) : RemoteData<T>() {

    override val data: T
        get() = v!!

    override val isSuccess: Boolean
        get() = v != null && error == null

    override val meta: Meta?
        get() = m
}


inline fun <T> RemoteResponse<T>.onComplete(block: (RemoteResponse<T>) -> Unit): RemoteResponse<T> {
    block(this)
    return this
}


inline fun <T, R> RemoteResponse<T>.onCompleted(block: (RemoteResponse<T>) -> R): R? {
    return block(this)
}

inline fun <T> RemoteResponse<T>.onSuccess(block: (T) -> Unit): RemoteResponse<T> {
    if (isSuccess) block(data)
    return this
}


inline fun <T, R> RemoteResponse<T>.onSuccessed(block: (T) -> R): R? {
    if (isSuccess) return block(data)
    return null
}

inline fun <T> RemoteResponse<T>.onFail(block: (RemoteResponse<T>) -> Unit): RemoteResponse<T> {
    if (!isSuccess) block(this)
    return this
}

inline fun <T, R> RemoteResponse<T>.onFailed(block: (RemoteResponse<T>) -> R): R? {
    if (!isSuccess) return block(this)
    return null
}