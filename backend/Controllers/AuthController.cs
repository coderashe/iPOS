using Microsoft.AspNetCore.Mvc;
using iPosApi.Models.Auth;
using iPosApi.Models;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;
    private readonly AppDbContext _db;

    public AuthController(IAuthService authService, AppDbContext db)
    {
        _authService = authService;
        _db = db;
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var token = await _authService.AuthenticateAsync(request.Email, request.Password, request.StoreId);

        if (token is null)
            return Unauthorized(new { error = "Unauthorized" });

        return Ok(new LoginResponse { Token = token });
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        var exists = _db.Users.Any(u => u.Email == request.Email);
        if (exists)
            return Conflict(new { error = "Email already registered" });

        var user = new User
        {
            Name     = request.Name,
            Email    = request.Email,
            Password = BCrypt.Net.BCrypt.HashPassword(request.Password),
            UserRole = "admin",
            StoreId  = request.StoreId,
            Contact  = request.Contact ?? string.Empty,
            Address  = request.Address
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();

        return Ok(new { message = "User registered successfully" });
    }
}
