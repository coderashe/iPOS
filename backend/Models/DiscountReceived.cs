using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class DiscountReceived
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long DiscountId { get; set; }
    public long OrderId { get; set; }
    public double DiscountAmount { get; set; }
    public long DateReceived { get; set; }
}
