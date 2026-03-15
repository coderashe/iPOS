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

    public async Task<List<Invoice>> GetAllAsync(string? deviceId = null)
    {
        var query = _db.Invoices.AsNoTracking();
        if (!string.IsNullOrEmpty(deviceId))
            query = query.Where(i => i.DeviceId == deviceId);
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
            existing.DeviceName = invoice.DeviceName;
            existing.LastUpdated = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            _logger.LogInformation("Updated invoice {InvoiceId}", invoice.InvoiceId);
        }
        await _db.SaveChangesAsync();
        return existing ?? invoice;
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
