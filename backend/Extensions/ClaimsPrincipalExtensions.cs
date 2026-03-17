using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace iPosApi.Extensions;

public static class ClaimsPrincipalExtensions
{
    public static string? GetStoreId(this ClaimsPrincipal user)
        => user.FindFirstValue("storeId");

    public static int GetUserId(this ClaimsPrincipal user)
    {
        var sub = user.FindFirstValue(JwtRegisteredClaimNames.Sub);
        return int.TryParse(sub, out var id) ? id : 0;
    }

    public static string? GetUserRole(this ClaimsPrincipal user)
        => user.FindFirstValue(ClaimTypes.Role);
}
