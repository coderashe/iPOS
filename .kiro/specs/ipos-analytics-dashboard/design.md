---
feature: iPOS Analytics Dashboard
type: feature
status: design
created: 2026-03-10
---

# iPOS Analytics Dashboard - Technical Design

## 1. System Architecture Overview

### High-Level Architecture
```
┌─────────────────────┐
│  Android iPOS App   │
│   (Jetpack Compose) │
│   Room DB (SQLite)  │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│  Polling Service    │
│   (.NET Console)    │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│   SQL Server DB     │
│     (iPosDb)        │
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│  ASP.NET Core API   │
│   (Enhanced with    │
│ Analytics Endpoints)│
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│  Angular Dashboard  │
│  (Charts & Reports) │
└─────────────────────┘
```

### Technology Stack

**Backend (API Enhancement)**
- ASP.NET Core 9.0 Web API
- Entity Framework Core 9.0
- SQL Server (LocalDB/Express)
- Swagger/OpenAPI for documentation

**Frontend (Angular Dashboard)**
- Angular 18+ (standalone components)
- TypeScript 5.x (strict mode)
- RxJS 7.x for reactive programming
- Chart.js 4.x with ng2-charts
- Angular Material 18 or PrimeNG 17
- SCSS for styling

**Development Tools**
- Node.js 18+
- Angular CLI
- Visual Studio Code / Visual Studio 2022

## 2. Backend API Design

### 2.1 New Controllers

#### DashboardController
**Purpose:** Provide summary statistics for dashboard overview

**Endpoints:**
```csharp
GET /api/dashboard/summary?startDate={date}&endDate={date}
```

**Response Model:**
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

public class TopProductDto
{
    public long ProductId { get; set; }
    public string ProductName { get; set; }
    public int QuantitySold { get; set; }
    public decimal Revenue { get; set; }
}
```

#### ReportsController
**Purpose:** Generate time-based analytics reports

**Endpoints:**

```csharp
GET /api/reports/sales?startDate={date}&endDate={date}&groupBy={day|week|month}
GET /api/reports/sales/by-cashier?startDate={date}&endDate={date}
GET /api/reports/sales/by-hour?date={date}
GET /api/reports/inventory/stock-levels
GET /api/reports/inventory/low-stock?threshold={number}
GET /api/reports/revenue?startDate={date}&endDate={date}&groupBy={day|week|month}
```

**Response Models:**
```csharp
public class SalesReportDto
{
    public List<SalesDataPointDto> DataPoints { get; set; }
    public decimal TotalRevenue { get; set; }
    public int TotalTransactions { get; set; }
}

public class SalesDataPointDto
{
    public DateTime Date { get; set; }
    public decimal Revenue { get; set; }
    public int TransactionCount { get; set; }
    public int ItemsSold { get; set; }
}

public class SalesByCashierDto
{
    public string CashierName { get; set; }
    public decimal TotalSales { get; set; }
    public int TransactionCount { get; set; }
    public decimal AverageTransactionValue { get; set; }
}

public class StockLevelDto
{
    public long ProductId { get; set; }
    public string ProductName { get; set; }
    public double CurrentStock { get; set; }
    public bool IsLowStock { get; set; }
    public int DaysOfStockRemaining { get; set; }
}
```

#### AnalyticsController
**Purpose:** Provide trend analysis and insights

**Endpoints:**
```csharp
GET /api/analytics/top-products?startDate={date}&endDate={date}&limit={number}
GET /api/analytics/customer-insights?startDate={date}&endDate={date}
GET /api/analytics/revenue-trends?startDate={date}&endDate={date}
GET /api/analytics/product-performance?productId={id}&startDate={date}&endDate={date}
GET /api/analytics/peak-hours?startDate={date}&endDate={date}
```

**Response Models:**
```csharp
public class CustomerInsightsDto
{
    public int TotalCustomers { get; set; }
    public int NewCustomers { get; set; }
    public List<TopCustomerDto> TopCustomers { get; set; }
    public decimal AverageCustomerValue { get; set; }
    public decimal TotalReceivables { get; set; }
}

