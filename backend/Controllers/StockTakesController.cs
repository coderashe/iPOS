using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class StockTakesController : ControllerBase
{
    private readonly AppDbContext _db;
    public StockTakesController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<StockTakeDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.StockTakes.FindAsync(dto.Id);
            if (existing == null)
                _db.StockTakes.Add(new StockTake
                {
                    Id = dto.Id, ProductId = dto.ProductId, RecordedStock = dto.RecordedStock,
                    CountedStock = dto.CountedStock, Variance = dto.Variance,
                    VarianceReason = dto.VarianceReason, DateRecorded = dto.DateRecorded,
                    RecordedBy = dto.RecordedBy
                });
            else
            {
                existing.CountedStock = dto.CountedStock; existing.Variance = dto.Variance;
                existing.VarianceReason = dto.VarianceReason;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.StockTakes.ToListAsync());
}
