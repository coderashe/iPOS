# iPOS Analytics Dashboard - API Reference

## 📖 Overview

This document provides detailed information about all API endpoints available in the iPOS Analytics Dashboard backend.

**Base URL**: `http://localhost:5000/api`

**Swagger UI**: `http://localhost:5000/swagger`

## 🔐 Authentication

Currently, the API does not require authentication. This will be added in a future update.

**Future**: All endpoints will require JWT Bearer token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 📊 Dashboard Endpoints

### Get Dashboard Summary

Retrieves summary statistics for the dashboard overview.

**Endpoint**: `GET /api/dashboard/summary`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date for the report period |
| endDate | string (ISO 8601) | Yes | End date for the report period |

**Example Request**:
```http
GET /api/dashboard/summary?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z
```

**Response** (200 OK):
```json
{
  "totalSales": 1250,
  "totalRevenue": 45678.50,
  "transactionCount": 342,
  "averageTransactionValue": 133.56,
  "topProducts": [
    {
      "productId": 101,
      "productName": "Nike Air Max",
      "quantitySold": 45,
      "revenue": 6750.00
    }
  ],
  "lowStockCount": 12,
  "activeCustomersCount": 156,
  "lastSyncTime": "2026-03-10T14:30:00Z"
}
```

**Caching**: 5 minutes

## 📈 Reports Endpoints

### Get Sales Report

Retrieves sales data grouped by time period.

**Endpoint**: `GET /api/reports/sales`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |
| groupBy | string | No | Grouping: "day", "week", or "month" (default: "day") |

**Example Request**:
```http
GET /api/reports/sales?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z&groupBy=day
```

**Response** (200 OK):
```json
{
  "dataPoints": [
    {
      "date": "2026-03-01T00:00:00Z",
      "revenue": 4567.80,
      "transactionCount": 34,
      "itemsSold": 125
    },
    {
      "date": "2026-03-02T00:00:00Z",
      "revenue": 5234.20,
      "transactionCount": 42,
      "itemsSold": 156
    }
  ],
  "totalRevenue": 45678.50,
  "totalTransactions": 342
}
```

**Caching**: 15 minutes

---

### Get Sales by Cashier

Retrieves sales performance grouped by cashier.

**Endpoint**: `GET /api/reports/sales/by-cashier`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |

**Example Request**:
```http
GET /api/reports/sales/by-cashier?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z
```

**Response** (200 OK):
```json
[
  {
    "cashierName": "John Doe",
    "totalSales": 15678.50,
    "transactionCount": 123,
    "averageTransactionValue": 127.47
  },
  {
    "cashierName": "Jane Smith",
    "totalSales": 18234.20,
    "transactionCount": 145,
    "averageTransactionValue": 125.75
  }
]
```

**Caching**: 15 minutes

---

### Get Sales by Hour

Retrieves hourly sales breakdown for a specific date.

**Endpoint**: `GET /api/reports/sales/by-hour`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| date | string (ISO 8601) | Yes | Date to analyze |

**Example Request**:
```http
GET /api/reports/sales/by-hour?date=2026-03-10T00:00:00Z
```

**Response** (200 OK):
```json
[
  {
    "date": "2026-03-10T09:00:00Z",
    "revenue": 1234.50,
    "transactionCount": 12,
    "itemsSold": 45
  },
  {
    "date": "2026-03-10T10:00:00Z",
    "revenue": 2345.60,
    "transactionCount": 18,
    "itemsSold": 67
  }
]
```

**Caching**: 15 minutes

---

### Get Stock Levels

Retrieves current stock levels for all products.

**Endpoint**: `GET /api/reports/inventory/stock-levels`

**Parameters**: None

**Example Request**:
```http
GET /api/reports/inventory/stock-levels
```

**Response** (200 OK):
```json
[
  {
    "productId": 101,
    "productName": "Nike Air Max",
    "currentStock": 45.0,
    "averageDailySales": 5.2,
    "daysRemaining": 8
  },
  {
    "productId": 102,
    "productName": "Adidas Ultraboost",
    "currentStock": 12.0,
    "averageDailySales": 3.5,
    "daysRemaining": 3
  }
]
```

**Caching**: 15 minutes

---

### Get Low Stock Items

Retrieves products below the specified stock threshold.

**Endpoint**: `GET /api/reports/inventory/low-stock`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| threshold | number | No | Days of stock remaining threshold (default: 7) |

**Example Request**:
```http
GET /api/reports/inventory/low-stock?threshold=7
```

