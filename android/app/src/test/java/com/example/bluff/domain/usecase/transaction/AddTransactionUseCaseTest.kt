package com.example.bluff.domain.usecase.transaction

import com.example.bluff.data.repository.TransactionRepository
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AddTransactionUseCaseTest {

    private lateinit var useCase: AddTransactionUseCase
    private var lastInserted: Transaction? = null

    @Before
    fun setup() {
        lastInserted = null
        useCase = AddTransactionUseCase(object : TransactionRepository {
            override fun getAllTransactions() = flowOf(emptyList<Transaction>())
            override fun getTransactionsByAccount(accountId: String) = flowOf(emptyList<Transaction>())
            override fun getTransactionsByCategory(categoryId: String) = flowOf(emptyList<Transaction>())
            override fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate) = flowOf(emptyList<Transaction>())
            override fun searchTransactions(query: String) = flowOf(emptyList<Transaction>())
            override fun getRecentTransactions() = flowOf(emptyList<Transaction>())
            override suspend fun addTransaction(transaction: Transaction): Result<String> {
                lastInserted = transaction
                return Result.success("test-id")
            }
            override suspend fun updateTransaction(transaction: Transaction) = Result.success(Unit)
            override suspend fun deleteTransaction(id: String) = Result.success(Unit)
        })
    }

    @Test
    fun `rejects zero amount`() = runTest {
        val tx = Transaction(
            id = "", userId = "", amountMinor = 0L,
            type = TransactionType.EXPENSE, accountId = "acc1",
            transactionDate = LocalDate.now()
        )
        val result = useCase(tx)
        assertTrue(result.isFailure)
    }

    @Test
    fun `rejects negative amount`() = runTest {
        val tx = Transaction(
            id = "", userId = "", amountMinor = -100L,
            type = TransactionType.EXPENSE, accountId = "acc1",
            transactionDate = LocalDate.now()
        )
        val result = useCase(tx)
        assertTrue(result.isFailure)
    }

    @Test
    fun `accepts valid transaction`() = runTest {
        val tx = Transaction(
            id = "", userId = "", amountMinor = 5000L,
            type = TransactionType.EXPENSE, accountId = "acc1",
            transactionDate = LocalDate.now()
        )
        val result = useCase(tx)
        assertTrue(result.isSuccess)
        assertEquals("test-id", result.getOrNull())
        assertEquals(5000L, lastInserted?.amountMinor)
    }
}
