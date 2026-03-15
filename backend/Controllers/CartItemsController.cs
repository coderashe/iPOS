using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CartItemsController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public CartItemsController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.CartItems.AsNoTracking().ToListAsync());

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CartItemDto> items) =>
        Ok(new { synced = await _sync.SyncCartItemsAsync(items) });
}
