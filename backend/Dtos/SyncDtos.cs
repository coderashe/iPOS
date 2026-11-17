namespace iPosApi.Dtos;

public class CustomerDto
{
    public long CustomerId { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Email { get; set; }
    public string? Phone { get; set; }
    public string? Address { get; set; }
    public string CustomerType { get; set; } = "Regular";
    public string? TaxIdentificationNumber { get; set; }
    public int LoyaltyPoints { get; set; }
}

public class PaymentDto
{
    public long PaymentId { get; set; }
    public long InvoiceId { get; set; }
    public long PaymentDate { get; set; }
    public double Amount { get; set; }
    public string Method { get; set; } = string.Empty;
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class CartItemDto
{
    public long CartItemId { get; set; }
    public long CustomerId { get; set; }
    public long ProductId { get; set; }
    public long InvoiceId { get; set; }
    public double TotalAmount { get; set; }
    public double TotalAmountTendered { get; set; }
    public double Change { get; set; }
    public double? TaxAmount { get; set; }
    public double? DiscountAmount { get; set; }
    public long CreatedAt { get; set; }
    public long? UpdatedAt { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class InvoiceItemDto
{
    public long ItemId { get; set; }
    public long InvoiceId { get; set; }
    public long ProductId { get; set; }
    public long CustomerId { get; set; }
    public int Quantity { get; set; }
    public double Price { get; set; }
    public double Total { get; set; }
}

public class PriceListDto
{
    public long PriceListId { get; set; }
    public long ProductId { get; set; }
    public string ProductDescription { get; set; } = string.Empty;
    public double UnitCost { get; set; }
    public double SellingPrice { get; set; }
    public double MarkupPercentage { get; set; }
    public double GrossProfitPercentage { get; set; }
}

public class ExpenseDto
{
    public long ExpenseId { get; set; }
    public string Description { get; set; } = string.Empty;
    public double Amount { get; set; }
    public long ExpenseDate { get; set; }
    public string Category { get; set; } = string.Empty;
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class DayReportDto
{
    public long ReportId { get; set; }
    public int TotalInvoices { get; set; }
    public int TotalOrders { get; set; }
    public int TotalProductsSold { get; set; }
    public double TotalInvoiceSales { get; set; }
    public long ReportDate { get; set; }
    public long StartTime { get; set; }
    public long EndTime { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class ProductDayReportDto
{
    public long ReportId { get; set; }
    public long ProductId { get; set; }
    public string ProductName { get; set; } = string.Empty;
    public double OpeningStock { get; set; }
    public double ClosingStock { get; set; }
    public int QuantitySold { get; set; }
    public double TotalSales { get; set; }
    public double TotalCost { get; set; }
    public double TotalProfit { get; set; }
    public long ReportDate { get; set; }
}

public class CurrencyDto
{
    public string Code { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public double RateToBase { get; set; }
}

public class CashFlowDto
{
    public long CashFlowId { get; set; }
    public long OrderId { get; set; }
    public double Amount { get; set; }
    public bool CashOutflow { get; set; }
    public long TransactionDate { get; set; }
    public string? Description { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class TradeReceivableDto
{
    public long ReceivableId { get; set; }
    public long OrderId { get; set; }
    public double TotalAmount { get; set; }
    public double AmountPaid { get; set; }
    public int CreditPeriod { get; set; }
    public long DueDate { get; set; }
    public string PaymentStatus { get; set; } = string.Empty;
    public long DateCreated { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class DiscountReceivedDto
{
    public long DiscountId { get; set; }
    public long OrderId { get; set; }
    public double DiscountAmount { get; set; }
    public long DateReceived { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class ShoeBrandDto
{
    public long BrandId { get; set; }
    public string BrandName { get; set; } = string.Empty;
}

public class ShoeSizeDto
{
    public long ShoeSizeId { get; set; }
    public long BrandId { get; set; }
    public double Size { get; set; }
    public long Quantity { get; set; }
}

public class StockTakeDto
{
    public int Id { get; set; }
    public long ProductId { get; set; }
    public long RecordedStock { get; set; }
    public double CountedStock { get; set; }
    public double Variance { get; set; }
    public string? VarianceReason { get; set; }
    public long DateRecorded { get; set; }
    public string RecordedBy { get; set; } = string.Empty;
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}

public class FailedInvoiceItemDto
{
    public int Id { get; set; }
    public int RowIndex { get; set; }
    public string Brand { get; set; } = string.Empty;
    public double Size { get; set; }
    public string Description { get; set; } = string.Empty;
    public int Quantity { get; set; }
    public double AmountTendered { get; set; }
    public long CustomerId { get; set; }
    public string FailureReason { get; set; } = string.Empty;
    public long Timestamp { get; set; }
    public string? SuggestedMatches { get; set; }
}

/// <summary>DTO for device registration sync from Android.</summary>
public class DeviceSyncDto
{
    public string DeviceId { get; set; } = string.Empty;
    public string DeviceName { get; set; } = string.Empty;
    public string StoreId { get; set; } = string.Empty;
    public long LastSeen { get; set; }
}

/// <summary>DTO for user sync from Android.</summary>
public class UserSyncDto
{
    public long UserId { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Contact { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? StoreId { get; set; }
    public string? Address { get; set; }
}
