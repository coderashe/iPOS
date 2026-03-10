// Dashboard DTOs
export interface DashboardSummary {
  totalSales: number;
  totalRevenue: number;
  transactionCount: number;
  averageTransactionValue: number;
  topProducts: TopProduct[];
  lowStockCount: number;
  activeCustomersCount: number;
  lastSyncTime: string;
}

export interface TopProduct {
  productId: number;
  productName: string;
  quantitySold: number;
  revenue: number;
}

// Reports DTOs
export interface SalesReport {
  dataPoints: SalesDataPoint[];
  totalRevenue: number;
  totalTransactions: number;
}

export interface SalesDataPoint {
  date: string;
  revenue: number;
  transactionCount: number;
  itemsSold: number;
}

export interface SalesByCashier {
  cashierName: string;
  totalSales: number;
  transactionCount: number;
  averageTransactionValue: number;
}

export interface StockLevel {
  productId: number;
  productName: string;
  currentStock: number;
  averageDailySales: number;
  daysRemaining: number | null;
}

// Analytics DTOs
export interface CustomerInsights {
  totalCustomers: number;
  newCustomers: number;
  topCustomers: TopCustomer[];
  averageCustomerValue: number;
  totalReceivables: number;
}

export interface TopCustomer {
  customerId: number;
  customerName: string;
  customerPhone: string;
  totalSpent: number;
  purchaseCount: number;
}

export interface RevenueTrend {
  dataPoints: TrendDataPoint[];
  growthRate: number;
  trend: string;
}

export interface TrendDataPoint {
  date: string;
  value: number;
}

// Date Range
export interface DateRange {
  startDate: Date;
  endDate: Date;
}

// API Request Parameters
export interface SalesReportParams {
  startDate?: string;
  endDate?: string;
  groupBy?: 'day' | 'week' | 'month';
}

export interface AnalyticsParams {
  startDate?: string;
  endDate?: string;
  limit?: number;
}
