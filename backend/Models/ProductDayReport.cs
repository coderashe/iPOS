using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class ProductDayReport
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long ReportId { get; set; }
    public long ProductId { get; set; }
    public string ProductName { get; set; } = string.Empty;
    public double OpeningStock { get; set; }
    public double ClosingStock { get; set; }
    public int QuantitySold { get; set; }
    public double TotalSales { get; set; }
    public double TotalCost { get; set; }
    public double TotalProfit { get; set; }
    public long ReportDate { get; set; }
}
