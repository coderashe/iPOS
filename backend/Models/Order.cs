using System.ComponentModel.DataAnnotations;

namespace iPosApi.Models;

public class Order
{
    public int Id { get; set; }
    public long CustomerId { get; set; }
    public long CreatedAt { get; set; }
    public decimal TotalAmount { get; set; }
    public decimal PaidAmount { get; set; }
    public string Status { get; set; } = string.Empty;

    public List<OrderItem> Items { get; set; } = new();
}
