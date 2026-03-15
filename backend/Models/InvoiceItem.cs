using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class InvoiceItem
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long ItemId { get; set; }
    public long InvoiceId { get; set; }
    public long ProductId { get; set; }
    public long CustomerId { get; set; }
    public int Quantity { get; set; }
    public double Price { get; set; }
    public double Total { get; set; }
}
