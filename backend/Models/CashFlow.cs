using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class CashFlow
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long CashFlowId { get; set; }
    public long OrderId { get; set; }
    public double Amount { get; set; }
    public bool CashOutflow { get; set; }
    public long TransactionDate { get; set; }
    public string? Description { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}
