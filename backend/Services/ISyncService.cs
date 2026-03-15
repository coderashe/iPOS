using iPosApi.Dtos;

namespace iPosApi.Services;

public interface ISyncService
{
    Task<int> SyncCustomersAsync(List<CustomerDto> items);
    Task<int> SyncPaymentsAsync(List<PaymentDto> items);
    Task<int> SyncCartItemsAsync(List<CartItemDto> items);
    Task<int> SyncInvoiceItemsAsync(List<InvoiceItemDto> items);
    Task<int> SyncPriceListsAsync(List<PriceListDto> items);
    Task<int> SyncExpensesAsync(List<ExpenseDto> items);
    Task<int> SyncDayReportsAsync(List<DayReportDto> items);
    Task<int> SyncProductDayReportsAsync(List<ProductDayReportDto> items);
    Task<int> SyncCurrenciesAsync(List<CurrencyDto> items);
    Task<int> SyncCashFlowsAsync(List<CashFlowDto> items);
    Task<int> SyncTradeReceivablesAsync(List<TradeReceivableDto> items);
    Task<int> SyncDiscountsReceivedAsync(List<DiscountReceivedDto> items);
    Task<int> SyncShoeBrandsAsync(List<ShoeBrandDto> items);
    Task<int> SyncShoeSizesAsync(List<ShoeSizeDto> items);
    Task<int> SyncStockTakesAsync(List<StockTakeDto> items);
    Task<int> SyncFailedInvoiceItemsAsync(List<FailedInvoiceItemDto> items);
}
