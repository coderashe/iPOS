using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class FailedInvoiceItemsController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public FailedInvoiceItemsController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.FailedInvoiceItems.AsNoTracking().ToListAsync());

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<FailedInvoiceItemDto> items) =>
        Ok(new { synced = await _sync.SyncFailedInvoiceItemsAsync(items) });
}
