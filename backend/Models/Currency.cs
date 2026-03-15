namespace iPosApi.Models;

public class Currency
{
    public string Code { get; set; } = string.Empty; // PK e.g. "USD"
    public string Name { get; set; } = string.Empty;
    public double RateToBase { get; set; }
}
