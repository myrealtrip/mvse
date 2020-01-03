package com.mrt.mvse.repo.remote.base

/**
 * Created by jaehochoe on 2019-09-25.
 */
class RemoteRequest<T>(val execute: suspend () -> T?)