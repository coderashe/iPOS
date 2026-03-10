---
feature: iPOS Analytics Dashboard
type: feature
status: requirements
created: 2026-03-10
---

# iPOS Analytics Dashboard - Requirements

## Overview
Build a web-based analytics dashboard using Angular that connects to the existing ASP.NET Core API to provide visual reporting, charts, and business insights for the iPOS retail management system.

## Goals
- Provide real-time visual analytics for sales, inventory, and customer data
- Enable data-driven decision making for retail operations
- Create a responsive web interface accessible from desktop and tablet devices
- Leverage existing SQL Server database synced from Android POS devices

## Stakeholders
- Store managers (primary users)
- Business owners
- Sales staff (view-only access)

## Functional Requirements

### 1. Dashboard Overview Screen
- **Summary Cards**
  - Total sales (today, week, month)
  - Total revenue with currency formatting
  - Number of transactions
  - Average transaction value
  - Top selling products (top 5)
  - Low stock alerts
  - Active customers count

### 2. Sales Analytics
- **Sales Reports**
  - Daily sales chart (line/bar chart)
  - Weekly sales trends
  - Monthly sales comparison
  - Year-over-year comparison
  - Sales by cashier/employee
  - Sales by payment method
  - Peak hours analysis (hourly breakdown)

- **Revenue Analytics**
  - Revenue trends over time
  - Profit margins
  - Tax collected summary
  - Discounts given analysis

### 3. Inventory Management Reports
- **Stock Analytics**
  - Current stock levels (all products)
  - Low stock warnings (configurable threshold)
  - Out of stock items
  - Stock movement trends
  - Product turnover rate
  - Stock variance reports (from StockTake table)

- **Product Performance**
  - Best selling products (by quantity and revenue)
  - Slow moving products
  - Product category breakdown
  - Brand performance (shoe brands)
  - Size distribution analysis

### 4. Customer Insights
- **Customer Analytics**
  - Total customers
  - New customers (by period)
  - Customer purchase frequency
  - Top customers by spending
  - Customer retention metrics
  - Trade receivables overview

### 5. Financial Reports
- **Cash Flow**
  - Daily cash flow summary
  - Expenses tracking
  - Revenue vs expenses comparison
  - Payment method breakdown
  - Currency exchange tracking (multi-currency support)

### 6. Date Range Filtering
- Predefined ranges: Today, Yesterday, Last 7 days, Last 30 days, This Month, Last Month, Custom Range
- Date picker for custom range selection
- Apply filters across all reports

### 7. Export Functionality
- Export reports to PDF
- Export data to Excel/CSV
- Print-friendly report views

## Non-Functional Requirements

### Performance
- Dashboard should load within 3 seconds
- Charts should render smoothly with up to 10,000 data points
- API responses should be cached appropriately
- Lazy loading for heavy components

### Security
- Authentication required (integrate with existing user system)
- Role-based access control (admin, manager, viewer)
- Secure API communication (HTTPS)
- Session timeout after inactivity

### Usability
- Responsive design (desktop 1920x1080, tablet 768x1024)
- Intuitive navigation
- Consistent color scheme matching iPOS branding
- Loading indicators for async operations
- Error handling with user-friendly messages

### Compatibility
- Modern browsers (Chrome, Firefox, Edge, Safari - latest 2 versions)
- No IE11 support required

### Scalability
- Support for multiple store locations (future)
- Handle growing data volume (100k+ transactions)

## Technical Constraints
- Must use existing SQL Server database (OrderManagementDb)
- Must connect to existing ASP.NET Core API
- Angular 18+ with standalone components
- TypeScript strict mode
- Material Design or PrimeNG component library

## Data Sources
- **Primary Database**: SQL Server (OrderManagementDb)
- **Tables to Query**:
  - InvoiceLines (sales transactions)
  - Products (inventory)
  - Orders
  - Customers
  - Payments
  - DayReports
  - ProductDayReport
  - Expenses
  - CashFlow
  - StockTake
  - TradeReceivables
  - DiscountsReceived
  - Currency

## Out of Scope (Phase 1)
- Real-time notifications/alerts
- Mobile app version
- Advanced forecasting/ML predictions
- Multi-language support
- Direct database modifications from dashboard
- User management UI (use existing system)

## Success Criteria
- Dashboard displays accurate data from SQL Server
- All charts render correctly with real data
- Page load time < 3 seconds
- Zero critical bugs in production
- Positive feedback from 3 store managers during UAT

## Dependencies
- ASP.NET Core API must be enhanced with analytics endpoints
- SQL Server database must be accessible
- Node.js and Angular CLI installed for development
- .NET 9 SDK for API development

## Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|------------|
| API performance with large datasets | High | Implement pagination, caching, and database indexing |
| Data sync delays from Android devices | Medium | Show last sync timestamp, implement manual refresh |
| Browser compatibility issues | Low | Use modern Angular features, test on target browsers |
| Complex chart requirements | Medium | Use proven chart library (Chart.js/ng2-charts) |

## Questions to Resolve
1. What is the expected data retention period? (affects query performance)
2. Should we support multiple store locations in Phase 1?
3. What are the specific low stock thresholds?
4. Do we need real-time updates or is periodic refresh acceptable?
5. What authentication mechanism should be used? (JWT, cookies, etc.)

## Next Steps
1. Review and approve requirements
2. Create technical design document
3. Design API endpoints for analytics
4. Create UI mockups/wireframes
5. Break down into implementation tasks
