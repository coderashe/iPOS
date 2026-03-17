using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Extensions;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/analytics")]
[Authorize]
public class AnalyticsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly CacheService _cacheService;
    private readonly IStoreDeviceService _storeDeviceService;
    private readonly ILogger<AnalyticsController> _logger;

    public AnalyticsController(
        AppDbContext context,
        CacheService cacheService,
        IStoreDeviceService storeDeviceService,
        ILogger<AnalyticsController> logger)
    {
        _context = context;
        _cacheService = cacheService;
        _storeDeviceService = storeDeviceService;
        _logger = logger;
    }

    [HttpGet("top-products")]
    public async Task<IActionResult> GetTopProducts(
        [FromQuery] DateTime? startDate = null,
        [FromQuery] DateTime? endDate = null,
        [FromQuery] int limit = 10)
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var start = startDate ?? DateTime.Today.AddDays(-30);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"top_products_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}_{limit}";

            var topProducts = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateTopProductsReport(start, end, limit, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(topProducts);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating top products report");
            return StatusCode(500, "An error occurred while generating the top products report");
        }
    }

    private async Task<List<TopProductDto>> GenerateTopProductsReport(
        DateTime startDate,
        DateTime endDate,
        int limit,
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

        var topProducts = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .GroupBy(il => new { il.ProductID, il.ProductName })
            .Select(g => new TopProductDto
            {
                ProductId = g.Key.ProductID,
                ProductName = g.Key.ProductName,
                QuantitySold = g.Sum(il => il.ProductQtySold),
                Revenue = (decimal)g.Sum(il => il.TotalAmount)
            })
            .OrderByDescending(p => p.Revenue)
            .Take(limit)
            .ToListAsync();

        return topProducts;
    }

    [HttpGet("customer-insights")]
    public async Task<IActionResult> GetCustomerInsights(
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

            var cacheKey = $"customer_insights_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}";

            var insights = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateCustomerInsights(start, end, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(insights);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating customer insights");
            return StatusCode(500, "An error occurred while generating customer insights");
        }
    }

    private async Task<CustomerInsightsDto> GenerateCustomerInsights(
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

        // Get customer IDs that have purchased from this store's devices
        var storeCustomerIds = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => deviceInvoiceIds.Contains(il.InvoiceId))
            .Select(il => il.CustomerID)
            .Distinct()
            .ToListAsync();

        // Total customers scoped to this store (via Orders linked to store customers)
        var totalCustomers = await _context.Orders
            .AsNoTracking()
            .Where(o => storeCustomerIds.Contains(o.CustomerId))
            .Select(o => o.CustomerId)
            .Distinct()
            .CountAsync();

        // New customers in date range scoped to this store
        var newCustomers = await _context.Orders
            .AsNoTracking()
            .Where(o => storeCustomerIds.Contains(o.CustomerId)
                     && o.CreatedAt >= startTimestamp
                     && o.CreatedAt <= endTimestamp)
            .GroupBy(o => o.CustomerId)
            .Where(g => g.Min(o => o.CreatedAt) >= startTimestamp)
            .CountAsync();

        // Top customers by total spent, scoped to store's devices
        var topCustomers = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .GroupBy(il => new { il.CustomerID, il.CustomerName, il.CustomerPhone })
            .Select(g => new TopCustomerDto
            {
                CustomerId = g.Key.CustomerID,
                CustomerName = g.Key.CustomerName,
                CustomerPhone = g.Key.CustomerPhone,
                TotalSpent = (decimal)g.Sum(il => il.TotalAmount),
                PurchaseCount = g.Select(il => il.InvoiceId).Distinct().Count()
            })
            .OrderByDescending(c => c.TotalSpent)
            .Take(10)
            .ToListAsync();

        // Calculate average customer value scoped to store's devices
        var totalRevenue = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .SumAsync(il => (decimal)il.TotalAmount);

        var customerCount = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .Select(il => il.CustomerID)
            .Distinct()
            .CountAsync();

        var averageCustomerValue = customerCount > 0 ? totalRevenue / customerCount : 0;

        // Total receivables from Orders scoped to store's customers
        var totalReceivables = await _context.Orders
            .AsNoTracking()
            .Where(o => storeCustomerIds.Contains(o.CustomerId)
                     && (o.Status == "pending" || o.Status == "partial"))
            .SumAsync(o => o.TotalAmount - o.PaidAmount);

        return new CustomerInsightsDto
        {
            TotalCustomers = totalCustomers,
            NewCustomers = newCustomers,
            TopCustomers = topCustomers,
            AverageCustomerValue = averageCustomerValue,
            TotalReceivables = totalReceivables
        };
    }

    [HttpGet("revenue-trends")]
    public async Task<IActionResult> GetRevenueTrends(
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

            var cacheKey = $"revenue_trends_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}_{groupBy}";

            var trends = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateRevenueTrends(start, end, groupBy, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(trends);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating revenue trends");
            return StatusCode(500, "An error occurred while generating revenue trends");
        }
    }

    private async Task<RevenueTrendDto> GenerateRevenueTrends(
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

        List<TrendDataPointDto> dataPoints;

        if (groupBy.ToLower() == "day")
        {
            dataPoints = invoiceLines
                .GroupBy(il => DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date)
                .Select(g => new TrendDataPointDto
                {
                    Date = g.Key,
                    Value = (decimal)g.Sum(il => il.TotalAmount)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else if (groupBy.ToLower() == "week")
        {
            dataPoints = invoiceLines
                .GroupBy(il => GetWeekStart(DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date))
                .Select(g => new TrendDataPointDto
                {
                    Date = g.Key,
                    Value = (decimal)g.Sum(il => il.TotalAmount)
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
                .Select(g => new TrendDataPointDto
                {
                    Date = g.Key,
                    Value = (decimal)g.Sum(il => il.TotalAmount)
                })
                .OrderBy(d => d.Date)
                .ToList();
        }
        else
        {
            dataPoints = new List<TrendDataPointDto>();
        }

        decimal growthRate = 0;
        string trend = "stable";

        if (dataPoints.Count >= 2)
        {
            var firstValue = dataPoints.First().Value;
            var lastValue = dataPoints.Last().Value;

            if (firstValue > 0)
            {
                growthRate = ((lastValue - firstValue) / firstValue) * 100;
            }

            if (growthRate > 5)
                trend = "increasing";
            else if (growthRate < -5)
                trend = "decreasing";
        }

        return new RevenueTrendDto
        {
            DataPoints = dataPoints,
            GrowthRate = growthRate,
            Trend = trend
        };
    }

    private DateTime GetWeekStart(DateTime date)
    {
        int diff = (7 + (date.DayOfWeek - DayOfWeek.Monday)) % 7;
        return date.AddDays(-1 * diff).Date;
    }

    [HttpGet("product-performance/{productId}")]
    public async Task<IActionResult> GetProductPerformance(
        long productId,
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

            var cacheKey = $"product_performance_{storeId}_{productId}_{start:yyyyMMdd}_{end:yyyyMMdd}";

            var performance = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GenerateProductPerformance(productId, start, end, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(performance);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating product performance report");
            return StatusCode(500, "An error occurred while generating the product performance report");
        }
    }

    private async Task<object> GenerateProductPerformance(
        long productId,
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

        var salesHistory = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.ProductID == productId
                      && il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .GroupBy(il => DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Date)
            .Select(g => new
            {
                Date = g.Key,
                QuantitySold = g.Sum(il => il.ProductQtySold),
                Revenue = (decimal)g.Sum(il => il.TotalAmount)
            })
            .OrderBy(s => s.Date)
            .ToListAsync();

        var totalQuantitySold = salesHistory.Sum(s => s.QuantitySold);
        var totalRevenue = salesHistory.Sum(s => s.Revenue);
        var averageDailySales = salesHistory.Any() ? totalQuantitySold / (decimal)salesHistory.Count : 0;

        return new
        {
            ProductId = productId,
            SalesHistory = salesHistory,
            TotalQuantitySold = totalQuantitySold,
            TotalRevenue = totalRevenue,
            AverageDailySales = averageDailySales
        };
    }

    [HttpGet("peak-hours")]
    public async Task<IActionResult> GetPeakHours(
        [FromQuery] DateTime? startDate = null,
        [FromQuery] DateTime? endDate = null)
    {
        try
        {
            var storeId = User.GetStoreId();
            if (string.IsNullOrEmpty(storeId))
                return Forbid();

            var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

            var start = startDate ?? DateTime.Today.AddDays(-7);
            var end = endDate ?? DateTime.Today.AddDays(1);

            var cacheKey = $"peak_hours_{storeId}_{start:yyyyMMdd}_{end:yyyyMMdd}";

            var peakHours = await _cacheService.GetOrCreateAsync(
                cacheKey,
                async () => await GeneratePeakHoursReport(start, end, allowedDeviceIds),
                TimeSpan.FromMinutes(15)
            );

            return Ok(peakHours);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error generating peak hours report");
            return StatusCode(500, "An error occurred while generating the peak hours report");
        }
    }

    private async Task<List<object>> GeneratePeakHoursReport(
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

        var invoiceLines = await _context.InvoiceLines
            .AsNoTracking()
            .Where(il => il.InvoiceDate >= startTimestamp
                      && il.InvoiceDate <= endTimestamp
                      && deviceInvoiceIds.Contains(il.InvoiceId))
            .ToListAsync();

        var hourlyData = invoiceLines
            .GroupBy(il => DateTimeOffset.FromUnixTimeMilliseconds(il.InvoiceDate).Hour)
            .Select(g => new
            {
                Hour = g.Key,
                TransactionCount = g.Select(il => il.InvoiceId).Distinct().Count(),
                Revenue = (decimal)g.Sum(il => il.TotalAmount),
                AverageTransactionValue = (decimal)g.Average(il => il.TotalAmount)
            })
            .OrderByDescending(h => h.TransactionCount)
            .ToList<object>();

        return hourlyData;
    }
}
