using iPosApi.Models;

namespace iPosApi.Services;

public interface IDeviceService
{
    Task<List<Device>> GetAllAsync();
    Task<Device?> GetByIdAsync(string deviceId);
    Task<Device> UpsertAsync(Device device);

    Task<int> SyncAsync(List<Device> devices);

    /// <summary>Resolve the StoreId for a given DeviceId. Returns null if device unknown.</summary>
    Task<string?> GetStoreIdAsync(string deviceId);
}
