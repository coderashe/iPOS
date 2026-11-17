using Microsoft.AspNetCore.Mvc;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DevicesController : ControllerBase
{
    private readonly IDeviceService _service;

    public DevicesController(IDeviceService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());

    [HttpGet("{deviceId}")]
    public async Task<IActionResult> GetById(string deviceId)
    {
        var device = await _service.GetByIdAsync(deviceId);
        return device is null ? NotFound() : Ok(device);
    }

    /// <summary>Batch sync devices from Android. Accepts DeviceSyncDto list.</summary>
    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<DeviceSyncDto> dtos)
    {
        var devices = dtos.Select(d => new Device
        {
            DeviceId = d.DeviceId,
            DeviceName = d.DeviceName,
            StoreId = d.StoreId,
            LastSeen = d.LastSeen
        }).ToList();

        var count = await _service.SyncAsync(devices);
        return Ok(new { synced = count });
    }

    /// <summary>Register or update a device. Called by Android on device setup.</summary>
    [HttpPost]
    public async Task<IActionResult> Upsert([FromBody] Device device) =>
        Ok(await _service.UpsertAsync(device));

    /// <summary>Resolve storeId for a given deviceId — used by frontend for session filtering.</summary>
    [HttpGet("{deviceId}/store")]
    public async Task<IActionResult> GetStore(string deviceId)
    {
        var storeId = await _service.GetStoreIdAsync(deviceId);
        return storeId is null ? NotFound() : Ok(new { storeId });
    }
}
