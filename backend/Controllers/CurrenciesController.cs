using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CurrenciesController : ControllerBase
{
    private readonly AppDbContext _db;
    public CurrenciesController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CurrencyDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.Currencies.FindAsync(dto.Code);
            if (existing == null)
                _db.Currencies.Add(new Currency { Code = dto.Code, Name = dto.Name, RateToBase = dto.RateToBase });
            else
            {
                existing.Name = dto.Name; existing.RateToBase = dto.RateToBase;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.Currencies.ToListAsync());
}
