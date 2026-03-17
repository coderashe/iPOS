namespace iPosApi.Models.Auth;

public class LoginRequest
{
    public string Email { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string StoreId { get; set; } = string.Empty;
}
