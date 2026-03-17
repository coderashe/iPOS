namespace iPosApi.Services;

public interface IJwtService
{
    /// <summary>
    /// Generates a signed JWT for the given user.
    /// exp = iat + 28800 seconds (8 hours).
    /// </summary>
    string GenerateToken(int userId, string storeId, string userRole);
}
