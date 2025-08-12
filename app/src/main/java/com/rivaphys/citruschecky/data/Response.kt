package com.rivaphys.citruschecky.data

interface Response {
    fun onResponse(response: String)
    fun onError(throwable: Throwable)
}