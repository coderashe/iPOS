using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class StockTakesController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public StockTakesController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] long? productId = null)
    {
        var query = _db.StockTakes.AsNoTracking();
        if (productId.HasValue)
            query = query.Where(s => s.ProductId == productId.Value);
        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<StockTakeDto> items) =>
        Ok(new { synced = await _sync.SyncStockTakesAsync(items) });
}
