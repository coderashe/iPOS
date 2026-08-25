using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Extensions;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/reports")]
[Authorize]
public class ReportsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly CacheService _cacheService;
    private readonly IStoreDeviceService _storeDeviceService;
    private readonly ILogger<ReportsController> _logger;

    public ReportsController(
        AppDbContext context,
        CacheService cacheService,
        IStoreDeviceService storeDeviceService,
        ILogger<ReportsController> logger)
    {
        _context = context;
        _cacheService = cacheService;
        _storeDeviceService = storeDeviceService;
        _logger = logger;
    }

    [HttpGet("sales")]
    public async Task<IActionResult> GetSalesReport(
        [FromQuery] DateTime? startDate = null,
        [FromQuery] DateTime? endDate = null,
        [FromQuery] string groupBy = "day")
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"sales_report_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}_{groupBy}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSalesReport(start, end, groupBy, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(report);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating sales report");
            return StatusCode(500, "An error occurred while generating the sales report");
        }
    }

    private async Task<SalesReportDto> GenerateSalesReport(
        DateTime startDate,
        DateTime endDate,
        string groupBy,
        IReadOnlyList<string> allowedDeviceIds)
    {
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        // Get invoice IDs for the allowed devices
        var deviceInvoiceIds = await _context.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .ToListAsync();

        var dataPoints = new List<SalesDataPointDto>();

        if (groupBy.ToLower() == "day")
        {
            dataPoints = invoiceLines
                .GroupBy(il => DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date)
                .Select(g => new SalesDataPointDto
                {
                    Date = g.Key,
                    Revenue = (decimal)g.Sum(il => il.TotalAmount),
                    TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                    ItemsSold = g.Sum(il => il.ProductQtySold)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else if (groupBy.ToLower() == "week")
        {
            dataPoints = invoiceLines
                .GroupBy(il => GetWeekStart(DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date))
                .Select(g => new SalesDataPointDto
                {
                    Date = g.Key,
                    Revenue = (decimal)g.Sum(il => il.TotalAmount),
                    TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                    ItemsSold = g.Sum(il => il.ProductQtySold)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else if (groupBy.ToLower() == "month")
        {
            dataPoints = invoiceLines
                .GroupBy(il => new DateTime(
                    DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Year,
                    DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Month, 1))
                .Select(g => new SalesDataPointDto
                {
                    Date = g.Key,
                    Revenue = (decimal)g.Sum(il => il.TotalAmount),
                    TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                    ItemsSold = g.Sum(il => il.ProductQtySold)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }

        return new SalesReportDto
        {
            DataPoints = dataPoints,
            TotalRevenue = dataPoints.Sum(d => d.Revenue),
            TotalTransactions = dataPoints.Sum(d => d.TransactionCount)
        };
    }

    private DateTime GetWeekStart(DateTime date)
    {
        int diff = (7 + (date.DayOfWeek - DayOfWeek.Monday)) % 7;
        return date.AddDays(-1 * diff).Date;
    }

    [HttpGet("sales/by-cashier")]
    public async Task<IActionResult> GetSalesByCashier(
        [FromQuery] DateTime? startDate = null,
        [FromQuery] DateTime? endDate = null)
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"sales_by_cashier_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSalesByCashierReport(start, end, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(report);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating sales by cashier report");
            return StatusCode(500, "An error occurred while generating the sales by cashier report");
        }
    }

    private async Task<List<SalesByCashierDto>> GenerateSalesByCashierReport(
        DateTime startDate,
        DateTime endDate,
        IReadOnlyList<string> allowedDeviceIds)
    {
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        // Get invoice IDs for the allowed devices
        var deviceInvoiceIds = await _context.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var salesByCashier = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .GroupBy(il => il.Cashier)
            .Select(g => new SalesByCashierDto
            {
                CashierName = g.Key,
                TotalSales = (decimal)g.Sum(il => il.TotalAmount),
                TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                AverageTransactionValue = (decimal)g.Average(il => il.TotalAmount)
            })
            .OrderByDescending(c => c.TotalSales)
            .ToListAsync();

        return salesByCashier;
    }

    [HttpGet("sales/by-hour")]
    public async Task<IActionResult> GetSalesByHour([FromQuery] DateTime? date = null)
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var targetDate = date ?? DateTime.Today;
            var cacheKey = $"sales_by_hour_{storeId}_{targetDate:yyyyMMdd}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSalesByHourReport(targetDate, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(report);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating sales by hour report");
            return StatusCode(500, "An error occurred while generating the sales by hour report");
        }
    }

    private async Task<List<SalesDataPointDto>> GenerateSalesByHourReport(
        DateTime date,
        IReadOnlyList<string> allowedDeviceIds)
    {
        var startOfDay = new DateTimeOffset(date.Date).ToUnixTimeMilliseconds();
        var endOfDay = new DateTimeOffset(date.Date.AddDays(1)).ToUnixTimeMilliseconds();

        // Get invoice IDs for the allowed devices
        var deviceInvoiceIds = await _context.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startOfDay
                      && il.InvoiceDate < endOfDay
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .ToListAsync();

        var salesByHour = invoiceLines
            .GroupBy(il => DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Hour)
            .Select(g => new SalesDataPointDto
            {
                Date = date.Date.AddHours(g.Key),
                Revenue = (decimal)g.Sum(il => il.TotalAmount),
                TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                ItemsSold = g.Sum(il => il.ProductQtySold)
            })
            .OrderBy(d => d.Date)
            .ToList();

        return salesByHour;
    }

    [HttpGet("inventory/stock-levels")]
    public async Task<IActionResult> GetStockLevels()
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var cacheKey = $"inventory_stock_levels_v2_{storeId}";

            var stockLevels = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateStockLevelsReport(allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(stockLevels);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating stock levels report");
            return StatusCode(500, "An error occurred while generating the stock levels report");
        }
    }

    private async Task<List<StockLevelDto>> GenerateStockLevelsReport(IReadOnlyList<string> allowedDeviceIds)
    {
        // Get all products — StoreId and DeviceId are not reliably populated across all
        // synced records, so we return all products with stock rather than risk filtering out
        // legitimate inventory. Scoping is enforced at the invoice/sales level.
        var products = await _context.Products
            .AsNoTracking()
            .Where(p => p.CurrentStock.HasValue)
            .ToListAsync();

        // Get invoice IDs for the allowed devices
        var thirtyDaysAgo = new DateTimeOffset(DateTime.Today.AddDays(-30)).ToUnixTimeMilliseconds();
        var today = new DateTimeOffset(DateTime.Today.AddDays(1)).ToUnixTimeMilliseconds();

        var deviceInvoiceIds = await _context.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var salesVelocity = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= thirtyDaysAgo
                      && il.InvoiceDate < today
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .GroupBy(il => il.ProductID)
            .Select(g => new
            {
                ProductId = g.Key,
                AverageDailySales = g.Sum(il => il.ProductQtySold) / 30.0
            })
            .ToListAsync();

        var velocityDict = salesVelocity.ToDictionary(v => v.ProductId, v => v.AverageDailySales);

        var stockLevels = products.Select(p =>
        {
            var currentStock = p.CurrentStock ?? 0;
            var avgDailySales = velocityDict.ContainsKey(p.Id) ? velocityDict[p.Id] : 0;
            var daysRemaining = avgDailySales > 0 ? (int)(currentStock / avgDailySales) : int.MaxValue;

            return new StockLevelDto
            {
                ProductId = p.Id,
                ProductCode = p.ProductCode,
                ProductName = p.Name,
                CurrentStock = currentStock,
                AverageDailySales = avgDailySales,
                DaysRemaining = daysRemaining == int.MaxValue ? null : daysRemaining
            };
        })
        .OrderBy(s => s.DaysRemaining ?? int.MaxValue)
        .ToList();

        return stockLevels;
    }

    [HttpGet("inventory/low-stock")]
    public async Task<IActionResult> GetLowStock([FromQuery] int threshold = 7)
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var cacheKey = $"inventory_low_stock_v2_{storeId}_{threshold}";

            var lowStock = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateLowStockReport(threshold, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(lowStock);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating low stock report");
            return StatusCode(500, "An error occurred while generating the low stock report");
        }
    }

    private async Task<List<StockLevelDto>> GenerateLowStockReport(int threshold, IReadOnlyList<string> allowedDeviceIds)
    {
        var allStockLevels = await GenerateStockLevelsReport(allowedDeviceIds);

        return allStockLevels
            .Where(s => s.DaysRemaining.HasValue && s.DaysRemaining.Value <= threshold)
            .ToList();
    }

    [HttpGet("revenue")]
    public async Task<IActionResult> GetRevenueReport(
        [FromQuery] DateTime? startDate = null,
        [FromQuery] DateTime? endDate = null,
        [FromQuery] string groupBy = "day")
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"revenue_report_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}_{groupBy}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateRevenueReport(start, end, groupBy, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(report);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating revenue report");
            return StatusCode(500, "An error occurred while generating the revenue report");
        }
    }

    private async Task<List<SalesDataPointDto>> GenerateRevenueReport(
        DateTime startDate,
        DateTime endDate,
        string groupBy,
        IReadOnlyList<string> allowedDeviceIds)
    {
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        // Get invoice IDs for the allowed devices
        var deviceInvoiceIds = await _context.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .ToListAsync();

        List<SalesDataPointDto> dataPoints;

        if (groupBy.ToLower() == "day")
        {
            dataPoints = invoiceLines
                .GroupBy(il => DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date)
                .Select(g => new SalesDataPointDto
                {
                    Date = g.Key,
                    Revenue = (decimal)g.Sum(il => il.TotalAmount),
                    TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                    ItemsSold = g.Sum(il => il.ProductQtySold)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else if (groupBy.ToLower() == "week")
        {
            dataPoints = invoiceLines
                .GroupBy(il => GetWeekStart(DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date))
                .Select(g => new SalesDataPointDto
                {
                    Date = g.Key,
                    Revenue = (decimal)g.Sum(il => il.TotalAmount),
                    TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                    ItemsSold = g.Sum(il => il.ProductQtySold)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else if (groupBy.ToLower() == "month")
        {
            dataPoints = invoiceLines
                .GroupBy(il => new DateTime(
                    DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Year,
                    DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Month, 1))
                .Select(g => new SalesDataPointDto
                {
                    Date = g.Key,
                    Revenue = (decimal)g.Sum(il => il.TotalAmount),
                    TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                    ItemsSold = g.Sum(il => il.ProductQtySold)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else
        {
            dataPoints = new List<SalesDataPointDto>();
        }

        return dataPoints;
    }
}
