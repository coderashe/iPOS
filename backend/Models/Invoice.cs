using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class Invoice
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long InvoiceId { get; set; }
    public long CustomerId { get; set; }
    public double TotalAmount { get; set; }
    public long InvoiceDate { get; set; }
    public long? DueDate { get; set; }
    public string TheStatus { get; set; } = string.Empty;
    public string CurrencyCode { get; set; } = string.Empty;
    public long? LastUpdated { get; set; }
    public bool IsArchived { get; set; }
    public string? DeviceId { get; set; }
    public string? DeviceName { get; set; }
}
