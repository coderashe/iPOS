using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/dashboard")]
public class DashboardController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly CacheService _cacheService;
    private readonly ILogger<DashboardController> _logger;

    public DashboardController(
        AppDbContext context,
        CacheService cacheService,
        ILogger<DashboardController> logger)
    {
        _context = context;
        _cacheService = cacheService;
        _logger = logger;
    }

    [HttpGet("summary")]
    public async Task<IActionResult> GetSummary(
        [FromQuery] DateTime? startDate = null,
        [FromQuery] DateTime? endDate = null)
    {
        try
        {
            // Default to last 30 days if not specified
            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"dashboard_summary_{start:yyyyMMdd}_{end:yyyyMMdd}";

            var summary = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSummary(start, end),
                TimeSpan.FromMinutes(5)
            );

            return Ok(summary);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating dashboard summary");
            return StatusCode(500, "An error occurred while generating the dashboard summary");
        }
    }

    private async Task<DashboardSummaryDto> GenerateSummary(DateTime startDate, DateTime endDate)
    {
        // Convert DateTime to Unix timestamp (milliseconds)
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        // Query invoice lines within date range
        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp && il.InvoiceDate <= endTimestamp)
            .ToListAsync();

        // Calculate total sales and revenue
        var totalRevenue = invoiceLines.Sum(il => (decimal)il.TotalAmount);
        var transactionCount = invoiceLines.Select(il => il.InvoiceId).Distinct().Count();
        var averageTransactionValue = transactionCount > 0 ? totalRevenue / transactionCount : 0;

        // Get top 5 products by quantity sold
        var topProducts = invoiceLines
            .GroupBy(il => new { il.ProductID, il.ProductName })
            .Select(g => new TopProductDto
            {
                ProductId = g.Key.ProductID,
                ProductName = g.Key.ProductName,
                QuantitySold = g.Sum(il => il.ProductQtySold),
                Revenue = (decimal)g.Sum(il => il.TotalAmount)
            })
            .OrderByDescending(p => p.QuantitySold)
            .Take(5)
            .ToList();

        // Get low stock count (products with stock < 10)
        var lowStockCount = await _context.Products
            .AsNoTracking()
            .CountAsync(p => p.Price < 10);

        // Get active customers count
        var activeCustomersCount = invoiceLines
            .Select(il => il.CustomerID)
            .Distinct()
            .Count();

        return new DashboardSummaryDto
        {
            TotalSales = invoiceLines.Sum(il => il.ProductQtySold),
            TotalRevenue = totalRevenue,
            TransactionCount = transactionCount,
            AverageTransactionValue = averageTransactionValue,
            TopProducts = topProducts,
            LowStockCount = lowStockCount,
            ActiveCustomersCount = activeCustomersCount,
            LastSyncTime = DateTime.UtcNow
        };
    }
}
