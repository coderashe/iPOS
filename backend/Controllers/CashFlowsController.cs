using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CashFlowsController : ControllerBase
{
    private readonly AppDbContext _db;
    public CashFlowsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CashFlowDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.CashFlows.FindAsync(dto.CashFlowId);
            if (existing == null)
                _db.CashFlows.Add(new CashFlow
                {
                    CashFlowId = dto.CashFlowId, OrderId = dto.OrderId, Amount = dto.Amount,
                    CashOutflow = dto.CashOutflow, TransactionDate = dto.TransactionDate,
                    Description = dto.Description
                });
            else
            {
                existing.Amount = dto.Amount; existing.CashOutflow = dto.CashOutflow;
                existing.Description = dto.Description;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.CashFlows.ToListAsync());
}
