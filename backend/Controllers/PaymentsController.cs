using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class PaymentsController : ControllerBase
{
    private readonly AppDbContext _db;
    public PaymentsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<PaymentDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.Payments.FindAsync(dto.PaymentId);
            if (existing == null)
                _db.Payments.Add(new Payment
                {
                    PaymentId = dto.PaymentId, InvoiceId = dto.InvoiceId,
                    PaymentDate = dto.PaymentDate, Amount = dto.Amount, Method = dto.Method
                });
            else
            {
                existing.InvoiceId = dto.InvoiceId; existing.PaymentDate = dto.PaymentDate;
                existing.Amount = dto.Amount; existing.Method = dto.Method;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.Payments.ToListAsync());
}
