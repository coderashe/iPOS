using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace iPosApi.Services;

public class JwtService : IJwtService
{
    private readonly string _secret;
    private readonly string _issuer;
    private readonly string _audience;

    public JwtService(IConfiguration configuration)
    {
        _secret   = configuration["Jwt:Secret"]   ?? throw new InvalidOperationException("Jwt:Secret is not configured.");
        _issuer   = configuration["Jwt:Issuer"]   ?? throw new InvalidOperationException("Jwt:Issuer is not configured.");
        _audience = configuration["Jwt:Audience"] ?? throw new InvalidOperationException("Jwt:Audience is not configured.");
    }

    public string GenerateToken(int userId, string storeId, string userRole)
    {
        var key   = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var iat = DateTimeOffset.UtcNow;
        var exp = iat.AddSeconds(28800); // 8 hours

        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub,     userId.ToString()),
            new Claim("storeId",                       storeId),
            new Claim(ClaimTypes.Role,                 userRole),
            new Claim(JwtRegisteredClaimNames.Iat,     iat.ToUnixTimeSeconds().ToString(), ClaimValueTypes.Integer64),
        };

        var token = new JwtSecurityToken(
            issuer:             _issuer,
            audience:           _audience,
            claims:             claims,
            notBefore:          iat.UtcDateTime,
            expires:            exp.UtcDateTime,
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}
