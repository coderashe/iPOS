import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { switchMap } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';
import { DashboardStateService } from '../../core/services/dashboard-state.service';
import { DateRangePickerComponent } from '../../shared/components/date-range-picker/date-range-picker.component';
import { SalesReport, SalesByCashier } from '../../core/models/api-models';

@Component({
  selector: 'app-sales-analytics',
  standalone: true,
  imports: [CommonModule, DateRangePickerComponent],
  template: `
    <div class="sales-container">
      <h1>Sales Analytics</h1>
      
      <app-date-range-picker></app-date-range-picker>

      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading sales data...</p>
      </div>

      <div *ngIf="error" class="error-message">
        <span class="error-icon">⚠️</span>
        <p>{{ error }}</p>
      </div>

      <div *ngIf="!loading && !error" class="sales-content">
        <div class="section">
          <h2>Sales Overview</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <h3>Total Revenue</h3>
              <p class="stat-value">{{ salesReport?.totalRevenue | currency }}</p>
            </div>
            <div class="stat-card">
              <h3>Total Transactions</h3>
              <p class="stat-value">{{ salesReport?.totalTransactions }}</p>
            </div>
          </div>
        </div>

        <div class="section">
          <h2>Sales by Cashier</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Cashier</th>
                  <th>Total Sales</th>
                  <th>Transactions</th>
                  <th>Avg Transaction</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let cashier of salesByCashier">
                  <td>{{ cashier.cashierName }}</td>
                  <td>{{ cashier.totalSales | currency }}</td>
                  <td>{{ cashier.transactionCount }}</td>
                  <td>{{ cashier.averageTransactionValue | currency }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .sales-container {
      padding: 2rem;
      max-width: 1400px;
      margin: 0 auto;

      h1 {
        margin: 0 0 1.5rem 0;
        color: #333;
        font-size: 2rem;
        font-weight: 700;
      }

      .loading {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 4rem;

        .spinner {
          width: 50px;
          height: 50px;
          border: 4px solid #f3f3f3;
          border-top: 4px solid #1976d2;
          border-radius: 50%;
          animation: spin 1s linear infinite;
          margin-bottom: 1rem;
        }
      }

      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }

      .error-message {
        display: flex;
        align-items: center;
        gap: 1rem;
        padding: 1.5rem;
        background: #ffebee;
        border: 1px solid #ef5350;
        border-radius: 8px;
      }

      .section {
        background: white;
        border-radius: 8px;
        padding: 1.5rem;
        margin-bottom: 1.5rem;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        h2 {
          margin: 0 0 1.5rem 0;
          color: #333;
          font-size: 1.5rem;
        }
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 1rem;
      }

      .stat-card {
        padding: 1rem;
        background: #f5f5f5;
        border-radius: 4px;

        h3 {
          margin: 0 0 0.5rem 0;
          font-size: 0.875rem;
          color: #666;
          text-transform: uppercase;
        }

        .stat-value {
          margin: 0;
          font-size: 1.5rem;
          font-weight: 700;
          color: #333;
        }
      }

      .table-container {
        overflow-x: auto;

        table {
          width: 100%;
          border-collapse: collapse;

          th, td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid #eee;
          }

          th {
            background: #f5f5f5;
            font-weight: 600;
            color: #666;
            text-transform: uppercase;
            font-size: 0.875rem;
          }

          tbody tr:hover {
            background: #f9f9f9;
          }
        }
      }
    }

    @media (max-width: 768px) {
      .sales-container {
        padding: 1rem;

        h1 {
          font-size: 1.5rem;
        }
      }
    }
  `]
})
export class SalesAnalyticsComponent implements OnInit {
  private apiService = inject(ApiService);
  private stateService = inject(DashboardStateService);

  salesReport: SalesReport | null = null;
  salesByCashier: SalesByCashier[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.stateService.dateRange$.pipe(
      switchMap(range => {
        this.loading = true;
        this.error = null;
        const startDate = range.startDate.toISOString();
        const endDate = range.endDate.toISOString();
        
        return this.apiService.getSalesReport({ startDate, endDate, groupBy: 'day' });
      })
    ).subscribe({
      next: (data) => {
        this.salesReport = data;
        this.loadCashierData();
      },
      error: (err) => {
        this.error = 'Failed to load sales data. Please check if the API is running.';
        this.loading = false;
        console.error('Sales error:', err);
      }
    });
  }

  private loadCashierData(): void {
    const range = this.stateService.getCurrentDateRange();
    this.apiService.getSalesByCashier(
      range.startDate.toISOString(),
      range.endDate.toISOString()
    ).subscribe({
      next: (data) => {
        this.salesByCashier = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Cashier data error:', err);
        this.loading = false;
      }
    });
  }
}
