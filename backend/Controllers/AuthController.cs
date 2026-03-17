using Microsoft.AspNetCore.Mvc;
using iPosApi.Models.Auth;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    public AuthController(IAuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var token = await _authService.AuthenticateAsync(request.Email, request.Password, request.StoreId);

        if (token is null)
            return Unauthorized(new { error = "Unauthorized" });

        return Ok(new LoginResponse { Token = token });
    }
}
