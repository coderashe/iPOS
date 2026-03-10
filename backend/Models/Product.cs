using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class Product
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string ProductCode { get; set; } = string.Empty;
    public decimal Price { get; set; }
    public double? CurrentStock { get; set; }
    public long? BrandId { get; set; }
    public long? ShoeSizeId { get; set; }
    public bool Saleable { get; set; }
    public bool Purchasable { get; set; }
    public double AverageCost { get; set; }
    public double StandardCost { get; set; }
    public string? SalesTaxCode { get; set; }
    public bool InclusiveTaxed { get; set; }
    public string? ImageUri { get; set; }
    public double UnitCost { get; set; }
    public double MarkupPercentage { get; set; }
    public long? LastUpdated { get; set; }
    public string? DeviceId { get; set; }
    public string? DeviceName { get; set; }
}
