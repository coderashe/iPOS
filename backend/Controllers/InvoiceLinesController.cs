using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/invoicelines")]
public class InvoiceLinesController : ControllerBase
{
    private readonly AppDbContext _context;

    public InvoiceLinesController(AppDbContext context)
    {
        _context = context;
    }

    [HttpPost]
    public IActionResult Create(InvoiceLine invoiceLine)
    {
        _context.InvoiceLines.Add(invoiceLine);
        _context.SaveChanges();
        return Ok(invoiceLine);
    }

    [HttpGet]
    public IActionResult GetAll()
    {
        return Ok(_context.InvoiceLines.ToList());
    }

    [HttpGet("{id}")]
    public IActionResult GetById(int id)
    {
        var invoiceLine = _context.InvoiceLines.Find(id);
        if (invoiceLine == null)
            return NotFound();
        return Ok(invoiceLine);
    }

    [HttpPut("{id}")]
    public IActionResult Update(int id, InvoiceLine invoiceLine)
    {
        var existing = _context.InvoiceLines.Find(id);
        if (existing == null)
            return NotFound();

        existing.InvoiceStatus = invoiceLine.InvoiceStatus;
        existing.ProductName = invoiceLine.ProductName;
        existing.ProductPrice = invoiceLine.ProductPrice;
        existing.ProductQtySold = invoiceLine.ProductQtySold;
        existing.Cashier = invoiceLine.Cashier;
        existing.CustomerName = invoiceLine.CustomerName;
        existing.CustomerPhone = invoiceLine.CustomerPhone;
        existing.TotalAmount = invoiceLine.TotalAmount;
        existing.AmountTendered = invoiceLine.AmountTendered;
        existing.ChangeBalance = invoiceLine.ChangeBalance;
        existing.TaxAmount = invoiceLine.TaxAmount;
        existing.CurrentStock = invoiceLine.CurrentStock;
        existing.UpdatedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

        _context.SaveChanges();
        return Ok(existing);
    }

    [HttpDelete("{id}")]
    public IActionResult Delete(int id)
    {
        var invoiceLine = _context.InvoiceLines.Find(id);
        if (invoiceLine == null)
            return NotFound();

        _context.InvoiceLines.Remove(invoiceLine);
        _context.SaveChanges();
        return NoContent();
    }
}
