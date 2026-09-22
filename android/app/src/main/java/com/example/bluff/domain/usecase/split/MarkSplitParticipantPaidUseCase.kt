package com.example.bluff.domain.usecase.split

import android.content.Context
import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.preferences.UserPreferencesManager
import com.example.bluff.data.repository.SplitBillRepository
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.transaction.AddTransactionUseCase
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
            val markResult = splitBillRepository.markParticipantPaid(participantId, true)
            if (markResult.isFailure) return Result.failure(Exception("Failed to mark paid"))

            val participant = db.splitBillDao().getParticipantById(participantId)
                ?: return Result.failure(Exception("Participant not found"))

            val userId = UserPreferencesManager(context).getUserIdBlocking()

            // Use defaultAccountId from settings, else first available account
            val settings = db.appSettingsDao().getAppSettingsSync(userId)
            val accounts = db.accountDao().getAllAccountsSync()
            val receiveAccountId = settings?.defaultAccountId?.takeIf { id ->
                accounts.any { it.id == id }
            } ?: accounts.firstOrNull()?.id
                ?: return Result.failure(Exception("No account found to receive money"))

            val incomeTx = Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = receiveAccountId,
                categoryId = null,
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

