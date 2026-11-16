package ru.example.lab05

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs

enum class Operation(val symbol: String) {
    ADD("+"),
    DIVIDE("÷"),
    SUBTRACT("−"),
    MULTIPLY("×")
}

enum class CalcError { INPUT, DIVISION_BY_ZERO, OVERFLOW }

data class CalcResult(
    val value: Double? = null,
    val error: CalcError? = null
)

fun calculate(a: String, b: String, op: Operation): CalcResult {
    val x = a.trim().replace(',', '.').toDoubleOrNull()
    val y = b.trim().replace(',', '.').toDoubleOrNull()
    if (x == null || y == null || !x.isFinite() || !y.isFinite()) {
        return CalcResult(error = CalcError.INPUT)
    }
    if (op == Operation.DIVIDE && y == 0.0) {
        return CalcResult(error = CalcError.DIVISION_BY_ZERO)
    }
    val result = when (op) {
        Operation.ADD -> x + y
        Operation.DIVIDE -> x / y
        Operation.SUBTRACT -> x - y
        Operation.MULTIPLY -> x * y
    }
    return if (result.isFinite()) CalcResult(value = result)
    else CalcResult(error = CalcError.OVERFLOW)
}

fun formatNumber(value: Double): String {
    val rounded = BigDecimal.valueOf(value)
        .round(MathContext(10)).stripTrailingZeros()
    return if (abs(value) >= 1e10 ||
        (value != 0.0 && abs(value) < 1e-6)) rounded.toString()
    else rounded.toPlainString()
}