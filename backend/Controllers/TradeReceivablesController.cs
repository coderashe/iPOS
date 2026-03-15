using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TradeReceivablesController : ControllerBase
{
    private readonly AppDbContext _db;
    public TradeReceivablesController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<TradeReceivableDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.TradeReceivables.FindAsync(dto.ReceivableId);
            if (existing == null)
                _db.TradeReceivables.Add(new TradeReceivable
                {
                    ReceivableId = dto.ReceivableId, OrderId = dto.OrderId,
                    TotalAmount = dto.TotalAmount, AmountPaid = dto.AmountPaid,
                    CreditPeriod = dto.CreditPeriod, DueDate = dto.DueDate,
                    PaymentStatus = dto.PaymentStatus, DateCreated = dto.DateCreated
                });
            else
            {
                existing.AmountPaid = dto.AmountPaid; existing.PaymentStatus = dto.PaymentStatus;
                existing.DueDate = dto.DueDate;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.TradeReceivables.ToListAsync());
}
