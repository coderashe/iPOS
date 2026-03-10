using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api")]
public class UsersController : ControllerBase
{
    private readonly AppDbContext _context;

    public UsersController(AppDbContext context)
    {
        _context = context;
    }

    [HttpPost("user")]
    public IActionResult Create(User user)
    {
        _context.Users.Add(user);
        _context.SaveChanges();
        return Ok(user);
    }

    [HttpGet("users")]
    public IActionResult GetAll()
    {
        return Ok(_context.Users.ToList());
    }
}
