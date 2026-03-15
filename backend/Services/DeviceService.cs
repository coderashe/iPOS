using iPosApi.Models;
using iPosApi.Models.Data;
using Microsoft.EntityFrameworkCore;

namespace iPosApi.Services;

public class DeviceService : IDeviceService
{
    private readonly AppDbContext _db;
    private readonly ILogger<DeviceService> _logger;

    public DeviceService(AppDbContext db, ILogger<DeviceService> logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task<List<Device>> GetAllAsync() =>
        await _db.Devices.AsNoTracking().ToListAsync();

    public async Task<Device?> GetByIdAsync(string deviceId) =>
        await _db.Devices.FindAsync(deviceId);

    public async Task<Device> UpsertAsync(Device device)
    {
        device.LastSeen = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        var existing = await _db.Devices.FindAsync(device.DeviceId);
        if (existing == null)
        {
            _db.Devices.Add(device);
            _logger.LogInformation("Registered device {Id} for store {Store}", device.DeviceId, device.StoreId);
        }
        else
        {
            existing.DeviceName = device.DeviceName;
            existing.StoreId = device.StoreId;
            existing.LastSeen = device.LastSeen;
            _logger.LogInformation("Updated device {Id}", device.DeviceId);
        }
        await _db.SaveChangesAsync();
        return existing ?? device;
    }

    public async Task<int> SyncAsync(List<Device> devices)
    {
        int count = 0;
        foreach (var device in devices)
        {
            await UpsertAsync(device);
            count++;
        }
        return count;
    }

    public async Task<string?> GetStoreIdAsync(string deviceId) =>
        await _db.Devices
            .AsNoTracking()
            .Where(d => d.DeviceId == deviceId)
            .Select(d => d.StoreId)
            .FirstOrDefaultAsync();
}
