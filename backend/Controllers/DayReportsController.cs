using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DayReportsController : ControllerBase
{
    private readonly AppDbContext _db;
    public DayReportsController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<DayReportDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.DayReports.FindAsync(dto.ReportId);
            if (existing == null)
                _db.DayReports.Add(new DayReport
                {
                    ReportId = dto.ReportId, TotalInvoices = dto.TotalInvoices,
                    TotalOrders = dto.TotalOrders, TotalProductsSold = dto.TotalProductsSold,
                    TotalInvoiceSales = dto.TotalInvoiceSales, ReportDate = dto.ReportDate,
                    StartTime = dto.StartTime, EndTime = dto.EndTime
                });
            else
            {
                existing.TotalInvoices = dto.TotalInvoices; existing.TotalOrders = dto.TotalOrders;
                existing.TotalProductsSold = dto.TotalProductsSold;
                existing.TotalInvoiceSales = dto.TotalInvoiceSales; existing.EndTime = dto.EndTime;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.DayReports.ToListAsync());
}
