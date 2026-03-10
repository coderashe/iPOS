using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/business-performance")]
public class BusinessPerformanceController : ControllerBase
{
    private readonly AppDbContext _context;

    public BusinessPerformanceController(AppDbContext context)
    {
        _context = context;
    }

    [HttpPost]
    public IActionResult Create(BusinessPerformance performance)
    {
        _context.BusinessPerformances.Add(performance);
        _context.SaveChanges();
        return Ok(performance);
    }

    [HttpGet]
    public IActionResult GetAll()
    {
        return Ok(_context.BusinessPerformances.ToList());
    }
}
