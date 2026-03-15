using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ShoeSizesController : ControllerBase
{
    private readonly AppDbContext _db;
    public ShoeSizesController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ShoeSizeDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.ShoeSizes.FindAsync(dto.ShoeSizeId);
            if (existing == null)
                _db.ShoeSizes.Add(new ShoeSize
                {
                    ShoeSizeId = dto.ShoeSizeId, BrandId = dto.BrandId,
                    Size = dto.Size, Quantity = dto.Quantity
                });
            else
            {
                existing.Size = dto.Size; existing.Quantity = dto.Quantity;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.ShoeSizes.ToListAsync());
}
