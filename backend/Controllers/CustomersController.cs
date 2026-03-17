using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Extensions;
using iPosApi.Models.Data;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class CustomersController : ControllerBase
{
    private readonly ISyncService _sync;
    private readonly AppDbContext _db;
    private readonly IStoreDeviceService _storeDeviceService;

    public CustomersController(ISyncService sync, AppDbContext db, IStoreDeviceService storeDeviceService)
    {
        _sync = sync;
        _db = db;
        _storeDeviceService = storeDeviceService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var storeId = User.GetStoreId();
        if (string.IsNullOrEmpty(storeId))
            return Forbid();

        var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

        // Scope customers via InvoiceLines → Invoices → DeviceId
        var deviceInvoiceIds = await _db.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var customerIds = await _db.InvoiceLines
            .AsNoTracking()
            .Where(il => deviceInvoiceIds.Contains(il.InvoiceId))
            .Select(il => il.CustomerID)
            .Distinct()
            .ToListAsync();

        var customers = await _db.Customers
            .AsNoTracking()
            .Where(c => customerIds.Contains(c.CustomerId))
            .ToListAsync();

        return Ok(customers);
    }

    [HttpGet("{id:long}")]
    public async Task<IActionResult> GetById(long id)
    {
        var storeId = User.GetStoreId();
        if (string.IsNullOrEmpty(storeId))
            return Forbid();

        var allowedDeviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(storeId);

        // Verify this customer belongs to the store
        var deviceInvoiceIds = await _db.Invoices
            .AsNoTracking()
            .Where(i => i.DeviceId != null && allowedDeviceIds.Contains(i.DeviceId))
            .Select(i => i.InvoiceId)
            .ToListAsync();

        var customerIds = await _db.InvoiceLines
            .AsNoTracking()
            .Where(il => deviceInvoiceIds.Contains(il.InvoiceId))
            .Select(il => il.CustomerID)
            .Distinct()
            .ToListAsync();

        if (!customerIds.Contains(id))
            return NotFound();

        var customer = await _db.Customers.FindAsync(id);
        return customer is null ? NotFound() : Ok(customer);
    }

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CustomerDto> items)
    {
        var count = await _sync.SyncCustomersAsync(items);
        return Ok(new { synced = count });
    }
}
