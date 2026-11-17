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
public class DayReportsController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    private readonly IStoreDeviceService _storeDeviceService;

    public DayReportsController(ISyncService sync, AppDbContext db, IStoreDeviceService storeDeviceService)
    {
        _sync = sync; _db = db; _storeDeviceService = storeDeviceService;
    }

    [HttpGet]
    [Authorize]
    public async Task<IActionResult> GetAll()
    {
        var storeId = User.GetStoreId();
        var query = _db.DayReports.AsNoTracking();

        if (!string.IsNullOrEmpty(storeId))
            query = query.Where(d => d.StoreId == storeId);

        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<DayReportDto> items)
    {
        if (items.Any(i => string.IsNullOrEmpty(i.StoreId)))
            return BadRequest("All items must include a StoreId.");
        return Ok(new { synced = await _sync.SyncDayReportsAsync(items) });
    }
}
