using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TradeReceivablesController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public TradeReceivablesController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] string? status = null)
    {
        var query = _db.TradeReceivables.AsNoTracking();
        if (!string.IsNullOrEmpty(status))
            query = query.Where(t => t.PaymentStatus == status);
        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<TradeReceivableDto> items) =>
        Ok(new { synced = await _sync.SyncTradeReceivablesAsync(items) });
}
