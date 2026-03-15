using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ProductDayReportsController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public ProductDayReportsController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] long? productId = null)
    {
        var query = _db.ProductDayReports.AsNoTracking();
        if (productId.HasValue)
            query = query.Where(p => p.ProductId == productId.Value);
        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ProductDayReportDto> items) =>
        Ok(new { synced = await _sync.SyncProductDayReportsAsync(items) });
}
