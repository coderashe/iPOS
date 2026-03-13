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
        [FromQuery] DateTime? endDate = null,
        [FromQuery] string? deviceId = null)
    {
        try
        {
            // Default to last 30 days if not specified
            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"dashboard_summary_{start:yyyyMMdd}_{end:yyyyMMdd}_{deviceId ?? "all"}";

            var summary = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSummary(start, end, deviceId),
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

    [HttpGet("devices")]
    public async Task<IActionResult> GetDevices()
    {
        try
        {
            var cacheKey = "devices_list";

            var devices = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () =>
                {
                    // Get unique devices from invoices
                    var invoiceDevices = await _context.Invoices
                        .AsNoTracking()
                        .Where(i => !string.IsNullOrEmpty(i.DeviceId))
                        .Select(i => new { i.DeviceId, i.DeviceName })
                        .Distinct()
                        .ToListAsync();

                    // Get unique devices from products
                    var productDevices = await _context.Products
                        .AsNoTracking()
                        .Where(p => !string.IsNullOrEmpty(p.DeviceId))
                        .Select(p => new { p.DeviceId, p.DeviceName })
                        .Distinct()
                        .ToListAsync();

                    // Combine and deduplicate
                    var allDevices = invoiceDevices
                        .Union(productDevices)
                        .GroupBy(d => d.DeviceId)
                        .Select(g => new DeviceDto
                        {
                            DeviceId = g.Key!,
                            DeviceName = g.First().DeviceName ?? g.Key!
                        })
                        .OrderBy(d => d.DeviceName)
                        .ToList();

                    return allDevices;
                },
                TimeSpan.FromMinutes(15)
            );

            return Ok(devices);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching devices");
            return StatusCode(500, "An error occurred while fetching devices");
        }
    }

    private async Task<DashboardSummaryDto> GenerateSummary(DateTime startDate, DateTime endDate, string? deviceId = null)
    {
        // Convert DateTime to Unix timestamp (milliseconds)
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        // Query invoice lines within date range
        var invoiceLinesQuery = _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp && il.InvoiceDate <= endTimestamp);

        // Filter by device if specified
        if (!string.IsNullOrEmpty(deviceId))
        {
            var deviceInvoiceIds = await _context.Invoices
                .AsNoTracking()
                .Where(i => i.DeviceId == deviceId)
                .Select(i => i.InvoiceId)
                .ToListAsync();

            invoiceLinesQuery = invoiceLinesQuery.Where(il => deviceInvoiceIds.Contains(il.InvoiceId));
        }

        var invoiceLines = await invoiceLinesQuery.ToListAsync();

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
        var lowStockQuery = _context.Products.AsNoTracking();
        if (!string.IsNullOrEmpty(deviceId))
        {
            lowStockQuery = lowStockQuery.Where(p => p.DeviceId == deviceId);
        }
        var lowStockCount = await lowStockQuery.CountAsync(p => p.Price < 10);

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
