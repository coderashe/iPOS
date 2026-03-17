using iPosApi.Models.Data;
using Microsoft.EntityFrameworkCore;

namespace iPosApi.Services;

public class StoreDeviceService : IStoreDeviceService
{
    private readonly AppDbContext _db;
    private readonly ILogger<StoreDeviceService> _logger;

    public StoreDeviceService(AppDbContext db, ILogger<StoreDeviceService> logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task<IReadOnlyList<string>> GetDeviceIdsForStoreAsync(string? storeId)
    {
        if (string.IsNullOrEmpty(storeId))
        {
            _logger.LogWarning("GetDeviceIdsForStoreAsync called with null or empty storeId");
            return Array.Empty<string>();
        }

        var deviceIds = await _db.Devices
            .AsNoTracking()
            .Where(d => d.StoreId == storeId)
            .Select(d => d.DeviceId)
            .ToListAsync();

        return deviceIds;
    }
}
