import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DashboardSummary,
  SalesReport,
  SalesByCashier,
  SalesDataPoint,
  StockLevel,
  CustomerInsights,
  RevenueTrend,
  TopProduct,
  SalesReportParams,
  AnalyticsParams,
  Device
} from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  // Dashboard endpoints
  getDashboardSummary(startDate: string, endDate: string, deviceId?: string): Observable<DashboardSummary> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    if (deviceId) {
      params = params.set('deviceId', deviceId);
    }
    
    return this.http.get<DashboardSummary>(`${this.baseUrl}/dashboard/summary`, { params });
  }

  // Device endpoints
  getDevices(): Observable<Device[]> {
    return this.http.get<Device[]>(`${this.baseUrl}/dashboard/devices`);
  }

  // Reports endpoints
  getSalesReport(params: SalesReportParams): Observable<SalesReport> {
    let httpParams = new HttpParams();
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.groupBy) httpParams = httpParams.set('groupBy', params.groupBy);
    if (params.deviceId) httpParams = httpParams.set('deviceId', params.deviceId);
    
    return this.http.get<SalesReport>(`${this.baseUrl}/reports/sales`, { params: httpParams });
  }

  getSalesByCashier(startDate: string, endDate: string, deviceId?: string): Observable<SalesByCashier[]> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    if (deviceId) {
      params = params.set('deviceId', deviceId);
    }
    
    return this.http.get<SalesByCashier[]>(`${this.baseUrl}/reports/sales/by-cashier`, { params });
  }

  getSalesByHour(date: string): Observable<SalesDataPoint[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<SalesDataPoint[]>(`${this.baseUrl}/reports/sales/by-hour`, { params });
  }

  getStockLevels(): Observable<StockLevel[]> {
    return this.http.get<StockLevel[]>(`${this.baseUrl}/reports/inventory/stock-levels`);
  }

  getLowStock(threshold: number = 7): Observable<StockLevel[]> {
    const params = new HttpParams().set('threshold', threshold.toString());
    return this.http.get<StockLevel[]>(`${this.baseUrl}/reports/inventory/low-stock`, { params });
  }

  getRevenueReport(params: SalesReportParams): Observable<SalesDataPoint[]> {
    let httpParams = new HttpParams();
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.groupBy) httpParams = httpParams.set('groupBy', params.groupBy);
    
    return this.http.get<SalesDataPoint[]>(`${this.baseUrl}/reports/revenue`, { params: httpParams });
  }

  // Analytics endpoints
  getTopProducts(params: AnalyticsParams): Observable<TopProduct[]> {
    let httpParams = new HttpParams();
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.limit) httpParams = httpParams.set('limit', params.limit.toString());
    if (params.deviceId) httpParams = httpParams.set('deviceId', params.deviceId);
    
    return this.http.get<TopProduct[]>(`${this.baseUrl}/analytics/top-products`, { params: httpParams });
  }

  getCustomerInsights(startDate: string, endDate: string, deviceId?: string): Observable<CustomerInsights> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    if (deviceId) {
      params = params.set('deviceId', deviceId);
    }
    
    return this.http.get<CustomerInsights>(`${this.baseUrl}/analytics/customer-insights`, { params });
  }

  getRevenueTrends(params: SalesReportParams): Observable<RevenueTrend> {
    let httpParams = new HttpParams();
    if (params.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params.groupBy) httpParams = httpParams.set('groupBy', params.groupBy);
    if (params.deviceId) httpParams = httpParams.set('deviceId', params.deviceId);
    
    return this.http.get<RevenueTrend>(`${this.baseUrl}/analytics/revenue-trends`, { params: httpParams });
  }

  getProductPerformance(productId: number, startDate: string, endDate: string): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get(`${this.baseUrl}/analytics/product-performance/${productId}`, { params });
  }

  getPeakHours(startDate: string, endDate: string): Observable<any[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    
    return this.http.get<any[]>(`${this.baseUrl}/analytics/peak-hours`, { params });
  }
}
