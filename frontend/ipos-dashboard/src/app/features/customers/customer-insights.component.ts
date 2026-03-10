import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { switchMap } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';
import { DashboardStateService } from '../../core/services/dashboard-state.service';
import { DateRangePickerComponent } from '../../shared/components/date-range-picker/date-range-picker.component';
import { SummaryCardComponent } from '../../shared/components/summary-card/summary-card.component';
import { CustomerInsights } from '../../core/models/api-models';

@Component({
  selector: 'app-customer-insights',
  standalone: true,
  imports: [CommonModule, DateRangePickerComponent, SummaryCardComponent],
  template: `
    <div class="customers-container">
      <h1>Customer Insights</h1>
      
      <app-date-range-picker></app-date-range-picker>

      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading customer data...</p>
      </div>

      <div *ngIf="error" class="error-message">
        <span class="error-icon">⚠️</span>
        <p>{{ error }}</p>
      </div>

      <div *ngIf="!loading && !error && insights" class="customers-content">
        <div class="summary-cards">
          <app-summary-card
            title="Total Customers"
            [value]="insights.totalCustomers"
            icon="👥"
            format="number"
          ></app-summary-card>
          <app-summary-card
            title="New Customers"
            [value]="insights.newCustomers"
            icon="✨"
            format="number"
          ></app-summary-card>
          <app-summary-card
            title="Avg Customer Value"
            [value]="insights.averageCustomerValue"
            icon="💎"
            format="currency"
          ></app-summary-card>
          <app-summary-card
            title="Total Receivables"
            [value]="insights.totalReceivables"
            icon="💳"
            format="currency"
          ></app-summary-card>
        </div>

        <div class="section">
          <h2>Top Customers</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Customer</th>
                  <th>Phone</th>
                  <th>Total Spent</th>
                  <th>Purchases</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let customer of insights.topCustomers">
                  <td>{{ customer.customerName }}</td>
                  <td>{{ customer.customerPhone }}</td>
                  <td>{{ customer.totalSpent | currency }}</td>
                  <td>{{ customer.purchaseCount }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .customers-container {
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

      .summary-cards {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 1.5rem;
        margin-bottom: 2rem;
      }

      .section {
        background: white;
        border-radius: 8px;
        padding: 1.5rem;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

        h2 {
          margin: 0 0 1.5rem 0;
          color: #333;
          font-size: 1.5rem;
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
      .customers-container {
        padding: 1rem;

        h1 {
          font-size: 1.5rem;
        }

        .summary-cards {
          grid-template-columns: 1fr;
        }
      }
    }
  `]
})
export class CustomerInsightsComponent implements OnInit {
  private apiService = inject(ApiService);
  private stateService = inject(DashboardStateService);

  insights: CustomerInsights | null = null;
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.stateService.dateRange$.pipe(
      switchMap(range => {
        this.loading = true;
        this.error = null;
        return this.apiService.getCustomerInsights(
          range.startDate.toISOString(),
          range.endDate.toISOString()
        );
      })
    ).subscribe({
      next: (data) => {
        this.insights = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load customer data. Please check if the API is running.';
        this.loading = false;
        console.error('Customer insights error:', err);
      }
    });
  }
}
