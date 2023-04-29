package com.github.dzivko1.dullcoin.data.util

fun String.fitsHashRequirement(miningDifficulty: Int) = startsWith("0".repeat(miningDifficulty))