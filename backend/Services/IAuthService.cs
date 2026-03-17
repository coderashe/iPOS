namespace iPosApi.Services;

public interface IAuthService
{
    /// <summary>
    /// Validates credentials and returns a signed JWT, or null on any failure.
    /// Only users with UserRole == "admin" whose StoreId matches are accepted.
    /// </summary>
    Task<string?> AuthenticateAsync(string email, string password, string storeId);
}
