namespace iPosApi.Dtos;

public class CustomerInsightsDto
{
    public int TotalCustomers { get; set; }
    public int NewCustomers { get; set; }
    public List<TopCustomerDto> TopCustomers { get; set; } = new();
    public decimal AverageCustomerValue { get; set; }
    public decimal TotalReceivables { get; set; }
}

public class TopCustomerDto
{
    public long CustomerId { get; set; }
    public string CustomerName { get; set; } = string.Empty;
    public string? CustomerPhone { get; set; }
    public decimal TotalSpent { get; set; }
    public int PurchaseCount { get; set; }
}

public class RevenueTrendDto
{
    public List<TrendDataPointDto> DataPoints { get; set; } = new();
    public decimal GrowthRate { get; set; }
    public string Trend { get; set; } = string.Empty; // "increasing", "decreasing", "stable"
}

public class TrendDataPointDto
{
    public DateTime Date { get; set; }
    public decimal Value { get; set; }
}
