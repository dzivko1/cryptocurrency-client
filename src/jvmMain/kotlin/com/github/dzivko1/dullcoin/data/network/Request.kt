package com.github.dzivko1.dullcoin.data.network

data class Request<T>(
    val fromAddress: String,
    val code: String,
    val data: T
)