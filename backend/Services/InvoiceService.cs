using iPosApi.Models;
using iPosApi.Models.Data;
using Microsoft.EntityFrameworkCore;

namespace iPosApi.Services;

public class InvoiceService : IInvoiceService
{
    private readonly AppDbContext _db;
    private readonly ILogger<InvoiceService> _logger;

    public InvoiceService(AppDbContext db, ILogger<InvoiceService> logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task<List<Invoice>> GetAllAsync(string? deviceId = null, string? storeId = null)
    {
        var query = _db.Invoices.AsNoTracking();
        if (!string.IsNullOrEmpty(deviceId))
            query = query.Where(i => i.DeviceId == deviceId);
        if (!string.IsNullOrEmpty(storeId))
            query = query.Where(i =>
                _db.Devices.Any(d => d.DeviceId == i.DeviceId && d.StoreId == storeId));
        return await query.ToListAsync();
    }

    public async Task<Invoice?> GetByIdAsync(long id) =>
        await _db.Invoices.FindAsync(id);

    public async Task<Invoice> UpsertAsync(Invoice invoice)
    {
        var existing = await _db.Invoices.FindAsync(invoice.InvoiceId);
        if (existing == null)
        {
            _db.Invoices.Add(invoice);
            _logger.LogInformation("Created invoice {InvoiceId}", invoice.InvoiceId);
        }
        else
        {
            existing.CustomerId = invoice.CustomerId; existing.TotalAmount = invoice.TotalAmount;
            existing.InvoiceDate = invoice.InvoiceDate; existing.DueDate = invoice.DueDate;
            existing.TheStatus = invoice.TheStatus; existing.CurrencyCode = invoice.CurrencyCode;
            existing.IsArchived = invoice.IsArchived; existing.DeviceId = invoice.DeviceId;
            existing.DeviceName = invoice.DeviceName; existing.StoreId = invoice.StoreId;
            existing.LastUpdated = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            _logger.LogInformation("Updated invoice {InvoiceId}", invoice.InvoiceId);
        }
        await _db.SaveChangesAsync();
        return existing ?? invoice;
    }

    /// <summary>Upsert a batch of invoices in a single SaveChanges call — much faster than one-by-one.</summary>
    public async Task UpsertBatchAsync(List<Invoice> invoices)
    {
        if (invoices.Count == 0) return;

        var ids = invoices.Select(i => i.InvoiceId).ToList();
        var existingMap = await _db.Invoices
            .Where(i => ids.Contains(i.InvoiceId))
            .ToDictionaryAsync(i => i.InvoiceId);

        foreach (var invoice in invoices)
        {
            if (existingMap.TryGetValue(invoice.InvoiceId, out var existing))
            {
                existing.CustomerId = invoice.CustomerId; existing.TotalAmount = invoice.TotalAmount;
                existing.InvoiceDate = invoice.InvoiceDate; existing.DueDate = invoice.DueDate;
                existing.TheStatus = invoice.TheStatus; existing.CurrencyCode = invoice.CurrencyCode;
                existing.IsArchived = invoice.IsArchived; existing.DeviceId = invoice.DeviceId;
                existing.DeviceName = invoice.DeviceName; existing.StoreId = invoice.StoreId;
                existing.LastUpdated = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            }
            else
            {
                _db.Invoices.Add(invoice);
            }
        }

        await _db.SaveChangesAsync(); // Single round trip for the whole batch
        _logger.LogInformation("Batch upserted {Count} invoices", invoices.Count);
    }

    public async Task<bool> DeleteAsync(long id)
    {
        var invoice = await _db.Invoices.FindAsync(id);
        if (invoice == null) return false;
        _db.Invoices.Remove(invoice);
        await _db.SaveChangesAsync();
        _logger.LogInformation("Deleted invoice {InvoiceId}", id);
        return true;
    }
}
