package com.tuanha.wallet.utils.ext

import java.math.BigDecimal

fun String?.toBigDecimalOrZero() = this?.toBigDecimalOrNull() ?: BigDecimal.ZERO