public class TopCustomerDto
{
    public long CustomerId { get; set; }
    public string CustomerName { get; set; }
    public string CustomerPhone { get; set; }
    public decimal TotalSpent { get; set; }
    public int PurchaseCount { get; set; }
}

public class RevenueTrendDto
{
    public List<TrendDataPointDto> DataPoints { get; set; }
    public decimal GrowthRate { get; set; }
    public string Trend { get; set; } // "increasing", "decreasing", "stable"
}

public class TrendDataPointDto
{
    public DateTime Date { get; set; }
    public decimal Value { get; set; }
}
```

### 2.2 Database Query Optimization

**Indexing Strategy:**
```sql
-- InvoiceLines table indexes
CREATE INDEX IX_InvoiceLines_InvoiceDate ON InvoiceLines(InvoiceDate);
CREATE INDEX IX_InvoiceLines_ProductID ON InvoiceLines(ProductID);
CREATE INDEX IX_InvoiceLines_CustomerID ON InvoiceLines(CustomerID);
CREATE INDEX IX_InvoiceLines_Cashier ON InvoiceLines(Cashier);

-- Products table indexes
CREATE INDEX IX_Products_CurrentStock ON Products(CurrentStock);

-- Composite indexes for common queries
CREATE INDEX IX_InvoiceLines_Date_Status ON InvoiceLines(InvoiceDate, InvoiceStatus);
```

**Query Patterns:**

- Use LINQ with proper filtering and projection
- Implement pagination for large result sets
- Use `AsNoTracking()` for read-only queries
- Aggregate data at database level using GroupBy

**Example Query:**
```csharp
var salesData = await _context.InvoiceLines
    .AsNoTracking()
    .Where(il => il.InvoiceDate >= startDate && il.InvoiceDate <= endDate)
    .GroupBy(il => il.InvoiceDate.Date)
    .Select(g => new SalesDataPointDto
    {
        Date = g.Key,
        Revenue = g.Sum(il => il.TotalAmount),
        TransactionCount = g.Count(),
        ItemsSold = g.Sum(il => il.ProductQtySold)
    })
    .OrderBy(s => s.Date)
    .ToListAsync();
```

### 2.3 Caching Strategy

**Implementation:**
- Use `IMemoryCache` for frequently accessed data
- Cache duration: 5 minutes for dashboard summary, 15 minutes for reports
- Cache key pattern: `{endpoint}_{startDate}_{endDate}_{params}`


**Example Caching Service:**
```csharp
public class CacheService
{
    private readonly IMemoryCache _cache;
    
