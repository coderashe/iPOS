using System.ComponentModel.DataAnnotations.Schema;

namespace iPosApi.Models;

public class User
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Contact { get; set; } = string.Empty;

    /// <summary>Maps to the <c>Role</c> column; exposed as <c>UserRole</c> for auth.</summary>
    [Column("Role")]
    public string UserRole { get; set; } = string.Empty;

    public string Password { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;

    /// <summary>Maps to the <c>Store</c> column; exposed as <c>StoreId</c> for auth.</summary>
    [Column("Store")]
    public string? StoreId { get; set; }

    public string? Address { get; set; }
}
