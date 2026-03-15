using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class ShoeBrand
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long BrandId { get; set; }
    public string BrandName { get; set; } = string.Empty;
}
