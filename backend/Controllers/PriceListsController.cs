using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PriceListsController : ControllerBase
{
    private readonly AppDbContext _db;
    public PriceListsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<PriceListDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.PriceLists.FindAsync(dto.PriceListId);
            if (existing == null)
                _db.PriceLists.Add(new PriceList
                {
                    PriceListId = dto.PriceListId, ProductId = dto.ProductId,
                    ProductDescription = dto.ProductDescription, UnitCost = dto.UnitCost,
                    SellingPrice = dto.SellingPrice, MarkupPercentage = dto.MarkupPercentage,
                    GrossProfitPercentage = dto.GrossProfitPercentage
                });
            else
            {
                existing.ProductId = dto.ProductId; existing.ProductDescription = dto.ProductDescription;
                existing.UnitCost = dto.UnitCost; existing.SellingPrice = dto.SellingPrice;
                existing.MarkupPercentage = dto.MarkupPercentage;
                existing.GrossProfitPercentage = dto.GrossProfitPercentage;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.PriceLists.ToListAsync());
}
