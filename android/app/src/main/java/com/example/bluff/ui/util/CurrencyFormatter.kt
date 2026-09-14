package com.example.bluff.ui.util

fun Long.toDisplayAmount(): String {
    val rupees = this / 100
    val paise = this % 100
    return if (paise == 0L) "₹${"%,d".format(rupees)}" 
    else "₹${"%,d".format(rupees)}.${"%02d".format(paise)}"
}

fun Long.toRupeesDisplay(): String {
    val rupees = this / 100
    return "₹${"%,d".format(rupees)}"
}
