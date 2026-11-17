using System.ComponentModel.DataAnnotations;

namespace iPosApi.Models;

public class InvoiceLine
{
    public int Id { get; set; }
    public long InvoiceId { get; set; }
    public string InvoiceStatus { get; set; } = string.Empty;
    public long ProductID { get; set; }
    public string ProductName { get; set; } = string.Empty;
    public double ProductPrice { get; set; }
    public int ProductQtySold { get; set; }
    public string Cashier { get; set; } = string.Empty;
    public long CustomerID { get; set; }
    public string CustomerName { get; set; } = string.Empty;
    public string? CustomerPhone { get; set; }
    public double TotalAmount { get; set; }
    public double? AmountTendered { get; set; }
    public double? ChangeBalance { get; set; }
    public long InvoiceDate { get; set; }
    public double? TaxAmount { get; set; }
    public double CurrentStock { get; set; }
    public long CreatedAt { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    public long? UpdatedAt { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}
