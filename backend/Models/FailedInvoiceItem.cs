using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class FailedInvoiceItem
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
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
