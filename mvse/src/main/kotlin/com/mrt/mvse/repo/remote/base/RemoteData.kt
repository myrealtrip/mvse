package com.mrt.mvse.repo.remote.base

/**
 * Created by jaehochoe on 2019-09-25.
 */
abstract class RemoteData<T> {
    abstract val isSuccess: Boolean
    abstract val data: T
    abstract val meta: Meta?
}