**Response** (200 OK):
```json
[
  {
    "productId": 102,
    "productName": "Adidas Ultraboost",
    "currentStock": 12.0,
    "averageDailySales": 3.5,
    "daysRemaining": 3
  }
]
```

**Caching**: 15 minutes

---

### Get Revenue Report

Retrieves revenue data grouped by time period.

**Endpoint**: `GET /api/reports/revenue`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |
| groupBy | string | No | Grouping: "day", "week", or "month" (default: "day") |

**Example Request**:
```http
GET /api/reports/revenue?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z&groupBy=day
```

**Response** (200 OK):
```json
[
  {
    "date": "2026-03-01T00:00:00Z",
    "revenue": 4567.80,
    "transactionCount": 34,
    "itemsSold": 125
  }
]
```

**Caching**: 15 minutes

## 🔍 Analytics Endpoints

### Get Top Products

Retrieves best-selling products by quantity or revenue.

**Endpoint**: `GET /api/analytics/top-products`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |
| limit | number | No | Number of products to return (default: 10) |

**Example Request**:
```http
GET /api/analytics/top-products?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z&limit=5
```

**Response** (200 OK):
```json
[
  {
    "productId": 101,
    "productName": "Nike Air Max",
    "quantitySold": 45,
    "revenue": 6750.00
  },
  {
    "productId": 103,
    "productName": "Puma Suede",
    "quantitySold": 38,
    "revenue": 5320.00
  }
]
```

**Caching**: 15 minutes

---

### Get Customer Insights

Retrieves customer analytics and top customers.

**Endpoint**: `GET /api/analytics/customer-insights`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |

**Example Request**:
```http
GET /api/analytics/customer-insights?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z
```

**Response** (200 OK):
```json
{
  "totalCustomers": 156,
  "newCustomers": 23,
  "topCustomers": [
    {
      "customerId": 501,
      "customerName": "ABC Store",
      "customerPhone": "+1234567890",
      "totalSpent": 12345.67,
      "purchaseCount": 45
    }
  ],
  "averageCustomerValue": 292.81,
  "totalReceivables": 5678.90
}
```

**Caching**: 15 minutes

---

### Get Revenue Trends

Retrieves revenue trends with growth rate analysis.

**Endpoint**: `GET /api/analytics/revenue-trends`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |
| groupBy | string | No | Grouping: "day", "week", or "month" (default: "day") |

**Example Request**:
```http
GET /api/analytics/revenue-trends?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z&groupBy=day
```

**Response** (200 OK):
```json
{
  "dataPoints": [
    {
      "date": "2026-03-01T00:00:00Z",
      "value": 4567.80
    },
    {
      "date": "2026-03-02T00:00:00Z",
      "value": 5234.20
    }
  ],
  "growthRate": 14.6,
  "trend": "increasing"
}
```

**Trend Values**: "increasing", "decreasing", "stable"

**Caching**: 15 minutes

---

### Get Product Performance

Retrieves performance metrics for a specific product.

**Endpoint**: `GET /api/analytics/product-performance`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| productId | number | Yes | Product ID |
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |

**Example Request**:
```http
GET /api/analytics/product-performance?productId=101&startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z
```

**Response** (200 OK):
```json
{
  "productId": 101,
  "productName": "Nike Air Max",
  "totalQuantitySold": 45,
  "totalRevenue": 6750.00,
  "averagePrice": 150.00,
  "salesTrend": "increasing"
}
```

**Caching**: 15 minutes

---

### Get Peak Hours

Retrieves peak transaction hours analysis.

**Endpoint**: `GET /api/analytics/peak-hours`

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | string (ISO 8601) | Yes | Start date |
| endDate | string (ISO 8601) | Yes | End date |

**Example Request**:
```http
GET /api/analytics/peak-hours?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z
```

**Response** (200 OK):
```json
[
  {
    "hour": 10,
    "transactionCount": 145,
    "revenue": 18234.50,
    "isPeakHour": true
  },
  {
    "hour": 14,
    "transactionCount": 167,
    "revenue": 21456.80,
    "isPeakHour": true
  }
]
```

**Caching**: 15 minutes

## 🔄 Response Codes

| Code | Description |
|------|-------------|
| 200 | Success - Request completed successfully |
| 400 | Bad Request - Invalid parameters |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Server error occurred |

## 📝 Common Response Patterns

### Success Response
```json
{
  "data": { ... },
  "success": true
}
```

### Error Response
```json
{
  "error": "Error message",
  "success": false
}
```

## 🎯 Usage Examples

