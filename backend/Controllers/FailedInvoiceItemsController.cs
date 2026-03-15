using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class FailedInvoiceItemsController : ControllerBase
{
    private readonly AppDbContext _db;
    public FailedInvoiceItemsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<FailedInvoiceItemDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.FailedInvoiceItems.FindAsync(dto.Id);
            if (existing == null)
                _db.FailedInvoiceItems.Add(new FailedInvoiceItem
                {
                    Id = dto.Id, RowIndex = dto.RowIndex, Brand = dto.Brand, Size = dto.Size,
                    Description = dto.Description, Quantity = dto.Quantity,
                    AmountTendered = dto.AmountTendered, CustomerId = dto.CustomerId,
                    FailureReason = dto.FailureReason, Timestamp = dto.Timestamp,
                    SuggestedMatches = dto.SuggestedMatches
                });
            else
            {
                existing.FailureReason = dto.FailureReason;
                existing.SuggestedMatches = dto.SuggestedMatches;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.FailedInvoiceItems.ToListAsync());
}
