package com.ipos.data.entities

data class BuyerAddress(
    val houseNumber: String,
    val street: String,
    val city: String,
    val province: String
)

data class BuyerContacts(
    val phoneNo: String,
    val email: String
)

data class BuyerData(
    val buyerRegisterName: String,
    val buyerTradeName: String,
    val vatNumber: String,
    val buyerTin: String,
    val buyerAddress: BuyerAddress,
    val buyerContacts: BuyerContacts
)

data class ReceiptLine(
    val item_name: String,
    val tax_percent: Double,
    val quantity: Int,
    val unit_price: Double,
    val hs_code: String
)

data class ReceiptPayment(
    val moneyTypeCode: String,
    val paymentAmount: Double
)

data class FiscalInvoiceRequest(
    val deviceID: Int,
    val receiptType: String,
    val receiptCurrency: String,
    val receiptCounter: Int,
    val receiptGlobalNo: Int,
    val invoiceNo: String,
    val receiptDate: String,
    val receiptLinesTaxInclusive: Boolean,
    val buyerData: BuyerData,
    val receiptLines: List<ReceiptLine>,
    val receiptPayments: List<ReceiptPayment>
)

data class FiscalItem(
    val productName: String,
    val quantity: Int,
    val price: Double,
    val taxAmount: Double?
)
