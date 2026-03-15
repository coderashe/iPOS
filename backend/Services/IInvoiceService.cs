using iPosApi.Models;

namespace iPosApi.Services;

public interface IInvoiceService
{
    Task<List<Invoice>> GetAllAsync(string? deviceId = null, string? storeId = null);
    Task<Invoice?> GetByIdAsync(long id);
    Task<Invoice> UpsertAsync(Invoice invoice);
    Task<bool> DeleteAsync(long id);
}
