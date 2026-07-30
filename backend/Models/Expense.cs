using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class Expense
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long ExpenseId { get; set; }
    public string Description { get; set; } = string.Empty;
    public double Amount { get; set; }
    public long ExpenseDate { get; set; }
    public string Category { get; set; } = string.Empty;
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}
