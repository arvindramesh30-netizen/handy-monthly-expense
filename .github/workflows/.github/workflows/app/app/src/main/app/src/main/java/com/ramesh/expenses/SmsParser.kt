package com.ramesh.expenses

data class Parsed(val amount: Double, val debit: Boolean, val merchant: String)

object SmsParser {
    private val amt = Regex("""(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
    private val debitWords = Regex("""debited|spent|sent|paid|purchase|withdrawn|payment of|txn of""", RegexOption.IGNORE_CASE)
    private val creditWords = Regex("""credited|received|refund|deposited""", RegexOption.IGNORE_CASE)
    private val merch = Regex("""(?:\bat\b|\bto\b|towards|@|info[:\-]|\bfrom\b)\s+([A-Za-z0-9 &.\-_/]+?)(?=\s+(?:on|ref|via|using|upi|avl|bal|for|dated)\b|[.,\n]|$)""", RegexOption.IGNORE_CASE)

    fun parse(body: String): Parsed? {
        if (body.contains("otp", true) || body.contains("will be debited", true)) return null
        val a = amt.find(body)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: return null
        val isDebit = debitWords.containsMatchIn(body)
        val isCredit = creditWords.containsMatchIn(body)
        if (!isDebit && !isCredit) return null
        val m = merch.findAll(body).map { it.groupValues[1].trim() }
            .firstOrNull { !it.matches(Regex("""(?i)(a/c|acct|card).*""")) } ?: ""
        return Parsed(a, isDebit && !(isCredit && !isDebit), m)
    }
}
