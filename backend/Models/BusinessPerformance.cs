namespace iPosApi.Models;

public class BusinessPerformance
{
    public int Id { get; set; }
    public double TotalRevenue { get; set; }
    public int TotalOrders { get; set; }
    public long Timestamp { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}
