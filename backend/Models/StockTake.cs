using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class StockTake
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public int Id { get; set; }
    public long ProductId { get; set; }
    public long RecordedStock { get; set; }
    public double CountedStock { get; set; }
    public double Variance { get; set; }
    public string? VarianceReason { get; set; }
    public long DateRecorded { get; set; }
    public string RecordedBy { get; set; } = string.Empty;
    public string? DeviceId { get; set; }
    public string? StoreId { get; set; }
}
