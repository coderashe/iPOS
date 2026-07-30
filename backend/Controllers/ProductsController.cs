using Microsoft.AspNetCore.Mvc;
using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Controllers;

[ApiController]
[Route("api/products")]
public class ProductsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly ILogger<ProductsController> _logger;

    public ProductsController(AppDbContext context, ILogger<ProductsController> logger)
    {
        _context = context;
        _logger = logger;
    }

    [HttpPost]
    public IActionResult Create([FromBody] ProductDto productDto)
    {
        if (productDto.SellingPrice <= 0)
        {
            _logger.LogWarning("Attempt to create product with invalid price: {Price} for product {ProductCode}", 
                productDto.SellingPrice, productDto.ProductCode);
            return BadRequest("Selling price must be greater than zero");
        }

        // Check if product already exists
        var existingProduct = _context.Products.Find(productDto.ProductId);
        
        if (existingProduct != null)
        {
            // Update existing product
            existingProduct.Name = productDto.ProductDescription;
            existingProduct.ProductCode = productDto.ProductCode;
            existingProduct.Price = (decimal)productDto.SellingPrice;
            existingProduct.CurrentStock = (double)productDto.ProductQty;
            existingProduct.BrandId = productDto.BrandId;
            existingProduct.ShoeSizeId = productDto.ShoeSizeId;
            existingProduct.Saleable = productDto.Saleable;
            existingProduct.Purchasable = productDto.Purchasable;
            existingProduct.AverageCost = productDto.AverageCost;
            existingProduct.StandardCost = productDto.StandardCost;
            existingProduct.SalesTaxCode = productDto.SalesTaxCode;
            existingProduct.InclusiveTaxed = productDto.InclusiveTaxed;
            existingProduct.ImageUri = productDto.ImageUri;
            existingProduct.UnitCost = productDto.UnitCost;
            existingProduct.MarkupPercentage = productDto.MarkupPercentage;
            existingProduct.LastUpdated = productDto.LastUpdated ?? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            existingProduct.DeviceId = productDto.DeviceId;
            existingProduct.DeviceName = productDto.DeviceName;
            existingProduct.StoreId = productDto.StoreId;
            
            _context.SaveChanges();
            _logger.LogInformation("Updated product {ProductId} - {ProductCode}", existingProduct.Id, existingProduct.ProductCode);
            return Ok(existingProduct);
        }
        else
        {
            // Create new product
            var product = new Product
            {
                Id = productDto.ProductId,
                Name = productDto.ProductDescription,
                ProductCode = productDto.ProductCode,
                Price = (decimal)productDto.SellingPrice,
                CurrentStock = (double)productDto.ProductQty,
                BrandId = productDto.BrandId,
                ShoeSizeId = productDto.ShoeSizeId,
                Saleable = productDto.Saleable,
                Purchasable = productDto.Purchasable,
                AverageCost = productDto.AverageCost,
                StandardCost = productDto.StandardCost,
                SalesTaxCode = productDto.SalesTaxCode,
                InclusiveTaxed = productDto.InclusiveTaxed,
                ImageUri = productDto.ImageUri,
                UnitCost = productDto.UnitCost,
                MarkupPercentage = productDto.MarkupPercentage,
                LastUpdated = productDto.LastUpdated ?? DateTimeOffset.UtcNow.ToUnixTimeMilliseconds(),
                DeviceId = productDto.DeviceId,
                DeviceName = productDto.DeviceName,
                StoreId = productDto.StoreId
            };

            _context.Products.Add(product);
            _context.SaveChanges();
            _logger.LogInformation("Created product {ProductId} - {ProductCode}", product.Id, product.ProductCode);
            return Ok(product);
        }
    }

    [HttpGet]
    public IActionResult GetAll()
    {
        return Ok(_context.Products.ToList());
    }

    [HttpGet("{id}")]
    public IActionResult GetById(long id)
    {
        var product = _context.Products.Find(id);
        if (product == null)
            return NotFound();
        return Ok(product);
    }
}

// DTO for receiving product data from Android
public class ProductDto
{
    public long ProductId { get; set; }
    public string ProductDescription { get; set; } = string.Empty;
    public long ProductQty { get; set; }
    public long BrandId { get; set; }
    public long ShoeSizeId { get; set; }
    public bool Saleable { get; set; }
    public bool Purchasable { get; set; }
    public double AverageCost { get; set; }
    public double StandardCost { get; set; }
    public string? SalesTaxCode { get; set; }
    public string ProductCode { get; set; } = string.Empty;
    public bool InclusiveTaxed { get; set; }
    public string? ImageUri { get; set; }
    public double SellingPrice { get; set; }
    public double UnitCost { get; set; }
    public double MarkupPercentage { get; set; }
    public long? LastUpdated { get; set; }
    public string? DeviceId { get; set; }
    public string? DeviceName { get; set; }
    public string? StoreId { get; set; }
}
