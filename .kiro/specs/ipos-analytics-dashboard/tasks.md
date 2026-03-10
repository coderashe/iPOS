# Implementation Plan: iPOS Analytics Dashboard

## Overview

This implementation plan breaks down the iPOS Analytics Dashboard into discrete coding tasks. The feature consists of two main components: an enhanced ASP.NET Core API with analytics endpoints and a new Angular dashboard application. Tasks are organized to build incrementally, starting with backend infrastructure, then frontend setup, followed by feature implementation, and finally integration and testing.

## Tasks

- [x] 1. Backend API - Database and Infrastructure Setup
  - [x] 1.1 Update database connection string to use iPosDb
    - Modify appsettings.json to point to iPosDb database
    - Update DbContext configuration if needed
    - _Requirements: Database configuration_
  
  - [x] 1.2 Create database indexes for performance optimization
    - Create indexes on InvoiceLines table (InvoiceDate, ProductID, CustomerID, Cashier)
    - Create index on Products table (CurrentStock)
    - Create composite index on InvoiceLines (InvoiceDate, InvoiceStatus)
    - _Requirements: Performance optimization_
  
  - [x] 1.3 Implement caching service
    - Create CacheService class with GetOrCreateAsync method
    - Configure IMemoryCache in dependency injection
    - _Requirements: Performance optimization_
  
  - [x] 1.4 Configure CORS for Angular dashboard
    - Add CORS policy "DashboardPolicy" allowing http://localhost:4200
    - Configure to allow any header and method
    - _Requirements: API security and cross-origin access_

- [x] 2. Backend API - DTOs and Models
  - [x] 2.1 Create dashboard DTOs
    - Create DashboardSummaryDto class
    - Create TopProductDto class
    - _Requirements: Dashboard summary endpoint_
  
  - [x] 2.2 Create reports DTOs
    - Create SalesReportDto, SalesDataPointDto classes
    - Create SalesByCashierDto class
    - Create StockLevelDto class
    - _Requirements: Reports endpoints_
  
  - [x] 2.3 Create analytics DTOs
    - Create CustomerInsightsDto, TopCustomerDto classes
    - Create RevenueTrendDto, TrendDataPointDto classes
    - _Requirements: Analytics endpoints_

- [x] 3. Backend API - DashboardController Implementation
  - [x] 3.1 Create DashboardController with summary endpoint
    - Implement GET /api/dashboard/summary endpoint
    - Query InvoiceLines for total sales, revenue, transaction count
    - Calculate average transaction value
    - Query for top products by quantity sold
    - Query Products for low stock count
    - Query Customers for active customers count
    - Implement caching with 5-minute expiration
    - _Requirements: Dashboard summary data_
  
  - [ ]* 3.2 Write unit tests for DashboardController
    - Test summary endpoint returns correct data structure
    - Test date range filtering
    - Test caching behavior
    - _Requirements: Dashboard summary data_

- [x] 4. Backend API - ReportsController Implementation
  - [x] 4.1 Implement sales report endpoints
    - Create GET /api/reports/sales endpoint with groupBy parameter
    - Implement grouping by day, week, month
    - Use AsNoTracking() for read-only queries
    - Implement caching with 15-minute expiration
    - _Requirements: Sales reporting_
  
  - [x] 4.2 Implement sales by cashier endpoint
    - Create GET /api/reports/sales/by-cashier endpoint
    - Group InvoiceLines by Cashier field
    - Calculate total sales and transaction count per cashier
    - _Requirements: Cashier performance reporting_
  
  - [x] 4.3 Implement sales by hour endpoint
    - Create GET /api/reports/sales/by-hour endpoint
    - Group sales data by hour of day
    - _Requirements: Peak hours analysis_
  
  - [x] 4.4 Implement inventory report endpoints
    - Create GET /api/reports/inventory/stock-levels endpoint
    - Create GET /api/reports/inventory/low-stock endpoint with threshold parameter
    - Query Products table for current stock levels
    - Calculate days of stock remaining based on sales velocity
    - _Requirements: Inventory management_
  
  - [x] 4.5 Implement revenue report endpoint
    - Create GET /api/reports/revenue endpoint with groupBy parameter
    - Aggregate revenue data by specified time period
    - _Requirements: Financial reporting_
  
  - [ ]* 4.6 Write unit tests for ReportsController
    - Test each endpoint returns correct data structure
    - Test date range filtering and grouping logic
    - Test pagination if implemented
    - _Requirements: Sales and inventory reporting_

