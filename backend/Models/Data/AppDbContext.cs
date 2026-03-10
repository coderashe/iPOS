using Microsoft.EntityFrameworkCore;
using iPosApi.Models;

namespace iPosApi.Models.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options)
        : base(options) { }

    public DbSet<Product> Products => Set<Product>();
    public DbSet<Order> Orders => Set<Order>();
    public DbSet<OrderItem> OrderItems => Set<OrderItem>();
    public DbSet<InvoiceLine> InvoiceLines => Set<InvoiceLine>();
}
