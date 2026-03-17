namespace iPosApi.Services;

public interface IStoreDeviceService
{
    /// <summary>
    /// Returns the list of deviceIds that belong to the given store.
    /// Returns an empty list if storeId is null or no devices are found.
    /// </summary>
    Task<IReadOnlyList<string>> GetDeviceIdsForStoreAsync(string? storeId);
}