- [x] 5. Backend API - AnalyticsController Implementation
  - [x] 5.1 Implement top products endpoint
    - Create GET /api/analytics/top-products endpoint with limit parameter
    - Query InvoiceLines grouped by ProductID
    - Order by quantity sold or revenue
    - _Requirements: Product performance analytics_
  
  - [x] 5.2 Implement customer insights endpoint
    - Create GET /api/analytics/customer-insights endpoint
    - Calculate total customers, new customers in date range
    - Query top customers by total spent
    - Calculate average customer value
    - Sum total receivables from Orders table
    - _Requirements: Customer analytics_
  
  - [x] 5.3 Implement revenue trends endpoint
    - Create GET /api/analytics/revenue-trends endpoint
    - Calculate growth rate between periods
    - Determine trend direction (increasing, decreasing, stable)
    - _Requirements: Revenue trend analysis_
  
  - [x] 5.4 Implement product performance endpoint
    - Create GET /api/analytics/product-performance endpoint with productId parameter
    - Query sales history for specific product
    - Calculate performance metrics
    - _Requirements: Product-specific analytics_
  
  - [x] 5.5 Implement peak hours endpoint
    - Create GET /api/analytics/peak-hours endpoint
    - Analyze transaction patterns by hour
    - Identify busiest hours
    - _Requirements: Peak hours analysis_
  
  - [ ]* 5.6 Write unit tests for AnalyticsController
    - Test each analytics endpoint
    - Test calculation accuracy
    - Test edge cases (no data, single data point)
    - _Requirements: Analytics functionality_

- [ ] 6. Checkpoint - Backend API Complete
  - Ensure all API endpoints are functional
  - Test endpoints using Swagger UI
  - Verify caching is working
  - Ask the user if questions arise

