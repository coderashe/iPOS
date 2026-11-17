namespace iPosApi.Dtos;

public class SalesReportDto
{
    public List<SalesDataPointDto> DataPoints { get; set; } = new();
    public decimal TotalRevenue { get; set; }
    public int TotalTransactions { get; set; }
}

public class SalesDataPointDto
{
    public DateTime Date { get; set; }
    public decimal Revenue { get; set; }
    public int TransactionCount { get; set; }
    public int ItemsSold { get; set; }
}

public class SalesByCashierDto
{
    public string CashierName { get; set; } = string.Empty;
    public decimal TotalSales { get; set; }
    public int TransactionCount { get; set; }
    public decimal AverageTransactionValue { get; set; }
}

public class StockLevelDto
{
    public long ProductId { get; set; }
    public string ProductCode { get; set; } = string.Empty;
    public string ProductName { get; set; } = string.Empty;
    public double CurrentStock { get; set; }
    public double AverageDailySales { get; set; }
    public int? DaysRemaining { get; set; }
}
