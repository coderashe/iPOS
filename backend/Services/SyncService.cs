using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;
using Microsoft.EntityFrameworkCore;

namespace iPosApi.Services;

public class SyncService : ISyncService
{
    private readonly AppDbContext _db;
    private readonly ILogger<SyncService> _logger;

    public SyncService(AppDbContext db, ILogger<SyncService> logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task<int> SyncCustomersAsync(List<CustomerDto> items)
    {
        var ids = items.Select(i => i.CustomerId).ToList();
        var existing = await _db.Customers.Where(c => ids.Contains(c.CustomerId)).ToDictionaryAsync(c => c.CustomerId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.CustomerId, out var entity))
            {
                entity.Name = dto.Name; entity.Email = dto.Email; entity.Phone = dto.Phone;
                entity.Address = dto.Address; entity.CustomerType = dto.CustomerType;
                entity.TaxIdentificationNumber = dto.TaxIdentificationNumber;
                entity.LoyaltyPoints = dto.LoyaltyPoints;
            }
            else
            {
                _db.Customers.Add(new Customer
                {
                    CustomerId = dto.CustomerId, Name = dto.Name, Email = dto.Email,
                    Phone = dto.Phone, Address = dto.Address, CustomerType = dto.CustomerType,
                    TaxIdentificationNumber = dto.TaxIdentificationNumber, LoyaltyPoints = dto.LoyaltyPoints
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} customers", items.Count);
        return items.Count;
    }

    public async Task<int> SyncPaymentsAsync(List<PaymentDto> items)
    {
        var ids = items.Select(i => i.PaymentId).ToList();
        var existing = await _db.Payments.Where(p => ids.Contains(p.PaymentId)).ToDictionaryAsync(p => p.PaymentId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.PaymentId, out var entity))
            {
                entity.InvoiceId = dto.InvoiceId; entity.PaymentDate = dto.PaymentDate;
                entity.Amount = dto.Amount; entity.Method = dto.Method;
            }
            else
            {
                _db.Payments.Add(new Payment
                {
                    PaymentId = dto.PaymentId, InvoiceId = dto.InvoiceId,
                    PaymentDate = dto.PaymentDate, Amount = dto.Amount, Method = dto.Method
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} payments", items.Count);
        return items.Count;
    }

    public async Task<int> SyncCartItemsAsync(List<CartItemDto> items)
    {
        var ids = items.Select(i => i.CartItemId).ToList();
        var existing = await _db.CartItems.Where(c => ids.Contains(c.CartItemId)).ToDictionaryAsync(c => c.CartItemId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.CartItemId, out var entity))
            {
                entity.TotalAmount = dto.TotalAmount; entity.TotalAmountTendered = dto.TotalAmountTendered;
                entity.Change = dto.Change; entity.TaxAmount = dto.TaxAmount;
                entity.DiscountAmount = dto.DiscountAmount; entity.UpdatedAt = dto.UpdatedAt;
            }
            else
            {
                _db.CartItems.Add(new CartItem
                {
                    CartItemId = dto.CartItemId, CustomerId = dto.CustomerId, ProductId = dto.ProductId,
                    InvoiceId = dto.InvoiceId, TotalAmount = dto.TotalAmount,
                    TotalAmountTendered = dto.TotalAmountTendered, Change = dto.Change,
                    TaxAmount = dto.TaxAmount, DiscountAmount = dto.DiscountAmount,
                    CreatedAt = dto.CreatedAt, UpdatedAt = dto.UpdatedAt
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} cart items", items.Count);
        return items.Count;
    }

    public async Task<int> SyncInvoiceItemsAsync(List<InvoiceItemDto> items)
    {
        var ids = items.Select(i => i.ItemId).ToList();
        var existing = await _db.InvoiceItems.Where(i => ids.Contains(i.ItemId)).ToDictionaryAsync(i => i.ItemId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.ItemId, out var entity))
            {
                entity.Quantity = dto.Quantity; entity.Price = dto.Price; entity.Total = dto.Total;
            }
            else
            {
                _db.InvoiceItems.Add(new InvoiceItem
                {
                    ItemId = dto.ItemId, InvoiceId = dto.InvoiceId, ProductId = dto.ProductId,
                    CustomerId = dto.CustomerId, Quantity = dto.Quantity, Price = dto.Price, Total = dto.Total
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} invoice items", items.Count);
        return items.Count;
    }

    public async Task<int> SyncPriceListsAsync(List<PriceListDto> items)
    {
        var ids = items.Select(i => i.PriceListId).ToList();
        var existing = await _db.PriceLists.Where(p => ids.Contains(p.PriceListId)).ToDictionaryAsync(p => p.PriceListId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.PriceListId, out var entity))
            {
                entity.ProductId = dto.ProductId; entity.ProductDescription = dto.ProductDescription;
                entity.UnitCost = dto.UnitCost; entity.SellingPrice = dto.SellingPrice;
                entity.MarkupPercentage = dto.MarkupPercentage; entity.GrossProfitPercentage = dto.GrossProfitPercentage;
            }
            else
            {
                _db.PriceLists.Add(new PriceList
                {
                    PriceListId = dto.PriceListId, ProductId = dto.ProductId,
                    ProductDescription = dto.ProductDescription, UnitCost = dto.UnitCost,
                    SellingPrice = dto.SellingPrice, MarkupPercentage = dto.MarkupPercentage,
                    GrossProfitPercentage = dto.GrossProfitPercentage
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} price lists", items.Count);
        return items.Count;
    }

    public async Task<int> SyncExpensesAsync(List<ExpenseDto> items)
    {
        var ids = items.Select(i => i.ExpenseId).ToList();
        var existing = await _db.Expenses.Where(e => ids.Contains(e.ExpenseId)).ToDictionaryAsync(e => e.ExpenseId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.ExpenseId, out var entity))
            {
                entity.Description = dto.Description; entity.Amount = dto.Amount;
                entity.ExpenseDate = dto.ExpenseDate; entity.Category = dto.Category;
            }
            else
            {
                _db.Expenses.Add(new Expense
                {
                    ExpenseId = dto.ExpenseId, Description = dto.Description,
                    Amount = dto.Amount, ExpenseDate = dto.ExpenseDate, Category = dto.Category
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} expenses", items.Count);
        return items.Count;
    }

    public async Task<int> SyncDayReportsAsync(List<DayReportDto> items)
    {
        var ids = items.Select(i => i.ReportId).ToList();
        var existing = await _db.DayReports.Where(d => ids.Contains(d.ReportId)).ToDictionaryAsync(d => d.ReportId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.ReportId, out var entity))
            {
                entity.TotalInvoices = dto.TotalInvoices; entity.TotalOrders = dto.TotalOrders;
                entity.TotalProductsSold = dto.TotalProductsSold;
                entity.TotalInvoiceSales = dto.TotalInvoiceSales; entity.EndTime = dto.EndTime;
            }
            else
            {
                _db.DayReports.Add(new DayReport
                {
                    ReportId = dto.ReportId, TotalInvoices = dto.TotalInvoices,
                    TotalOrders = dto.TotalOrders, TotalProductsSold = dto.TotalProductsSold,
                    TotalInvoiceSales = dto.TotalInvoiceSales, ReportDate = dto.ReportDate,
                    StartTime = dto.StartTime, EndTime = dto.EndTime
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} day reports", items.Count);
        return items.Count;
    }

    public async Task<int> SyncProductDayReportsAsync(List<ProductDayReportDto> items)
    {
        var ids = items.Select(i => i.ReportId).ToList();
        var existing = await _db.ProductDayReports.Where(p => ids.Contains(p.ReportId)).ToDictionaryAsync(p => p.ReportId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.ReportId, out var entity))
            {
                entity.ClosingStock = dto.ClosingStock; entity.QuantitySold = dto.QuantitySold;
                entity.TotalSales = dto.TotalSales; entity.TotalCost = dto.TotalCost;
                entity.TotalProfit = dto.TotalProfit;
            }
            else
            {
                _db.ProductDayReports.Add(new ProductDayReport
                {
                    ReportId = dto.ReportId, ProductId = dto.ProductId, ProductName = dto.ProductName,
                    OpeningStock = dto.OpeningStock, ClosingStock = dto.ClosingStock,
                    QuantitySold = dto.QuantitySold, TotalSales = dto.TotalSales,
                    TotalCost = dto.TotalCost, TotalProfit = dto.TotalProfit, ReportDate = dto.ReportDate
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} product day reports", items.Count);
        return items.Count;
    }

    public async Task<int> SyncCurrenciesAsync(List<CurrencyDto> items)
    {
        var codes = items.Select(i => i.Code).ToList();
        var existing = await _db.Currencies.Where(c => codes.Contains(c.Code)).ToDictionaryAsync(c => c.Code);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.Code, out var entity))
            {
                entity.Name = dto.Name; entity.RateToBase = dto.RateToBase;
            }
            else
            {
                _db.Currencies.Add(new Currency { Code = dto.Code, Name = dto.Name, RateToBase = dto.RateToBase });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} currencies", items.Count);
        return items.Count;
    }

    public async Task<int> SyncCashFlowsAsync(List<CashFlowDto> items)
    {
        var ids = items.Select(i => i.CashFlowId).ToList();
        var existing = await _db.CashFlows.Where(c => ids.Contains(c.CashFlowId)).ToDictionaryAsync(c => c.CashFlowId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.CashFlowId, out var entity))
            {
                entity.Amount = dto.Amount; entity.CashOutflow = dto.CashOutflow;
                entity.Description = dto.Description;
            }
            else
            {
                _db.CashFlows.Add(new CashFlow
                {
                    CashFlowId = dto.CashFlowId, OrderId = dto.OrderId, Amount = dto.Amount,
                    CashOutflow = dto.CashOutflow, TransactionDate = dto.TransactionDate,
                    Description = dto.Description
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} cash flows", items.Count);
        return items.Count;
    }

    public async Task<int> SyncTradeReceivablesAsync(List<TradeReceivableDto> items)
    {
        var ids = items.Select(i => i.ReceivableId).ToList();
        var existing = await _db.TradeReceivables.Where(t => ids.Contains(t.ReceivableId)).ToDictionaryAsync(t => t.ReceivableId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.ReceivableId, out var entity))
            {
                entity.AmountPaid = dto.AmountPaid; entity.PaymentStatus = dto.PaymentStatus;
                entity.DueDate = dto.DueDate;
            }
            else
            {
                _db.TradeReceivables.Add(new TradeReceivable
                {
                    ReceivableId = dto.ReceivableId, OrderId = dto.OrderId,
                    TotalAmount = dto.TotalAmount, AmountPaid = dto.AmountPaid,
                    CreditPeriod = dto.CreditPeriod, DueDate = dto.DueDate,
                    PaymentStatus = dto.PaymentStatus, DateCreated = dto.DateCreated
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} trade receivables", items.Count);
        return items.Count;
    }

    public async Task<int> SyncDiscountsReceivedAsync(List<DiscountReceivedDto> items)
    {
        var ids = items.Select(i => i.DiscountId).ToList();
        var existing = await _db.DiscountsReceived.Where(d => ids.Contains(d.DiscountId)).ToDictionaryAsync(d => d.DiscountId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.DiscountId, out var entity))
            {
                entity.DiscountAmount = dto.DiscountAmount;
            }
            else
            {
                _db.DiscountsReceived.Add(new DiscountReceived
                {
                    DiscountId = dto.DiscountId, OrderId = dto.OrderId,
                    DiscountAmount = dto.DiscountAmount, DateReceived = dto.DateReceived
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} discounts received", items.Count);
        return items.Count;
    }

    public async Task<int> SyncShoeBrandsAsync(List<ShoeBrandDto> items)
    {
        var ids = items.Select(i => i.BrandId).ToList();
        var existing = await _db.ShoeBrands.Where(s => ids.Contains(s.BrandId)).ToDictionaryAsync(s => s.BrandId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.BrandId, out var entity))
                entity.BrandName = dto.BrandName;
            else
                _db.ShoeBrands.Add(new ShoeBrand { BrandId = dto.BrandId, BrandName = dto.BrandName });
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} shoe brands", items.Count);
        return items.Count;
    }

    public async Task<int> SyncShoeSizesAsync(List<ShoeSizeDto> items)
    {
        var ids = items.Select(i => i.ShoeSizeId).ToList();
        var existing = await _db.ShoeSizes.Where(s => ids.Contains(s.ShoeSizeId)).ToDictionaryAsync(s => s.ShoeSizeId);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.ShoeSizeId, out var entity))
            {
                entity.Size = dto.Size; entity.Quantity = dto.Quantity;
            }
            else
            {
                _db.ShoeSizes.Add(new ShoeSize
                {
                    ShoeSizeId = dto.ShoeSizeId, BrandId = dto.BrandId,
                    Size = dto.Size, Quantity = dto.Quantity
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} shoe sizes", items.Count);
        return items.Count;
    }

    public async Task<int> SyncStockTakesAsync(List<StockTakeDto> items)
    {
        var ids = items.Select(i => i.Id).ToList();
        var existing = await _db.StockTakes.Where(s => ids.Contains(s.Id)).ToDictionaryAsync(s => s.Id);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.Id, out var entity))
            {
                entity.CountedStock = dto.CountedStock; entity.Variance = dto.Variance;
                entity.VarianceReason = dto.VarianceReason;
            }
            else
            {
                _db.StockTakes.Add(new StockTake
                {
                    Id = dto.Id, ProductId = dto.ProductId, RecordedStock = dto.RecordedStock,
                    CountedStock = dto.CountedStock, Variance = dto.Variance,
                    VarianceReason = dto.VarianceReason, DateRecorded = dto.DateRecorded,
                    RecordedBy = dto.RecordedBy
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} stock takes", items.Count);
        return items.Count;
    }

    public async Task<int> SyncFailedInvoiceItemsAsync(List<FailedInvoiceItemDto> items)
    {
        var ids = items.Select(i => i.Id).ToList();
        var existing = await _db.FailedInvoiceItems.Where(f => ids.Contains(f.Id)).ToDictionaryAsync(f => f.Id);

        foreach (var dto in items)
        {
            if (existing.TryGetValue(dto.Id, out var entity))
            {
                entity.FailureReason = dto.FailureReason; entity.SuggestedMatches = dto.SuggestedMatches;
            }
            else
            {
                _db.FailedInvoiceItems.Add(new FailedInvoiceItem
                {
                    Id = dto.Id, RowIndex = dto.RowIndex, Brand = dto.Brand, Size = dto.Size,
                    Description = dto.Description, Quantity = dto.Quantity,
                    AmountTendered = dto.AmountTendered, CustomerId = dto.CustomerId,
                    FailureReason = dto.FailureReason, Timestamp = dto.Timestamp,
                    SuggestedMatches = dto.SuggestedMatches
                });
            }
        }
        await _db.SaveChangesAsync();
        _logger.LogInformation("Synced {Count} failed invoice items", items.Count);
        return items.Count;
    }
}
