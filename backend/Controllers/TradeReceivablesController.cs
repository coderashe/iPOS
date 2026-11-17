using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Extensions;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TradeReceivablesController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    private readonly IStoreDeviceService _storeDeviceService;

    public TradeReceivablesController(ISyncService sync, AppDbContext db, IStoreDeviceService storeDeviceService)
    {
        _sync = sync; _db = db; _storeDeviceService = storeDeviceService;
    }

    [HttpGet]
    [Authorize]
    public async Task<IActionResult> GetAll([FromQuery] string? status = null)
    {
        var storeId = User.GetStoreId();
        var query = _db.TradeReceivables.AsNoTracking();

        if (!string.IsNullOrEmpty(storeId))
            query = query.Where(t => t.StoreId == storeId);
        if (!string.IsNullOrEmpty(status))
            query = query.Where(t => t.PaymentStatus == status);

        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<TradeReceivableDto> items)
    {
        if (items.Any(i => string.IsNullOrEmpty(i.StoreId)))
            return BadRequest("All items must include a StoreId.");
        return Ok(new { synced = await _sync.SyncTradeReceivablesAsync(items) });
    }
}
