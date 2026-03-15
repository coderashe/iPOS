using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using iPosApi.Dtos;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CustomersController : ControllerBase
{
    private readonly AppDbContext _db;
    public CustomersController(AppDbContext db) => _db = db;

    [HttpPost("sync")]
    public async Task<IActionResult> Sync([FromBody] List<CustomerDto> items)
    {
        foreach (var dto in items)
        {
            var existing = await _db.Customers.FindAsync(dto.CustomerId);
            if (existing == null)
                _db.Customers.Add(new Customer
                {
                    CustomerId = dto.CustomerId, Name = dto.Name, Email = dto.Email,
                    Phone = dto.Phone, Address = dto.Address, CustomerType = dto.CustomerType,
                    TaxIdentificationNumber = dto.TaxIdentificationNumber, LoyaltyPoints = dto.LoyaltyPoints
                });
            else
            {
                existing.Name = dto.Name; existing.Email = dto.Email; existing.Phone = dto.Phone;
                existing.Address = dto.Address; existing.CustomerType = dto.CustomerType;
                existing.TaxIdentificationNumber = dto.TaxIdentificationNumber;
                existing.LoyaltyPoints = dto.LoyaltyPoints;
            }
        }
        await _db.SaveChangesAsync();
        return Ok(new { synced = items.Count });
    }

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _db.Customers.ToListAsync());
}
