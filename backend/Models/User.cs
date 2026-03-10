namespace iPosApi.Models;

public class User
{
    public long Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Contact { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? Store { get; set; }
    public string? Address { get; set; }
}
