package com.example.bluff.data.local

import androidx.room.TypeConverter
import com.example.bluff.domain.model.AccountType
import com.example.bluff.domain.model.BudgetPeriod
import com.example.bluff.domain.model.CategoryType
import com.example.bluff.domain.model.RecurrenceFrequency
import com.example.bluff.domain.model.TransactionType
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? = value?.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType? = value?.let { AccountType.valueOf(it) }

    @TypeConverter
    fun fromCategoryType(value: CategoryType?): String? = value?.name

    @TypeConverter
    fun toCategoryType(value: String?): CategoryType? = value?.let { CategoryType.valueOf(it) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod?): String? = value?.name

    @TypeConverter
    fun toBudgetPeriod(value: String?): BudgetPeriod? = value?.let { BudgetPeriod.valueOf(it) }

    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency?): String? = value?.name

    @TypeConverter
    fun toRecurrenceFrequency(value: String?): RecurrenceFrequency? = value?.let { RecurrenceFrequency.valueOf(it) }
}
