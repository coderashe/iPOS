using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Services;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class InvoicesController : ControllerBase
{
    private readonly IInvoiceService _service;




















































































































































































































    public InvoicesController(IInvoiceService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll(
        [FromQuery] string? deviceId = null,
        [FromQuery] string? storeId = null) =>
        Ok(await _service.GetAllAsync(deviceId, storeId));

    [HttpGet("{id:long}")]
    public async Task<IActionResult> GetById(long id)
    {
        var invoice = await _service.GetByIdAsync(id);
        return invoice is null ? NotFound() : Ok(invoice);
    }

    /// <summary>Upsert a single invoice</summary>
    [HttpPost]
    public async Task<IActionResult> Upsert([FromBody] Invoice invoice) =>
        Ok(await _service.UpsertAsync(invoice));

    /// <summary>Batch sync from Android</summary>
    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<Invoice> invoices)
    {
        foreach (var invoice in invoices)
            await _service.UpsertAsync(invoice);
        return Ok(new { synced = invoices.Count });
    }

    [HttpDelete("{id:long}")]
    public async Task<IActionResult> Delete(long id)
    {
        var deleted = await _service.DeleteAsync(id);
        return deleted ? NoContent() : NotFound();
    }
}