    public async Task<T> GetOrCreateAsync<T>(
        string key, 
        Func<Task<T>> factory, 
        TimeSpan? expiration = null)
    {
        if (!_cache.TryGetValue(key, out T result))
        {
            result = await factory();
            var options = new MemoryCacheEntryOptions()
                .SetAbsoluteExpiration(expiration ?? TimeSpan.FromMinutes(5));
            _cache.Set(key, result, options);
        }
        return result;
    }
}
```

### 2.4 CORS Configuration

```csharp
builder.Services.AddCors(options =>
{
    options.AddPolicy("DashboardPolicy", policy =>
    {
        policy.WithOrigins("http://localhost:4200")
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});
```

## 3. Angular Frontend Architecture

### 3.1 Project Structure
```
frontend/ipos-dashboard/
├── src/
│   ├── app/
│   │   ├── core/
│   │   │   ├── services/
│   │   │   │   ├── api.service.ts
│   │   │   │   ├── auth.service.ts
│   │   │   │   └── cache.service.ts
│   │   │   ├── interceptors/
│   │   │   │   ├── auth.interceptor.ts
│   │   │   │   └── error.interceptor.ts
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts
│   │   │   └── models/
│   │   │       └── api-models.ts
│   │   ├── features/
│   │   │   ├── dashboard/
│   │   │   │   ├── dashboard.component.ts
│   │   │   │   ├── dashboard.component.html
│   │   │   │   └── dashboard.component.scss
│   │   │   ├── sales/
│   │   │   │   ├── sales-analytics.component.ts
│   │   │   │   └── components/
│   │   │   │       ├── sales-chart.component.ts
│   │   │   │       └── cashier-performance.component.ts
│   │   │   ├── inventory/
│   │   │   │   ├── inventory-reports.component.ts
│   │   │   │   └── components/
│   │   │   │       ├── stock-levels.component.ts
│   │   │   │       └── low-stock-alert.component.ts
│   │   │   ├── customers/
│   │   │   │   └── customer-insights.component.ts
│   │   │   └── financial/
│   │   │       └── financial-reports.component.ts
│   │   ├── shared/
│   │   │   ├── components/
│   │   │   │   ├── date-range-picker/
│   │   │   │   ├── summary-card/
│   │   │   │   ├── chart-wrapper/
│   │   │   │   └── export-button/
│   │   │   └── pipes/
│   │   │       ├── currency-format.pipe.ts
│   │   │       └── date-format.pipe.ts
│   │   ├── app.component.ts
│   │   ├── app.routes.ts
│   │   └── app.config.ts
│   ├── assets/
│   ├── environments/
│   │   ├── environment.ts
│   │   └── environment.prod.ts
│   └── styles.scss
└── angular.json
```


### 3.2 Routing Configuration

```typescript
export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  { 
    path: 'sales', 
    loadComponent: () => import('./features/sales/sales-analytics.component')
      .then(m => m.SalesAnalyticsComponent),
    canActivate: [AuthGuard]
  },
  { 
    path: 'inventory', 
    loadComponent: () => import('./features/inventory/inventory-reports.component')
      .then(m => m.InventoryReportsComponent),
    canActivate: [AuthGuard]
  },
  { 
    path: 'customers', 
    loadComponent: () => import('./features/customers/customer-insights.component')
      .then(m => m.CustomerInsightsComponent),
    canActivate: [AuthGuard]
  },
  { 
    path: 'financial', 
    loadComponent: () => import('./features/financial/financial-reports.component')
      .then(m => m.FinancialReportsComponent),
    canActivate: [AuthGuard]
  },
  { path: '**', redirectTo: '/dashboard' }
];
```

### 3.3 Core Services

#### API Service
```typescript
@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDashboardSummary(startDate: string, endDate: string): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(
      `${this.baseUrl}/dashboard/summary`,
      { params: { startDate, endDate } }
    );
  }

  getSalesReport(params: SalesReportParams): Observable<SalesReport> {
    return this.http.get<SalesReport>(
      `${this.baseUrl}/reports/sales`,
      { params: params as any }
    );
  }
}
```


#### State Management
Using RxJS BehaviorSubject for simple state management:

```typescript
@Injectable({ providedIn: 'root' })
export class DashboardStateService {
  private dateRangeSubject = new BehaviorSubject<DateRange>({
    startDate: new Date(),
    endDate: new Date()
  });
  
  dateRange$ = this.dateRangeSubject.asObservable();
  
  updateDateRange(range: DateRange): void {
    this.dateRangeSubject.next(range);
  }
}
```

### 3.4 Component Design

#### Dashboard Component (Main)
```typescript
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SummaryCardComponent, ChartWrapperComponent],
  template: `
    <div class="dashboard-container">
      <app-date-range-picker 
        (rangeChange)="onDateRangeChange($event)">
      </app-date-range-picker>
      
      <div class="summary-cards">
        <app-summary-card 
          *ngFor="let card of summaryCards"
          [title]="card.title"
          [value]="card.value"
          [icon]="card.icon"
          [trend]="card.trend">
        </app-summary-card>
      </div>
      
      <div class="charts-grid">
        <app-chart-wrapper 
          title="Sales Trend"
          [chartData]="salesChartData"
          chartType="line">
        </app-chart-wrapper>
        
        <app-chart-wrapper 
          title="Top Products"
          [chartData]="topProductsChartData"
          chartType="bar">
        </app-chart-wrapper>
      </div>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  summaryCards: SummaryCard[] = [];
  salesChartData: ChartData;
  
  constructor(
    private apiService: ApiService,
    private stateService: DashboardStateService
  ) {}
  
  ngOnInit(): void {
    this.stateService.dateRange$
      .pipe(
        switchMap(range => this.apiService.getDashboardSummary(
          range.startDate.toISOString(),
          range.endDate.toISOString()
        ))
      )
      .subscribe(data => this.updateDashboard(data));
  }
}
```


