import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { switchMap } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';
import { DashboardStateService } from '../../core/services/dashboard-state.service';
import { SummaryCardComponent } from '../../shared/components/summary-card/summary-card.component';
import { DateRangePickerComponent } from '../../shared/components/date-range-picker/date-range-picker.component';
import { DeviceFilterComponent } from '../../shared/components/device-filter/device-filter.component';
import { DashboardSummary } from '../../core/models/api-models';

interface SummaryCard {
  title: string;
  value: number | string;
  icon: string;
  format: 'number' | 'currency' | 'percent';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SummaryCardComponent, DateRangePickerComponent, DeviceFilterComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private apiService = inject(ApiService);
  private stateService = inject(DashboardStateService);

  summary: DashboardSummary | null = null;
  summaryCards: SummaryCard[] = [];
  loading = true;
  error: string | null = null;
  selectedDeviceId: string = '';

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private loadDashboardData(): void {
    // Subscribe to date range changes and reload dashboard
    this.stateService.dateRange$.pipe(
      switchMap(range => {
        this.loading = true;
        this.error = null;
        return this.apiService.getDashboardSummary(
          range.startDate.toISOString(),
          range.endDate.toISOString(),
          this.selectedDeviceId || undefined
        );
      })
    ).subscribe({
      next: (data) => {
        this.summary = data;
        this.updateSummaryCards(data);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load dashboard data. Please check if the API is running.';
        this.loading = false;
        console.error('Dashboard error:', err);
      }
    });
  }

  onDeviceChange(deviceId: string): void {
    this.selectedDeviceId = deviceId;
    this.loadDashboardData();
  }

  private updateSummaryCards(data: DashboardSummary): void {
    this.summaryCards = [
      {
        title: 'Total Revenue',
        value: data.totalRevenue,
        icon: '💰',
        format: 'currency'
      },
      {
        title: 'Total Sales',
        value: data.totalSales,
        icon: '📦',
        format: 'number'
      },
      {
        title: 'Transactions',
        value: data.transactionCount,
        icon: '🧾',
        format: 'number'
      },
      {
        title: 'Avg Transaction',
        value: data.averageTransactionValue,
        icon: '📊',
        format: 'currency'
      },
      {
        title: 'Active Customers',
        value: data.activeCustomersCount,
        icon: '👥',
        format: 'number'
      },
      {
        title: 'Low Stock Items',
        value: data.lowStockCount,
        icon: '⚠️',
        format: 'number'
      }
    ];
  }

  onDateRangeChange(): void {
    // Date range change is handled by the state service subscription
  }
}