- [-] 7. Frontend - Angular Project Setup
  - [x] 7.1 Create new Angular project
    - Run `ng new ipos-dashboard` in frontend directory
    - Configure for standalone components
    - Enable strict TypeScript mode
    - Set up SCSS for styling
    - _Requirements: Angular dashboard application_
  
  - [ ] 7.2 Install required dependencies
    - Install Chart.js and ng2-charts: `npm install chart.js ng2-charts`
    - Install Angular Material or PrimeNG: `npm install @angular/material` or `npm install primeng primeicons`
    - Install date-fns for date manipulation: `npm install date-fns`
    - Install export libraries: `npm install html2canvas jspdf xlsx`
    - _Requirements: Charting and UI components_
  
  - [ ] 7.3 Configure environment files
    - Set up environment.ts with development API URL (http://localhost:5000/api)
    - Set up environment.prod.ts with production API URL placeholder
    - _Requirements: Environment configuration_
  
  - [ ] 7.4 Set up routing configuration
    - Create app.routes.ts with routes for dashboard, sales, inventory, customers, financial
    - Configure lazy loading for feature components
    - Add wildcard route redirecting to dashboard
    - _Requirements: Navigation structure_

- [ ] 8. Frontend - Core Services and Infrastructure
  - [ ] 8.1 Create API service
    - Create core/services/api.service.ts
    - Implement methods for all API endpoints (dashboard, reports, analytics)
    - Use HttpClient with proper typing
    - _Requirements: API communication_
  
  - [ ] 8.2 Create authentication service
    - Create core/services/auth.service.ts
    - Implement login, logout, token storage methods
    - Implement isAuthenticated() method checking token validity
    - _Requirements: User authentication_
  
  - [ ] 8.3 Create state management service
    - Create core/services/dashboard-state.service.ts
    - Implement BehaviorSubject for date range state
    - Implement methods to update and observe state
    - _Requirements: Shared state management_
  
  - [ ] 8.4 Create API models
    - Create core/models/api-models.ts
    - Define TypeScript interfaces matching all DTOs from backend
    - _Requirements: Type safety_
  
  - [ ] 8.5 Implement HTTP interceptors
    - Create core/interceptors/auth.interceptor.ts to add JWT token to requests
    - Create core/interceptors/error.interceptor.ts for global error handling
    - Configure interceptors in app.config.ts
    - _Requirements: Authentication and error handling_
  
  - [ ] 8.6 Implement auth guard
    - Create core/guards/auth.guard.ts
    - Implement CanActivate interface checking authentication status
    - Redirect to login if not authenticated
    - Apply guard to all protected routes
    - _Requirements: Route protection_

- [ ] 9. Frontend - Shared Components
  - [ ] 9.1 Create date-range-picker component
    - Create shared/components/date-range-picker component
    - Implement start date and end date inputs
    - Emit rangeChange event when dates change
    - Add preset options (Today, Last 7 days, Last 30 days, This Month)
    - _Requirements: Date filtering across all views_
  
  - [ ] 9.2 Create summary-card component
    - Create shared/components/summary-card component
    - Accept inputs: title, value, icon, trend
    - Display formatted value with icon
    - Show trend indicator (up/down arrow with percentage)
    - Make responsive for mobile, tablet, desktop
    - _Requirements: Dashboard summary display_
  
  - [ ] 9.3 Create chart-wrapper component
    - Create shared/components/chart-wrapper component
    - Accept inputs: title, chartData, chartType
    - Wrap Chart.js canvas with title and container
    - Implement responsive sizing
    - _Requirements: Consistent chart display_
  
  - [ ] 9.4 Create export-button component
    - Create shared/components/export-button component
    - Implement exportToPDF() method using html2canvas and jsPDF
    - Implement exportToExcel() method using XLSX library
    - Accept input for data to export and filename
    - _Requirements: Data export functionality_
  
  - [ ] 9.5 Create utility pipes
    - Create shared/pipes/currency-format.pipe.ts for currency formatting
    - Create shared/pipes/date-format.pipe.ts for date formatting
    - _Requirements: Consistent data formatting_

- [ ] 10. Frontend - Dashboard Feature
  - [ ] 10.1 Implement dashboard component
    - Create features/dashboard/dashboard.component.ts
    - Subscribe to date range changes from state service
    - Call API service to fetch dashboard summary
    - Display summary cards for total sales, revenue, transactions, average transaction value
    - Display low stock count and active customers count
    - _Requirements: Dashboard overview_
  
  - [ ] 10.2 Add sales trend chart to dashboard
    - Create chart configuration for line chart
    - Fetch sales data from API
    - Transform data for Chart.js format
    - Display using chart-wrapper component
    - _Requirements: Sales visualization_
  
  - [ ] 10.3 Add top products chart to dashboard
    - Create chart configuration for bar chart
    - Display top 5 products by revenue
    - Use horizontal bar chart
    - _Requirements: Product performance visualization_
  
  - [ ]* 10.4 Write unit tests for dashboard component
    - Test component initialization
    - Test data loading from API
    - Test date range change handling
    - _Requirements: Dashboard functionality_

- [ ] 11. Frontend - Sales Analytics Feature
  - [ ] 11.1 Create sales analytics component
    - Create features/sales/sales-analytics.component.ts
    - Implement date range picker integration
    - Fetch sales report data from API
    - _Requirements: Sales analytics view_
  
  - [ ] 11.2 Create sales chart component
    - Create features/sales/components/sales-chart.component.ts
    - Implement line chart showing sales over time
    - Support grouping by day, week, month
    - Add dropdown to switch grouping
    - _Requirements: Sales trend visualization_
  
  - [ ] 11.3 Create cashier performance component
    - Create features/sales/components/cashier-performance.component.ts
    - Fetch sales by cashier data from API
    - Display in table format with sorting
    - Add bar chart visualization
    - _Requirements: Cashier performance tracking_
  
  - [ ] 11.4 Add sales by hour visualization
    - Fetch peak hours data from API
    - Create bar chart showing sales by hour of day
    - Highlight peak hours
    - _Requirements: Peak hours analysis_
  
  - [ ]* 11.5 Write unit tests for sales components
    - Test data fetching and display
    - Test chart rendering
    - Test grouping changes
    - _Requirements: Sales analytics functionality_

- [ ] 12. Frontend - Inventory Reports Feature
  - [ ] 12.1 Create inventory reports component
    - Create features/inventory/inventory-reports.component.ts
    - Implement layout for stock levels and alerts
    - _Requirements: Inventory management view_
  
  - [ ] 12.2 Create stock levels component
    - Create features/inventory/components/stock-levels.component.ts
    - Fetch stock levels data from API
    - Display in table with columns: Product Name, Current Stock, Days Remaining
    - Implement sorting and filtering
    - _Requirements: Stock level monitoring_
  
  - [ ] 12.3 Create low stock alert component
    - Create features/inventory/components/low-stock-alert.component.ts
    - Fetch low stock items from API with configurable threshold
    - Display alert cards for low stock items
    - Highlight critical items (< 3 days remaining)
    - _Requirements: Low stock alerts_
  
  - [ ]* 12.4 Write unit tests for inventory components
    - Test data fetching and display
    - Test threshold filtering
    - Test sorting functionality
    - _Requirements: Inventory reporting functionality_

- [ ] 13. Frontend - Customer Insights Feature
  - [ ] 13.1 Create customer insights component
    - Create features/customers/customer-insights.component.ts
    - Fetch customer insights data from API
    - Display summary cards for total customers, new customers, average customer value
    - _Requirements: Customer analytics view_
  
  - [ ] 13.2 Add top customers table
    - Display top customers by total spent
    - Show customer name, phone, total spent, purchase count
    - Implement sorting by different columns
    - _Requirements: Top customer identification_
  
  - [ ] 13.3 Add customer value chart
    - Create pie chart showing customer segmentation
    - Segment by spending levels (high, medium, low value)
    - _Requirements: Customer segmentation visualization_
  
  - [ ]* 13.4 Write unit tests for customer insights component
    - Test data fetching and display
    - Test sorting functionality
    - Test chart rendering
    - _Requirements: Customer analytics functionality_

- [ ] 14. Frontend - Financial Reports Feature
  - [ ] 14.1 Create financial reports component
    - Create features/financial/financial-reports.component.ts
    - Fetch revenue trends data from API
    - Display revenue summary with growth rate
    - _Requirements: Financial reporting view_
  
  - [ ] 14.2 Add revenue trend chart
    - Create area chart showing revenue over time
    - Display trend indicator (increasing, decreasing, stable)
    - Show growth rate percentage
    - _Requirements: Revenue trend visualization_
  
  - [ ] 14.3 Add revenue comparison view
    - Implement period-over-period comparison
    - Compare current period vs previous period
    - Display variance percentage
    - _Requirements: Revenue comparison analysis_
  
  - [ ]* 14.4 Write unit tests for financial reports component
    - Test data fetching and display
    - Test trend calculation
    - Test comparison logic
    - _Requirements: Financial reporting functionality_

- [ ] 15. Frontend - Responsive Design and Styling
  - [ ] 15.1 Implement responsive grid layouts
    - Configure summary cards: 1 column (mobile), 2 columns (tablet), 4 columns (desktop)
    - Configure charts: stack vertically (mobile), 2x2 grid (tablet/desktop)
    - _Requirements: Mobile responsiveness_
  
  - [ ] 15.2 Implement responsive navigation
    - Create hamburger menu for mobile
    - Create sidebar navigation for desktop
    - Implement menu toggle functionality
    - _Requirements: Navigation usability_
  
  - [ ] 15.3 Create global styles and theme
    - Define color palette and typography in styles.scss
    - Create SCSS mixins for breakpoints
    - Apply consistent spacing and sizing
    - _Requirements: Consistent UI design_

- [ ] 16. Integration and End-to-End Testing
  - [ ] 16.1 Test complete user flows
    - Test login flow
    - Test dashboard data loading
    - Test navigation between all views
    - Test date range filtering across views
    - Test export functionality
    - _Requirements: Complete feature functionality_
  
  - [ ] 16.2 Test API integration
    - Verify all API endpoints are called correctly
    - Test error handling for API failures
    - Test loading states
    - _Requirements: API communication_
  
  - [ ]* 16.3 Set up E2E tests with Playwright or Cypress
    - Install E2E testing framework
    - Write tests for critical user flows
    - Test responsive behavior at different breakpoints
    - _Requirements: Automated testing_

- [ ] 17. Performance Optimization
  - [ ] 17.1 Implement frontend performance optimizations
    - Apply OnPush change detection strategy to components
    - Implement virtual scrolling for large tables
    - Add debounce to date range picker (300ms)
    - Implement chart data caching in service
    - _Requirements: Performance optimization_
  
  - [ ] 17.2 Optimize bundle size
    - Analyze bundle with webpack-bundle-analyzer
    - Ensure lazy loading is working for feature modules
    - Remove unused dependencies
    - _Requirements: Fast load times_
  
  - [ ] 17.3 Test and validate backend caching
    - Verify cache is working for dashboard summary (5 min)
    - Verify cache is working for reports (15 min)
    - Test cache invalidation scenarios
    - _Requirements: Performance optimization_

- [ ] 18. Final Checkpoint and Documentation
  - [ ] 18.1 Final testing and validation
    - Run all unit tests and ensure they pass
    - Test all features manually
    - Verify responsive design on different devices
    - Test export functionality for PDF and Excel
    - _Requirements: Complete feature validation_
  
  - [ ] 18.2 Update API documentation
    - Ensure Swagger documentation is complete for all new endpoints
    - Add XML comments to controller methods
    - _Requirements: API documentation_
  
  - [ ] 18.3 Create deployment configuration
    - Document environment variables needed
    - Create deployment checklist
    - Document database migration steps
    - _Requirements: Deployment readiness_

- [ ] 19. Final Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP delivery
- Backend tasks (1-6) should be completed before frontend tasks (7-18) to ensure API availability
- Each feature module (Dashboard, Sales, Inventory, Customers, Financial) can be developed in parallel after core infrastructure is complete
- Testing tasks are marked optional but highly recommended for production quality
- Checkpoints ensure incremental validation and provide opportunities for user feedback
- All tasks reference the design document for detailed implementation specifications
