using BCrypt.Net;
using iPosApi.Models.Data;
using Microsoft.EntityFrameworkCore;

namespace iPosApi.Services;

public class AuthService : IAuthService
{
    private readonly AppDbContext _db;
    private readonly IJwtService  _jwt;

    public AuthService(AppDbContext db, IJwtService jwt)
    {
        _db  = db;
        _jwt = jwt;
    }

    public async Task<string?> AuthenticateAsync(string email, string password, string storeId)
    {
        var user = await _db.Users
            .AsNoTracking()
            .FirstOrDefaultAsync(u => u.Email == email);

        if (user is null)
            return null;

        // Verify password — try BCrypt first, fall back to plain-text comparison
        bool passwordValid;
        try
        {
            passwordValid = BCrypt.Net.BCrypt.Verify(password, user.Password);
        }
        catch
        {
            // Stored password is not a BCrypt hash — plain-text fallback (dev only)
            passwordValid = string.Equals(password, user.Password, StringComparison.Ordinal);
        }

        if (!passwordValid)
            return null;

        if (!string.Equals(user.UserRole, "admin", StringComparison.OrdinalIgnoreCase))
            return null;

        if (!string.Equals(user.StoreId, storeId, StringComparison.OrdinalIgnoreCase))
            return null;

        return _jwt.GenerateToken((int)user.Id, user.StoreId!, user.UserRole);
    }
}
