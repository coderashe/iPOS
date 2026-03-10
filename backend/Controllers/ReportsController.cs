using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/reports")]
public class ReportsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly CacheService _cacheService;
    private readonly ILogger<ReportsController> _logger;

    public ReportsController(
        AppDbContext context,
        CacheService cacheService,
        ILogger<ReportsController> logger)
    {
        _context = context;
        _cacheService = cacheService;
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
            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"sales_report_{start:yyyyMMdd}_{end:yyyyMMdd}_{groupBy}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSalesReport(start, end, groupBy),
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

    private async Task<SalesReportDto> GenerateSalesReport(DateTime startDate, DateTime endDate, string groupBy)
    {
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp && il.InvoiceDate <= endTimestamp)
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
            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"sales_by_cashier_{start:yyyyMMdd}_{end:yyyyMMdd}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSalesByCashierReport(start, end),
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

    private async Task<List<SalesByCashierDto>> GenerateSalesByCashierReport(DateTime startDate, DateTime endDate)
    {
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        var salesByCashier = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp && il.InvoiceDate <= endTimestamp)
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
            var targetDate = date ?? DateTime.Today;
            var cacheKey = $"sales_by_hour_{targetDate:yyyyMMdd}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateSalesByHourReport(targetDate),
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

    private async Task<List<SalesDataPointDto>> GenerateSalesByHourReport(DateTime date)
    {
        var startOfDay = new DateTimeOffset(date.Date).ToUnixTimeMilliseconds();
        var endOfDay = new DateTimeOffset(date.Date.AddDays(1)).ToUnixTimeMilliseconds();

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startOfDay && il.InvoiceDate < endOfDay)
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
            var cacheKey = "inventory_stock_levels";

            var stockLevels = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateStockLevelsReport(),
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

    private async Task<List<StockLevelDto>> GenerateStockLevelsReport()
    {
        // Get products with current stock
        var products = await _context.Products
            .AsNoTracking()
            .Where(p => p.CurrentStock.HasValue)
            .ToListAsync();

        // Calculate sales velocity (average daily sales) for each product
        var thirtyDaysAgo = new DateTimeOffset(DateTime.Today.AddDays(-30)).ToUnixTimeMilliseconds();
        var today = new DateTimeOffset(DateTime.Today.AddDays(1)).ToUnixTimeMilliseconds();

        var salesVelocity = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= thirtyDaysAgo && il.InvoiceDate < today)
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
            var cacheKey = $"inventory_low_stock_{threshold}";

            var lowStock = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateLowStockReport(threshold),
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

    private async Task<List<StockLevelDto>> GenerateLowStockReport(int threshold)
    {
        var allStockLevels = await GenerateStockLevelsReport();
        
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
            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"revenue_report_{start:yyyyMMdd}_{end:yyyyMMdd}_{groupBy}";

            var report = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateRevenueReport(start, end, groupBy),
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

    private async Task<List<SalesDataPointDto>> GenerateRevenueReport(DateTime startDate, DateTime endDate, string groupBy)
    {
        var startTimestamp = new DateTimeOffset(startDate).ToUnixTimeMilliseconds();
        var endTimestamp = new DateTimeOffset(endDate).ToUnixTimeMilliseconds();

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp && il.InvoiceDate <= endTimestamp)
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
