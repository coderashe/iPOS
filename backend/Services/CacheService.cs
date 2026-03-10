using Microsoft.Extensions.Caching.Memory;

namespace iPosApi.Services;

public class CacheService
{
    private readonly IMemoryCache _cache;
    private readonly ILogger<CacheService> _logger;

    public CacheService(IMemoryCache cache, ILogger<CacheService> logger)
    {
        _cache = cache;
        _logger = logger;
    }

    public async Task<T> GetOrCreateAsync<T>(
        string key,
        Func<Task<T>> factory,
        TimeSpan? expiration = null)
    {
        if (_cache.TryGetValue(key, out T? cachedResult) && cachedResult != null)
        {
            _logger.LogInformation("Cache hit for key: {Key}", key);
            return cachedResult;
        }

        _logger.LogInformation("Cache miss for key: {Key}. Fetching data...", key);
        var result = await factory();

        var cacheOptions = new MemoryCacheEntryOptions()
            .SetAbsoluteExpiration(expiration ?? TimeSpan.FromMinutes(5));

        _cache.Set(key, result, cacheOptions);
        _logger.LogInformation("Data cached for key: {Key} with expiration: {Expiration}", key, expiration ?? TimeSpan.FromMinutes(5));

        return result;
    }

    public void Remove(string key)
    {
        _cache.Remove(key);
        _logger.LogInformation("Cache cleared for key: {Key}", key);
    }

    public void Clear()
    {
        if (_cache is MemoryCache memoryCache)
        {
            memoryCache.Compact(1.0);
            _logger.LogInformation("Cache cleared completely");
        }
    }
}