### 3.5 Chart Configuration

#### Chart.js Setup with ng2-charts
```typescript
@Component({
  selector: 'app-sales-chart',
  standalone: true,
  imports: [BaseChartDirective],
  template: `
    <canvas baseChart
      [data]="chartData"
      [options]="chartOptions"
      [type]="'line'">
    </canvas>
  `
})
export class SalesChartComponent {
  chartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{
      label: 'Sales',
      data: [],
      borderColor: '#3f51b5',
      backgroundColor: 'rgba(63, 81, 181, 0.1)',
      tension: 0.4
    }]
  };
  
  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true },
      tooltip: { enabled: true }
    },
    scales: {
      y: { beginAtZero: true }
    }
  };
}
```

#### Chart Types Configuration
- **Line Chart:** Sales trends, revenue over time
- **Bar Chart:** Top products, cashier performance
- **Pie/Doughnut Chart:** Sales by category, payment methods
- **Area Chart:** Revenue comparison

## 4. Data Flow & Communication

### 4.1 Request Flow
```
User Action → Component → Service → HTTP Interceptor → API
                ↓                                        ↓
            Update UI ← Transform Data ← Response ← Cache Check
```

### 4.2 Error Handling

**HTTP Interceptor:**
```typescript
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An error occurred';
        
        if (error.error instanceof ErrorEvent) {
          errorMessage = `Error: ${error.error.message}`;
        } else {
          errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
        
        // Show user-friendly error
        this.notificationService.showError(errorMessage);
        return throwError(() => error);
      })
    );
  }
}
```


## 5. Authentication & Authorization

### 5.1 JWT Authentication Flow
```
1. User logs in → API validates credentials
2. API returns JWT token
3. Angular stores token in localStorage
4. All subsequent requests include token in Authorization header
5. API validates token on each request
```

### 5.2 Auth Guard Implementation
```typescript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  canActivate(): boolean {
    if (this.authService.isAuthenticated()) {
      return true;
    }
    
    this.router.navigate(['/login']);
    return false;
  }
}
```

### 5.3 Role-Based Access
```typescript
export enum UserRole {
  Admin = 'admin',
  Manager = 'manager',
  Viewer = 'viewer'
}

// Feature access matrix
const featureAccess = {
  [UserRole.Admin]: ['dashboard', 'sales', 'inventory', 'customers', 'financial', 'export'],
  [UserRole.Manager]: ['dashboard', 'sales', 'inventory', 'customers', 'export'],
  [UserRole.Viewer]: ['dashboard', 'sales', 'inventory']
};
```

## 6. Performance Optimization

### 6.1 Frontend Optimizations
- Lazy loading for feature modules
- Virtual scrolling for large lists
- OnPush change detection strategy
- Debounce user inputs (date range changes)
- Image optimization and lazy loading
- Bundle size optimization with tree-shaking

### 6.2 Backend Optimizations
- Database query optimization with indexes
- Response caching (5-15 minutes)
- Pagination for large datasets
- Async/await for non-blocking operations
- Connection pooling for database

### 6.3 Chart Performance
- Limit data points to 1000 per chart
- Use data decimation for large datasets
- Implement chart data caching
- Lazy render charts (only when visible)

## 7. Export Functionality

### 7.1 PDF Export
```typescript
exportToPDF(): void {
  const element = document.getElementById('report-content');
  html2canvas(element).then(canvas => {
    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF();
    pdf.addImage(imgData, 'PNG', 10, 10);
    pdf.save('report.pdf');
  });
}
```

### 7.2 Excel Export
```typescript
exportToExcel(data: any[], filename: string): void {
  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Report');
  XLSX.writeFile(wb, `${filename}.xlsx`);
}
```


## 8. Responsive Design

