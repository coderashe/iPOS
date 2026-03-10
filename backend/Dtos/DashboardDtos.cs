namespace iPosApi.Dtos;

public class DashboardSummaryDto
{
    public decimal TotalSales { get; set; }
    public decimal TotalRevenue { get; set; }
    public int TransactionCount { get; set; }
    public decimal AverageTransactionValue { get; set; }
    public List<TopProductDto> TopProducts { get; set; } = new();
    public int LowStockCount { get; set; }
    public int ActiveCustomersCount { get; set; }
    public DateTime LastSyncTime { get; set; }
}

public class TopProductDto
{
    public long ProductId { get; set; }
    public string ProductName { get; set; } = string.Empty;
    public int QuantitySold { get; set; }
    public decimal Revenue { get; set; }
}
