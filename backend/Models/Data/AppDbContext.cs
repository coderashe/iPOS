using Microsoft.EntityFrameworkCore;
using iPosApi.Models;

namespace iPosApi.Models.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options)
        : base(options) { }

    // Existing
    public DbSet<Product> Products => Set<Product>();
    public DbSet<Order> Orders => Set<Order>();
    public DbSet<OrderItem> OrderItems => Set<OrderItem>();
    public DbSet<InvoiceLine> InvoiceLines => Set<InvoiceLine>();
    public DbSet<User> Users => Set<User>();
    public DbSet<Invoice> Invoices => Set<Invoice>();
    public DbSet<BusinessPerformance> BusinessPerformances => Set<BusinessPerformance>();

    // New
    public DbSet<Customer> Customers => Set<Customer>();
    public DbSet<Payment> Payments => Set<Payment>();
    public DbSet<CartItem> CartItems => Set<CartItem>();
    public DbSet<InvoiceItem> InvoiceItems => Set<InvoiceItem>();
    public DbSet<PriceList> PriceLists => Set<PriceList>();
    public DbSet<Expense> Expenses => Set<Expense>();
    public DbSet<DayReport> DayReports => Set<DayReport>();
    public DbSet<ProductDayReport> ProductDayReports => Set<ProductDayReport>();
    public DbSet<Currency> Currencies => Set<Currency>();
    public DbSet<CashFlow> CashFlows => Set<CashFlow>();
    public DbSet<TradeReceivable> TradeReceivables => Set<TradeReceivable>();
    public DbSet<DiscountReceived> DiscountsReceived => Set<DiscountReceived>();
    public DbSet<ShoeBrand> ShoeBrands => Set<ShoeBrand>();
    public DbSet<ShoeSize> ShoeSizes => Set<ShoeSize>();
    public DbSet<StockTake> StockTakes => Set<StockTake>();
    public DbSet<FailedInvoiceItem> FailedInvoiceItems => Set<FailedInvoiceItem>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Currency>().HasKey(c => c.Code);
        modelBuilder.Entity<Customer>().HasKey(c => c.CustomerId);
        modelBuilder.Entity<Payment>().HasKey(p => p.PaymentId);
        modelBuilder.Entity<CartItem>().HasKey(c => c.CartItemId);
        modelBuilder.Entity<InvoiceItem>().HasKey(i => i.ItemId);
        modelBuilder.Entity<PriceList>().HasKey(p => p.PriceListId);
        modelBuilder.Entity<Expense>().HasKey(e => e.ExpenseId);
        modelBuilder.Entity<DayReport>().HasKey(d => d.ReportId);
        modelBuilder.Entity<ProductDayReport>().HasKey(p => p.ReportId);
        modelBuilder.Entity<CashFlow>().HasKey(c => c.CashFlowId);
        modelBuilder.Entity<TradeReceivable>().HasKey(t => t.ReceivableId);
        modelBuilder.Entity<DiscountReceived>().HasKey(d => d.DiscountId);
        modelBuilder.Entity<ShoeBrand>().HasKey(s => s.BrandId);
        modelBuilder.Entity<ShoeSize>().HasKey(s => s.ShoeSizeId);
        modelBuilder.Entity<StockTake>().HasKey(s => s.Id);
        modelBuilder.Entity<FailedInvoiceItem>().HasKey(f => f.Id);
    }
}
