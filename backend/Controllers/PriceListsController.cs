using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PriceListsController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public PriceListsController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.PriceLists.AsNoTracking().ToListAsync());

    [HttpGet("by-product/{productId:long}")]
    public async Task<IActionResult> GetByProduct(long productId) =>
        Ok(await _db.PriceLists.AsNoTracking().Where(p => p.ProductId == productId).ToListAsync());

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<PriceListDto> items) =>
        Ok(new { synced = await _sync.SyncPriceListsAsync(items) });
}
