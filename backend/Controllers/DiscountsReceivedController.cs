using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DiscountsReceivedController : ControllerBase
{
    private readonly AppDbContext _db;
    public DiscountsReceivedController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<DiscountReceivedDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.DiscountsReceived.FindAsync(dto.DiscountId);
            if (existing == null)
                _db.DiscountsReceived.Add(new DiscountReceived
                {
                    DiscountId = dto.DiscountId, OrderId = dto.OrderId,
                    DiscountAmount = dto.DiscountAmount, DateReceived = dto.DateReceived
                });
            else
            {
                existing.DiscountAmount = dto.DiscountAmount;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.DiscountsReceived.ToListAsync());
}