### 8.1 Breakpoints
```scss
$breakpoints: (
  'mobile': 320px,
  'tablet': 768px,
  'desktop': 1024px,
  'wide': 1920px
);

// Usage
@media (min-width: 768px) {
  .dashboard-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
}

@media (min-width: 1024px) {
  .dashboard-container {
    grid-template-columns: repeat(3, 1fr);
  }
}
```

### 8.2 Component Responsiveness
- Summary cards: 1 column (mobile), 2 columns (tablet), 4 columns (desktop)
- Charts: Stack vertically (mobile), 2x2 grid (tablet/desktop)
- Navigation: Hamburger menu (mobile), sidebar (desktop)

## 9. Testing Strategy

### 9.1 Backend Testing
```csharp
[Fact]
public async Task GetDashboardSummary_ReturnsCorrectData()
{
    // Arrange
    var controller = new DashboardController(_context, _cache);
    var startDate = DateTime.Today.AddDays(-7);
    var endDate = DateTime.Today;
    
    // Act
    var result = await controller.GetSummary(startDate, endDate);
    
    // Assert
    Assert.NotNull(result);
    Assert.IsType<OkObjectResult>(result);
}
```

### 9.2 Frontend Testing
```typescript
describe('DashboardComponent', () => {
  it('should load summary data on init', () => {
    const fixture = TestBed.createComponent(DashboardComponent);
    const component = fixture.componentInstance;
    
    fixture.detectChanges();
    
    expect(component.summaryCards.length).toBeGreaterThan(0);
  });
});
```

### 9.3 E2E Testing
- Use Playwright or Cypress
- Test critical user flows (login, view dashboard, export report)
- Test responsive behavior

## 10. Deployment Architecture

### 10.1 Development Environment
```
API: http://localhost:5000
Angular: http://localhost:4200
Database: (localdb)\mssqllocaldb
```

### 10.2 Production Deployment Options

**Option 1: IIS Hosting**
- API: Deploy to IIS on Windows Server
- Angular: Build and serve from IIS
- Database: SQL Server Express/Standard

**Option 2: Azure Hosting**
- API: Azure App Service
- Angular: Azure Static Web Apps or App Service
- Database: Azure SQL Database

**Option 3: Docker Containers**
```dockerfile
# API Dockerfile
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY . .
ENTRYPOINT ["dotnet", "OrderManagementApi.dll"]

# Angular Dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
```

### 10.3 Environment Configuration

**Angular environment files:**
```typescript
// environment.ts (development)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:5000/api'
};

// environment.prod.ts (production)
export const environment = {
  production: true,
  apiUrl: 'https://api.ipos.com/api'
};
```

**API appsettings:**
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=.;Database=iPosDb;Trusted_Connection=true"
  },
  "Jwt": {
    "Key": "your-secret-key",
    "Issuer": "iPOS-API",
    "Audience": "iPOS-Dashboard"
  }
}
```

## 11. Security Considerations

### 11.1 API Security
- HTTPS only in production
- JWT token expiration (1 hour)
- Refresh token mechanism
- Rate limiting on endpoints
- Input validation and sanitization
- SQL injection prevention (EF Core parameterized queries)

### 11.2 Frontend Security
- XSS prevention (Angular sanitization)
- CSRF protection
- Secure token storage
- Content Security Policy headers
- No sensitive data in localStorage

## 12. Monitoring & Logging

### 12.1 API Logging
```csharp
builder.Services.AddLogging(logging =>
{
    logging.AddConsole();
    logging.AddDebug();
    logging.AddEventLog();
});
```

### 12.2 Error Tracking
- Application Insights (Azure)
- Sentry for error monitoring
- Custom logging service

## 13. Future Enhancements

- Real-time updates using SignalR
- Advanced forecasting with ML.NET
- Mobile app version
- Multi-language support
- Custom report builder
- Scheduled email reports
- Data export automation

---

## Design Approval Checklist

- [ ] API endpoints reviewed and approved
- [ ] Database indexing strategy validated
- [ ] Angular architecture approved
- [ ] Chart configurations finalized
- [ ] Authentication mechanism confirmed
- [ ] Performance targets agreed upon
- [ ] Deployment strategy selected
- [ ] Security requirements met