### Using cURL

**Get Dashboard Summary:**
```bash
curl -X GET "http://localhost:5000/api/dashboard/summary?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z"
```

**Get Sales Report:**
```bash
curl -X GET "http://localhost:5000/api/reports/sales?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z&groupBy=day"
```

### Using JavaScript/TypeScript

**With Fetch API:**
```typescript
const response = await fetch(
  'http://localhost:5000/api/dashboard/summary?startDate=2026-03-01T00:00:00Z&endDate=2026-03-10T23:59:59Z'
);
const data = await response.json();
console.log(data);
```

**With Angular HttpClient:**
```typescript
this.http.get<DashboardSummary>(
  'http://localhost:5000/api/dashboard/summary',
  { 
    params: { 
      startDate: '2026-03-01T00:00:00Z', 
      endDate: '2026-03-10T23:59:59Z' 
    } 
  }
).subscribe(data => console.log(data));
```

## ⚡ Performance Considerations

### Caching Strategy
- **Dashboard Summary**: 5 minutes
- **Reports**: 15 minutes
- **Analytics**: 15 minutes

Cache keys are based on endpoint + parameters, so different date ranges are cached separately.

### Query Optimization
- All queries use `AsNoTracking()` for read-only operations
- Database indexes on frequently queried columns
- Efficient LINQ queries with proper filtering
- Aggregation at database level

### Best Practices
1. Use appropriate date ranges (avoid querying years of data)
2. Leverage caching by reusing the same parameters
3. Use groupBy parameter to reduce data points
4. Implement pagination for large result sets (future enhancement)

## 🔧 Configuration

### CORS Settings
The API allows requests from:
- `http://localhost:4200` (Angular dev server)

To add more origins, update `Program.cs`:
```csharp
builder.Services.AddCors(options =>
{
    options.AddPolicy("DashboardPolicy", policy =>
    {
        policy.WithOrigins("http://localhost:4200", "https://your-domain.com")
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});
```

### Cache Configuration
To adjust cache duration, modify the CacheService:
```csharp
// In controller
var cacheKey = $"dashboard_summary_{startDate}_{endDate}";
return await _cacheService.GetOrCreateAsync(
    cacheKey,
    () => GetSummaryData(startDate, endDate),
    TimeSpan.FromMinutes(10) // Change duration here
);
```

## 📊 Data Models

### DashboardSummaryDto
```csharp
public class DashboardSummaryDto
{
    public decimal TotalSales { get; set; }
    public decimal TotalRevenue { get; set; }
    public int TransactionCount { get; set; }
    public decimal AverageTransactionValue { get; set; }
    public List<TopProductDto> TopProducts { get; set; }
    public int LowStockCount { get; set; }
    public int ActiveCustomersCount { get; set; }
    public DateTime LastSyncTime { get; set; }
}
```

### SalesReportDto
```csharp
public class SalesReportDto
{
    public List<SalesDataPointDto> DataPoints { get; set; }
    public decimal TotalRevenue { get; set; }
    public int TotalTransactions { get; set; }
}
```

### CustomerInsightsDto
```csharp
public class CustomerInsightsDto
{
    public int TotalCustomers { get; set; }
    public int NewCustomers { get; set; }
    public List<TopCustomerDto> TopCustomers { get; set; }
    public decimal AverageCustomerValue { get; set; }
    public decimal TotalReceivables { get; set; }
}
```

## 🐛 Error Handling

### Common Errors

**Invalid Date Format:**
```json
{
  "error": "Invalid date format. Use ISO 8601 format (YYYY-MM-DDTHH:mm:ssZ)",
  "statusCode": 400
}
```

**Database Connection Error:**
```json
{
  "error": "Unable to connect to database",
  "statusCode": 500
}
```

**No Data Found:**
```json
{
  "dataPoints": [],
  "totalRevenue": 0,
  "totalTransactions": 0
}
```

## 🧪 Testing with Swagger

1. Navigate to http://localhost:5000/swagger
2. Expand any endpoint
3. Click "Try it out"
4. Fill in parameters
5. Click "Execute"
6. View response

## 📚 Additional Resources

- **Swagger UI**: http://localhost:5000/swagger
- **API Health Check**: http://localhost:5000/health (if implemented)
- **Design Document**: `.kiro/specs/ipos-analytics-dashboard/design.md`
- **Requirements**: `.kiro/specs/ipos-analytics-dashboard/requirements.md`

---

**Last Updated**: March 10, 2026
**API Version**: 1.0.0
