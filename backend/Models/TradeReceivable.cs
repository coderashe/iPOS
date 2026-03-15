using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class TradeReceivable
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long ReceivableId { get; set; }
    public long OrderId { get; set; }
    public double TotalAmount { get; set; }
    public double AmountPaid { get; set; }
    public int CreditPeriod { get; set; }
    public long DueDate { get; set; }
    public string PaymentStatus { get; set; } = string.Empty;
    public long DateCreated { get; set; }
}
