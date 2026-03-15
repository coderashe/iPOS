using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class Customer
{
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
    public long CustomerId { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Email { get; set; }
    public string? Phone { get; set; }
    public string? Address { get; set; }
    public string CustomerType { get; set; } = "Regular";
    public string? TaxIdentificationNumber { get; set; }
    public int LoyaltyPoints { get; set; } = 0;
}
