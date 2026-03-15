using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CartItemsController : ControllerBase
{
    private readonly AppDbContext _db;
    public CartItemsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CartItemDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.CartItems.FindAsync(dto.CartItemId);
            if (existing == null)
                _db.CartItems.Add(new CartItem
                {
                    CartItemId = dto.CartItemId, CustomerId = dto.CustomerId, ProductId = dto.ProductId,
                    InvoiceId = dto.InvoiceId, TotalAmount = dto.TotalAmount,
                    TotalAmountTendered = dto.TotalAmountTendered, Change = dto.Change,
                    TaxAmount = dto.TaxAmount, DiscountAmount = dto.DiscountAmount,
                    CreatedAt = dto.CreatedAt, UpdatedAt = dto.UpdatedAt
                });
            else
            {
                existing.TotalAmount = dto.TotalAmount; existing.TotalAmountTendered = dto.TotalAmountTendered;
                existing.Change = dto.Change; existing.TaxAmount = dto.TaxAmount;
                existing.DiscountAmount = dto.DiscountAmount; existing.UpdatedAt = dto.UpdatedAt;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.CartItems.ToListAsync());
}
