using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class CartItem
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long CartItemId { get; set; }
    public long CustomerId { get; set; }
    public long ProductId { get; set; }
    public long InvoiceId { get; set; }
    public double TotalAmount { get; set; }
    public double TotalAmountTendered { get; set; }
    public double Change { get; set; }
    public double? TaxAmount { get; set; }
    public double? DiscountAmount { get; set; }
    public long CreatedAt { get; set; }
    public long? UpdatedAt { get; set; }
}
