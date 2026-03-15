using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class ShoeSize
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long ShoeSizeId { get; set; }
    public long BrandId { get; set; }
    public double Size { get; set; }
    public long Quantity { get; set; }
}
