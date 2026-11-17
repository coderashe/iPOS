namespace iPosApi.Models.Auth;

public class RegisterRequest
{
    public string Name     { get; set; } = string.Empty;
    public string Email    { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string StoreId  { get; set; } = string.Empty;
    public string? Contact { get; set; }
    public string? Address { get; set; }
}
