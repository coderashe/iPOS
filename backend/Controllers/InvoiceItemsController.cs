using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class InvoiceItemsController : ControllerBase
{
    private readonly AppDbContext _db;
    public InvoiceItemsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<InvoiceItemDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.InvoiceItems.FindAsync(dto.ItemId);
            if (existing == null)
                _db.InvoiceItems.Add(new InvoiceItem
                {
                    ItemId = dto.ItemId, InvoiceId = dto.InvoiceId, ProductId = dto.ProductId,
                    CustomerId = dto.CustomerId, Quantity = dto.Quantity, Price = dto.Price, Total = dto.Total
                });
            else
            {
                existing.Quantity = dto.Quantity; existing.Price = dto.Price; existing.Total = dto.Total;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.InvoiceItems.ToListAsync());
}
