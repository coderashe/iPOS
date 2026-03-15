using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ShoeSizesController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public ShoeSizesController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] long? brandId = null)
    {
        var query = _db.ShoeSizes.AsNoTracking();
        if (brandId.HasValue)
            query = query.Where(s => s.BrandId == brandId.Value);
        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ShoeSizeDto> items) =>
        Ok(new { synced = await _sync.SyncShoeSizesAsync(items) });
}
