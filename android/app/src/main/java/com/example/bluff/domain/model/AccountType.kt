package com.example.bluff.domain.model

enum class AccountType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    WALLET("Digital Wallet"),
    SAVINGS("Savings"),
    CUSTOM("Custom")
}
