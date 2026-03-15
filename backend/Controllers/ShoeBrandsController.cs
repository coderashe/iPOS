using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ShoeBrandsController : ControllerBase
{
    private readonly AppDbContext _db;
    public ShoeBrandsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ShoeBrandDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.ShoeBrands.FindAsync(dto.BrandId);
            if (existing == null)
                _db.ShoeBrands.Add(new ShoeBrand { BrandId = dto.BrandId, BrandName = dto.BrandName });
            else
                existing.BrandName = dto.BrandName;
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.ShoeBrands.ToListAsync());
}
