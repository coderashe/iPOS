using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CustomersController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;

    public CustomersController(ISyncService sync, AppDbContext db)
    {
        _sync = sync;
        _db = db;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.Customers.AsNoTracking().ToListAsync());

    [HttpGet("{id:long}")]
    public async Task<IActionResult> GetById(long id)
    {
        var customer = await _db.Customers.FindAsync(id);
        return customer is null ? NotFound() : Ok(customer);
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CustomerDto> items)
    {
        var count = await _sync.SyncCustomersAsync(items);
        return Ok(new { synced = count });
    }
}
