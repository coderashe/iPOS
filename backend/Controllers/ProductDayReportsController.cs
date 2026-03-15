using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ProductDayReportsController : ControllerBase
{
    private readonly AppDbContext _db;
    public ProductDayReportsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<ProductDayReportDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.ProductDayReports.FindAsync(dto.ReportId);
            if (existing == null)
                _db.ProductDayReports.Add(new ProductDayReport
                {
                    ReportId = dto.ReportId, ProductId = dto.ProductId, ProductName = dto.ProductName,
                    OpeningStock = dto.OpeningStock, ClosingStock = dto.ClosingStock,
                    QuantitySold = dto.QuantitySold, TotalSales = dto.TotalSales,
                    TotalCost = dto.TotalCost, TotalProfit = dto.TotalProfit, ReportDate = dto.ReportDate
                });
            else
            {
                existing.ClosingStock = dto.ClosingStock; existing.QuantitySold = dto.QuantitySold;
                existing.TotalSales = dto.TotalSales; existing.TotalCost = dto.TotalCost;
                existing.TotalProfit = dto.TotalProfit;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.ProductDayReports.ToListAsync());
}
