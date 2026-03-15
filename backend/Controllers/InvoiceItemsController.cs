using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class InvoiceItemsController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public InvoiceItemsController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.InvoiceItems.AsNoTracking().ToListAsync());

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<InvoiceItemDto> items) =>
        Ok(new { synced = await _sync.SyncInvoiceItemsAsync(items) });
}
