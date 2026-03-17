using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Extensions;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/dashboard")]
[Authorize]
public class DashboardController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly CacheService _cacheService;
    private readonly IStoreDeviceService _storeDeviceService;
    private readonly ILogger<DashboardController> _logger;

    public DashboardController(
        AppDbContext context,
        CacheService cacheService,
        IStoreDeviceService storeDeviceService,
        ILogger<DashboardController> logger)
    {
        _context = context;
        _cacheService = cacheService;
        _storeDeviceService = storeDeviceService;
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
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            // If a specific deviceId is requested, verify it belongs to this store
            string? effectiveDeviceId = null;
            if (!string.IsNullOrEmpty(deviceId))
            {
                if (!allowedDeviceIds.Contains(deviceId))
                    return Forbid();
                effectiveDeviceId = deviceId;
            }

            // Default to last 30 days if not specified
            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"dashboard_summary_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}_{effectiveDeviceId ?? "all"}";

            var summary = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSummary(start, end, allowedDeviceIds, effectiveDeviceId),
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
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var cacheKey = $"devices_list_{storeId}";

            var devices = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () =>
                {
                    // Get unique devices from invoices scoped to this store's devices
                    var invoiceDevices = await _context.Invoices
                        .AsNoTracking()
                        .Where(i => !string.IsNullOrEmpty(i.DeviceId) && allowedDeviceIds.Contains(i.DeviceId!))
                        .Select(i => new { i.DeviceId, i.DeviceName })
                        .Distinct()
                        .ToListAsync();

                    // Get unique devices from products scoped to this store's devices
                    var productDevices = await _context.Products
                        .AsNoTracking()
                        .Where(p => !string.IsNullOrEmpty(p.DeviceId) && allowedDeviceIds.Contains(p.DeviceId!))
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

    private async Task<DashboardSummaryDto> GenerateSummary(
        DateTime startDate,
        DateTime endDate,
        IReadOnlyList<string> allowedDeviceIds,
        string? deviceId = null)
    {
        // Convert DateTime to Unix timestamp (milliseconds)
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        // Determine the effective device filter
        var effectiveDeviceIds = deviceId != null
            ? new List<string> { deviceId }
            : allowedDeviceIds.ToList();

        // Get invoice IDs for the allowed devices
        var deviceInvoiceIds = await _context.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && effectiveDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        // Query invoice lines within date range, scoped to store's devices
        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
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

        // Get low stock count (products with stock < 10) scoped to store's devices
        var lowStockCount = await _context.Products
            .AsNoTracking()
            .Where(p => p.DeviceId != null && effectiveDeviceIds.Contains(p.DeviceId))
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
