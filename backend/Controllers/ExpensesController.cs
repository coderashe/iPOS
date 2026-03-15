using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ExpensesController : ControllerBase
{
    private readonly AppDbContext _db;
    public ExpensesController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ExpenseDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.Expenses.FindAsync(dto.ExpenseId);
            if (existing == null)
                _db.Expenses.Add(new Expense
                {
                    ExpenseId = dto.ExpenseId, Description = dto.Description,
                    Amount = dto.Amount, ExpenseDate = dto.ExpenseDate, Category = dto.Category
                });
            else
            {
                existing.Description = dto.Description; existing.Amount = dto.Amount;
                existing.ExpenseDate = dto.ExpenseDate; existing.Category = dto.Category;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.Expenses.ToListAsync());
}
