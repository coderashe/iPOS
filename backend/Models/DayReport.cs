using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class DayReport
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long ReportId { get; set; }
    public int TotalInvoices { get; set; }
    public int TotalOrders { get; set; }
    public int TotalProductsSold { get; set; }
    public double TotalInvoiceSales { get; set; }
    public long ReportDate { get; set; }
    public long StartTime { get; set; }
    public long EndTime { get; set; }
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}
