using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class PriceList
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long PriceListId { get; set; }
    public long ProductId { get; set; }
    public string ProductDescription { get; set; } = string.Empty;
    public double UnitCost { get; set; }
    public double SellingPrice { get; set; }
    public double MarkupPercentage { get; set; }
    public double GrossProfitPercentage { get; set; }
}
