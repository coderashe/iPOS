import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';
import { DashboardStateService } from '../../core/services/dashboard-state.service';
import { FirestoreDataService } from '../../core/services/firestore-data.service';
import { AuthService } from '../../core/services/auth.service';
import { DateRangePickerComponent } from '../../shared/components/date-range-picker/date-range-picker.component';
import { DeviceFilterComponent } from '../../shared/components/device-filter/device-filter.component';
import { RevenueTrend } from '../../core/models/api-models';

@Component({
  selector: 'app-financial-reports',
  standalone: true,
  imports: [CommonModule, DateRangePickerComponent, DeviceFilterComponent],
  template: `
    <div class="financial-container">
      <h1>Financial Reports</h1>
      
      <app-date-range-picker></app-date-range-picker>
      
      <app-device-filter (deviceChange)="onDeviceChange($event)"></app-device-filter>

      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading financial data...</p>
      </div>

      <div *ngIf="error" class="error-message">
        <span class="error-icon">⚠️</span>
        <p>{{ error }}</p>
      </div>

      <div *ngIf="!loading && !error && revenueTrend" class="financial-content">
        <div class="section">
          <h2>Revenue Trend</h2>
          <div class="trend-summary">
            <div class="trend-card">
              <h3>Growth Rate</h3>
              <p class="trend-value" [class.positive]="revenueTrend.growthRate > 0" [class.negative]="revenueTrend.growthRate < 0">
                {{ revenueTrend.growthRate | number:'1.2-2' }}%
              </p>
            </div>
            <div class="trend-card">
              <h3>Trend Direction</h3>
              <p class="trend-status" [class]="revenueTrend.trend">
                {{ revenueTrend.trend | titlecase }}
                <span class="trend-icon">
                  {{ revenueTrend.trend === 'increasing' ? '📈' : revenueTrend.trend === 'decreasing' ? '📉' : '➡️' }}
                </span>
              </p>
            </div>
          </div>
        </div>

        <div class="section">
          <h2>Revenue Data Points</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Revenue</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let point of revenueTrend.dataPoints">
                  <td>{{ point.date | date:'short' }}</td>
                  <td>{{ point.value | currency }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .financial-container {
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

      .trend-summary {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 1.5rem;
      }

      .trend-card {
        padding: 1.5rem;
        background: #f5f5f5;
        border-radius: 8px;

        h3 {
          margin: 0 0 1rem 0;
          font-size: 0.875rem;
          color: #666;
          text-transform: uppercase;
        }

        .trend-value {
          margin: 0;
          font-size: 2rem;
          font-weight: 700;

          &.positive {
            color: #4caf50;
          }

          &.negative {
            color: #f44336;
          }
        }

        .trend-status {
          margin: 0;
          font-size: 1.5rem;
          font-weight: 600;
          display: flex;
          align-items: center;
          gap: 0.5rem;

          &.increasing {
            color: #4caf50;
          }

          &.decreasing {
            color: #f44336;
          }

          &.stable {
            color: #ff9800;
          }

          .trend-icon {
            font-size: 2rem;
          }
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
      .financial-container {
        padding: 1rem;

        h1 {
          font-size: 1.5rem;
        }

        .trend-summary {
          grid-template-columns: 1fr;
        }
      }
    }
  `]
})
export class FinancialReportsComponent implements OnInit {
  private apiService = inject(ApiService);
  private stateService = inject(DashboardStateService);
  private firestoreService = inject(FirestoreDataService);
  private authService = inject(AuthService);

  revenueTrend: RevenueTrend | null = null;
  loading = true;
  error: string | null = null;
  selectedDeviceId: string = '';

  ngOnInit(): void {
    this.loadFinancialData();
  }

  private loadFinancialData(): void {
    const storeId = this.authService.getStoreId() || 'ISTART';
    this.stateService.dateRange$.pipe(
      switchMap(range => {
        this.loading = true;
        this.error = null;
        return from(this.firestoreService.getRevenueTrends(
          storeId,
          range.startDate,
          range.endDate,
          this.selectedDeviceId || undefined
        ));
      })
    ).subscribe({
      next: (data) => {
        this.revenueTrend = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load financial data.';
        this.loading = false;
        console.error('Financial reports error:', err);
      }
    });
  }

  onDeviceChange(deviceId: string): void {
    this.selectedDeviceId = deviceId;
    this.loadFinancialData();
  }
}
