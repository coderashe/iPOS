# iPOS Analytics Dashboard - Project Documentation

**Project Start Date:** March 10, 2026  
**Status:** Planning Phase  
**Version:** 1.0

---

## Project Overview

Building a web-based analytics dashboard for the iPOS (Point of Sale) retail management system. The dashboard will provide visual reporting, charts, and business insights by connecting to an existing ASP.NET Core API and SQL Server database.

---

## Current System Architecture

### Android iPOS Application
- **Frontend:** Jetpack Compose (Android)
- **Local Database:** Room (SQLite) - `ipos_db`
- **Key Features:**
  - Invoice/billing management
  - Product inventory tracking
  - Customer management
  - Payment processing
  - Bluetooth thermal printer support
  - Offline-first with sync capabilities

### Backend API
- **Technology:** ASP.NET Core 9.0 Web API
- **Database:** SQL Server (LocalDB) - `iPosDb`
- **ORM:** Entity Framework Core 9.0
- **Location:** `frontend/OrderManagementApi/`
- **Existing Endpoints:**
  - `/api/invoicelines` - Invoice CRUD operations
  - `/api/products` - Product management
  - `/api/orders` - Order management
  - Swagger UI available for API documentation

### Polling Service
- **Purpose:** Syncs data from Android devices to SQL Server
- **Location:** `frontend/OrderManagement.PollingService/`
- **Technology:** .NET Console Application

---

## Database Schema (SQL Server)

### Key Tables
- **InvoiceLines** - Sales transaction details
- **Products** - Product catalog and inventory
- **Orders** - Customer orders
- **OrderItems** - Order line items
- **Customers** - Customer information
- **Payments** - Payment records
- **DayReports** - Daily summary reports
- **ProductDayReport** - Product-specific daily metrics
- **Expenses** - Business expenses
- **CashFlow** - Cash flow tracking
- **StockTake** - Inventory count records
- **TradeReceivables** - Outstanding customer payments
- **DiscountsReceived** - Discount tracking
- **Currency** - Multi-currency support

---

## Proposed Solution Architecture

### New Angular Dashboard
- **Location:** `frontend/ipos-dashboard/` (to be created)
- **Technology Stack:**
  - Angular 18+ (standalone components)
  - TypeScript (strict mode)
  - Chart.js or ng2-charts for visualizations
  - Material Design or PrimeNG for UI components
  - RxJS for reactive programming

### Data Flow
```
Android iPOS App → Room DB (SQLite)
         ↓
Polling Service → SQL Server (iPosDb)
         ↓
ASP.NET Core API (Enhanced with Analytics Endpoints)
         ↓
Angular Dashboard (Charts, Reports, Analytics)
```

---

## Project Phases

### Phase 1: API Enhancement (Backend)
**Goal:** Add analytics and reporting endpoints to existing API

**New Controllers to Create:**
1. **DashboardController** - Summary statistics
2. **ReportsController** - Time-based analytics
3. **AnalyticsController** - Trends and insights

**Endpoints Needed:**
- `GET /api/dashboard/summary` - Overview stats
- `GET /api/reports/sales` - Sales reports with date filters
- `GET /api/reports/inventory` - Stock analytics
- `GET /api/analytics/top-products` - Best sellers
- `GET /api/analytics/customer-insights` - Customer metrics
- `GET /api/analytics/revenue-trends` - Revenue over time

### Phase 2: Angular Dashboard (Frontend)
**Goal:** Create responsive web dashboard with visualizations

**Key Features:**
- Dashboard overview with summary cards
- Interactive charts (line, bar, pie, doughnut)
- Date range filtering
- Export to PDF/Excel
- Responsive design (desktop/tablet)

**Main Components:**
- Dashboard home
- Sales analytics
- Inventory reports
- Customer insights
- Financial reports

### Phase 3: Integration & Testing
**Goal:** Connect, test, and deploy

**Tasks:**
- API integration
- End-to-end testing
- Performance optimization
- User acceptance testing
- Deployment strategy

---

## Key Requirements Summary

### Functional
- Real-time visual analytics for sales, inventory, customers
- Multiple chart types (line, bar, pie, area)
- Date range filtering (today, week, month, custom)
- Export reports (PDF, Excel, CSV)
- Role-based access control

### Non-Functional
- Page load < 3 seconds
- Support 10,000+ data points in charts
- Responsive design (1920x1080 desktop, 768x1024 tablet)
- Modern browser support (Chrome, Firefox, Edge, Safari)
- Secure authentication and authorization

---

## Technology Decisions

### Why Angular?
- Strong TypeScript support
- Robust ecosystem for enterprise apps
- Excellent charting library integrations
- Material Design components available
- Good performance with large datasets

### Why Chart.js?
- Lightweight and performant
- Extensive chart types
- Good Angular integration (ng2-charts)
- Active community support
- Responsive by default

### API Enhancement Strategy
- Keep existing CRUD endpoints unchanged
- Add new analytics-specific controllers
- Implement caching for expensive queries
- Use database indexing for performance
- Return aggregated data (not raw records)

---

## Development Environment Setup

### Prerequisites
- Node.js 18+ and npm
- Angular CLI (`npm install -g @angular/cli`)
- .NET 9 SDK
- SQL Server (LocalDB or Express)
- Visual Studio Code or Visual Studio 2022

### API Connection
- **Base URL:** `http://localhost:5000` (development)
- **Database:** `Server=(localdb)\\mssqllocaldb;Database=iPosDb`
- **Authentication:** To be determined (JWT recommended)

---

## Next Steps

1. ✅ Requirements document created (`.kiro/specs/ipos-analytics-dashboard/requirements.md`)
2. ⏳ Review and approve requirements
3. ⏳ Create technical design document
4. ⏳ Design API endpoints specification
5. ⏳ Create UI wireframes/mockups
6. ⏳ Set up Angular project structure
7. ⏳ Implement API analytics endpoints
8. ⏳ Build Angular dashboard components
9. ⏳ Integration and testing
10. ⏳ Deployment

---

## Open Questions

1. **Data Retention:** How long should historical data be kept?
2. **Multi-Store:** Support multiple store locations in Phase 1?
3. **Stock Thresholds:** What defines "low stock"?
4. **Real-Time Updates:** Needed or periodic refresh acceptable?
5. **Authentication:** JWT tokens, session cookies, or other?
6. **Hosting:** Where will the dashboard be deployed?

---

## Project Files

- **Requirements:** `.kiro/specs/ipos-analytics-dashboard/requirements.md`
- **Documentation:** `docs/ipos-dashboard-project.md` (this file)
- **API Source:** `frontend/OrderManagementApi/`
- **Dashboard Source:** `frontend/ipos-dashboard/` (to be created)

---

## Contact & Resources

- **Spec File:** Use Kiro specs for structured development
- **API Docs:** Swagger UI at `http://localhost:5000/swagger`
- **Database:** SQL Server Management Studio or Azure Data Studio

---

**Last Updated:** March 10, 2026
