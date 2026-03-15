using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ExpensesController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    public ExpensesController(ISyncService sync, AppDbContext db) { _sync = sync; _db = db; }

    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] string? category = null)
    {
        var query = _db.Expenses.AsNoTracking();
        if (!string.IsNullOrEmpty(category))
            query = query.Where(e => e.Category == category);
        return Ok(await query.ToListAsync());
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ExpenseDto> items) =>
        Ok(new { synced = await _sync.SyncExpensesAsync(items) });
}
