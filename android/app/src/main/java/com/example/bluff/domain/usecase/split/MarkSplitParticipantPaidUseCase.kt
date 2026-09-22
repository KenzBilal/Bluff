package com.example.bluff.domain.usecase.split

import com.example.bluff.data.repository.SplitBillRepository
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
import com.example.bluff.data.local.dao.SplitBillDao
import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.preferences.UserPreferencesManager
import android.content.Context
import java.time.LocalDate
import java.util.UUID

class MarkSplitParticipantPaidUseCase(
    private val splitBillRepository: SplitBillRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val db: BluffDatabase,
    private val context: Context
) {
    suspend operator fun invoke(participantId: String): Result<Unit> {
        return try {
            // Mark as paid in DB
            val markResult = splitBillRepository.markParticipantPaid(participantId, true)
            if (markResult.isFailure) return Result.failure(Exception("Failed to mark paid"))

            val participant = db.splitBillDao().getParticipantById(participantId)
                ?: return Result.failure(Exception("Participant not found"))

            // Find an account to receive the income (first available)
            val accounts = db.accountDao().getAllAccountsSync() // Need to add this or use flow.first()
            val receiveAccountId = accounts.firstOrNull()?.id ?: return Result.failure(Exception("No account found to receive money"))

            val userId = UserPreferencesManager(context).getUserIdBlocking()

            // Auto-create Income Transaction
            val incomeTx = Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = receiveAccountId,
                categoryId = null, // Or some default category
                amountMinor = participant.amountMinor,
                type = TransactionType.INCOME,
                note = "Split payback from ${participant.contactName}",
                transactionDate = LocalDate.now(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val addResult = addTransactionUseCase(incomeTx)
            if (addResult.isFailure) return Result.failure(Exception("Failed to add income transaction"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
