using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class UsersController : ControllerBase
{
    private readonly IUserService _service;

    public UsersController(IUserService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());

    [HttpGet("{id:long}")]
    public async Task<IActionResult> GetById(long id)
    {
        var user = await _service.GetByIdAsync(id);
        return user is null ? NotFound() : Ok(user);
    }

    [HttpGet("by-email")]
    public async Task<IActionResult> GetByEmail([FromQuery] string email)
    {
        var user = await _service.GetByEmailAsync(email);
        return user is null ? NotFound() : Ok(user);
    }

    /// <summary>Upsert a single user (used by Android sync worker)</summary>
    [HttpPost]
    public async Task<IActionResult> Upsert([FromBody] User user)
    {
        var result = await _service.UpsertAsync(user);
        return Ok(result);
    }

    /// <summary>Batch sync from Android</summary>
    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<User> users)
    {
        foreach (var user in users)
            await _service.UpsertAsync(user);
        return Ok(new { synced = users.Count });
    }
}
