using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/invoices")]
public class InvoicesController : ControllerBase
{
    private readonly AppDbContext _context;

    public InvoicesController(AppDbContext context)
    {
        _context = context;
    }

    [HttpPost]
    public IActionResult Create(Invoice invoice)
    {
        _context.Invoices.Add(invoice);
        _context.SaveChanges();
        return Ok(invoice);
    }

    [HttpGet]
    public IActionResult GetAll()
    {
        return Ok(_context.Invoices.ToList());
    }

    [HttpGet("{id}")]
    public IActionResult GetById(long id)
    {
        var invoice = _context.Invoices.Find(id);
        if (invoice == null)
            return NotFound();
        return Ok(invoice);
    }

    [HttpPut("{id}")]
    public IActionResult Update(long id, Invoice invoice)
    {
        var existing = _context.Invoices.Find(id);
        if (existing == null)
            return NotFound();

        existing.CustomerId = invoice.CustomerId;
        existing.TotalAmount = invoice.TotalAmount;
        existing.InvoiceDate = invoice.InvoiceDate;
        existing.DueDate = invoice.DueDate;
        existing.TheStatus = invoice.TheStatus;
        existing.CurrencyCode = invoice.CurrencyCode;
        existing.LastUpdated = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        existing.IsArchived = invoice.IsArchived;
        existing.DeviceId = invoice.DeviceId;
        existing.DeviceName = invoice.DeviceName;

        _context.SaveChanges();
        return Ok(existing);
    }

    [HttpDelete("{id}")]
    public IActionResult Delete(long id)
    {
        var invoice = _context.Invoices.Find(id);
        if (invoice == null)
            return NotFound();

        _context.Invoices.Remove(invoice);
        _context.SaveChanges();
        return NoContent();
    }
}
