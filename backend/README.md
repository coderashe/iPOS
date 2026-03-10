# iPOS Analytics Dashboard - Backend API

ASP.NET Core Web API for the iPOS Analytics Dashboard.

## Structure

```
backend/
├── Controllers/          # API Controllers
│   ├── DashboardController.cs
│   ├── ReportsController.cs
│   └── AnalyticsController.cs
├── Dtos/                # Data Transfer Objects
│   ├── DashboardDtos.cs
│   ├── ReportsDtos.cs
│   └── AnalyticsDtos.cs
├── Models/              # Database Models
│   ├── Data/
│   │   └── AppDbContext.cs
│   ├── InvoiceLine.cs
│   ├── Order.cs
│   ├── OrderItem.cs
│   └── Product.cs
├── Services/            # Business Logic Services
│   ├── CacheService.cs
│   └── DataSeeder.cs
├── Migrations/          # Database Migrations
│   └── AddAnalyticsIndexes.sql
├── Program.cs           # Application Entry Point
├── appsettings.json     # Configuration
└── OrderManagementApi.csproj

```

## API Endpoints

### Dashboard
- `GET /api/dashboard/summary` - Get dashboard summary with date range filtering

### Reports
- `GET /api/reports/sales` - Get sales report (groupBy: day/week/month)
- `GET /api/reports/sales/by-cashier` - Get sales by cashier
- `GET /api/reports/sales/by-hour` - Get sales by hour
- `GET /api/reports/inventory/stock-levels` - Get stock levels
- `GET /api/reports/inventory/low-stock` - Get low stock items
- `GET /api/reports/revenue` - Get revenue report

### Analytics
- `GET /api/analytics/top-products` - Get top products
- `GET /api/analytics/customer-insights` - Get customer insights
- `GET /api/analytics/revenue-trends` - Get revenue trends
- `GET /api/analytics/product-performance/{productId}` - Get product performance
- `GET /api/analytics/peak-hours` - Get peak hours analysis

## Running the API

```bash
cd backend
dotnet restore
dotnet run
```

API will be available at: `http://localhost:5000`
Swagger UI: `http://localhost:5000/swagger`

## Database

Connection string in `appsettings.json` points to `iPosDb` on SQL Server LocalDB.

Run migrations:
```bash
dotnet ef database update
```

Apply analytics indexes:
```bash
sqlcmd -S (localdb)\mssqllocaldb -d iPosDb -i Migrations/AddAnalyticsIndexes.sql
```
