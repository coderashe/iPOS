using iPosApi.Models;
using iPosApi.Models.Data;

namespace iPosApi.Services;

public static class DataSeeder
{
    private static readonly string[] ProductNames = {
        "Laptop", "Mouse", "Keyboard", "Monitor", "Headphones", "Webcam", "Speaker", "Tablet", 
        "Phone", "Charger", "Cable", "Router", "Switch", "Printer", "Scanner", "Camera",
        "Microphone", "Desk", "Chair", "Lamp", "Book", "Pen", "Notebook", "Backpack",
        "Watch", "Glasses", "Bottle", "Cup", "Plate", "Bowl", "Spoon", "Fork", "Knife"
    };

    private static readonly string[] Cashiers = { "John", "Mary", "Peter", "Sarah", "Mike", "Lisa" };
    private static readonly string[] CustomerNames = { "Alice Johnson", "Bob Smith", "Charlie Brown", "Diana Prince", "Eve Adams", "Frank Miller", "Grace Lee", "Henry Ford" };

    public static void SeedData(AppDbContext context)
    {
        // Check if data already exists
        if (context.Products.Any())
        {
            Console.WriteLine("Database already seeded.");
            return;
        }

        var random = new Random();
        var products = new List<Product>();

        // Create 100 products with stock
        for (int i = 1; i <= 100; i++)
        {
            products.Add(new Product
            {
                Id = i,
                Name = $"{ProductNames[random.Next(ProductNames.Length)]} {i}",
                ProductCode = $"PROD{i:D4}",
                Price = Math.Round((decimal)(random.NextDouble() * 999 + 10), 2),
                CurrentStock = random.Next(0, 200),
                Saleable = true,
                Purchasable = true,
                AverageCost = random.NextDouble() * 500,
                StandardCost = random.NextDouble() * 500,
                InclusiveTaxed = false,
                UnitCost = random.NextDouble() * 500,
                MarkupPercentage = random.NextDouble() * 50
            });
        }

        context.Products.AddRange(products);
        context.SaveChanges();

        Console.WriteLine($"Created {products.Count} products");

        // Create invoice lines for the last 90 days
        var invoiceLines = new List<InvoiceLine>();
        long invoiceIdCounter = 1;
        var now = DateTimeOffset.UtcNow;

        for (int day = 90; day >= 0; day--)
        {
            var date = now.AddDays(-day);
            // Create 10-30 invoices per day
            int invoicesPerDay = random.Next(10, 31);

            for (int inv = 0; inv < invoicesPerDay; inv++)
            {
                // Random time during business hours (8 AM - 8 PM)
                var invoiceTime = date.Date.AddHours(random.Next(8, 21)).AddMinutes(random.Next(0, 60));
                var invoiceTimestamp = new DateTimeOffset(invoiceTime).ToUnixTimeMilliseconds();
                
                // Random number of items per invoice (1-5)
                int itemsInInvoice = random.Next(1, 6);
                
                for (int item = 0; item < itemsInInvoice; item++)
                {
                    var product = products[random.Next(products.Count)];
                    var quantity = random.Next(1, 6);
                    var lineTotal = (double)(product.Price * quantity);
                    var customerId = random.Next(1, 101);

                    invoiceLines.Add(new InvoiceLine
                    {
                        InvoiceId = invoiceIdCounter,
                        InvoiceStatus = "completed",
                        ProductID = product.Id,
                        ProductName = product.Name,
                        ProductPrice = (double)product.Price,
                        ProductQtySold = quantity,
                        Cashier = Cashiers[random.Next(Cashiers.Length)],
                        CustomerID = customerId,
                        CustomerName = CustomerNames[random.Next(CustomerNames.Length)],
                        CustomerPhone = $"+1{random.Next(200, 999)}{random.Next(100, 999)}{random.Next(1000, 9999)}",
                        TotalAmount = lineTotal,
                        AmountTendered = lineTotal + random.Next(0, 20),
                        ChangeBalance = random.Next(0, 20),
                        InvoiceDate = invoiceTimestamp,
                        TaxAmount = lineTotal * 0.1,
                        CurrentStock = product.CurrentStock ?? 0,
                        CreatedAt = invoiceTimestamp
                    });
                }

                invoiceIdCounter++;
            }
        }

        context.InvoiceLines.AddRange(invoiceLines);
        context.SaveChanges();

        Console.WriteLine($"Created {invoiceLines.Count} invoice lines across {invoiceIdCounter - 1} invoices");

        // Create some orders with receivables
        var orders = new List<Order>();
        for (int i = 0; i < 50; i++)
        {
            var totalAmount = (decimal)(random.NextDouble() * 1000 + 100);
            var paidAmount = random.Next(0, 2) == 0 ? totalAmount : totalAmount * (decimal)random.NextDouble();
            
            orders.Add(new Order
            {
                CustomerId = random.Next(1, 101),
                CreatedAt = now.AddDays(-random.Next(0, 90)).ToUnixTimeMilliseconds(),
                TotalAmount = Math.Round(totalAmount, 2),
                PaidAmount = Math.Round(paidAmount, 2),
                Status = paidAmount >= totalAmount ? "completed" : (paidAmount > 0 ? "partial" : "pending")
            });
        }

        context.Orders.AddRange(orders);
        context.SaveChanges();

        Console.WriteLine($"Created {orders.Count} orders");
        Console.WriteLine("Database seeding completed successfully!");
    }
}
