using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class Payment
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long PaymentId { get; set; }
    public long InvoiceId { get; set; }
    public long PaymentDate { get; set; }
    public double Amount { get; set; }
    public string Method { get; set; } = string.Empty;
}
