import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { from, of } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';
import { DashboardStateService } from '../../core/services/dashboard-state.service';
import { FirestoreDataService } from '../../core/services/firestore-data.service';
import { AuthService } from '../../core/services/auth.service';
import { SummaryCardComponent } from '../../shared/components/summary-card/summary-card.component';
import { DateRangePickerComponent } from '../../shared/components/date-range-picker/date-range-picker.component';
import { DeviceFilterComponent } from '../../shared/components/device-filter/device-filter.component';
import { DashboardSummary } from '../../core/models/api-models';
import { environment } from '../../../environments/environment';

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
export class DashboardComponent implements OnInit, OnDestroy {
  private apiService = inject(ApiService);
  private stateService = inject(DashboardStateService);
  private firestoreService = inject(FirestoreDataService);
  private authService = inject(AuthService);

  summary: DashboardSummary | null = null;
  summaryCards: SummaryCard[] = [];
  loading = true;
  error: string | null = null;
  selectedDeviceId: string = '';
  private unsubscribeLive?: () => void;

  ngOnInit(): void {
    this.loadDashboardData();
    this.startLiveSalesSubscription();
  }

  ngOnDestroy(): void {
    if (this.unsubscribeLive) {
      this.unsubscribeLive();
    }
  }

  private startLiveSalesSubscription(): void {
    const storeId = this.authService.getStoreId() || 'ISTART';
    this.unsubscribeLive = this.firestoreService.subscribeToLiveSales(storeId, () => {
      // When live sales update in Firestore, refresh dashboard
      this.loadDashboardData();
    });
  }

  private loadDashboardData(): void {
    const storeId = this.authService.getStoreId() || 'ISTART';

    this.stateService.dateRange$.pipe(
      switchMap(range => {
        this.loading = true;
        this.error = null;

        const hasCustomApi = environment.apiUrl &&
                             environment.apiUrl.trim() !== '' &&
                             !environment.apiUrl.includes('your-production-api-url');

        const source$ = hasCustomApi
          ? this.apiService.getDashboardSummary(
              range.startDate.toISOString(),
              range.endDate.toISOString(),
              this.selectedDeviceId || undefined
            ).pipe(
              catchError((apiErr) => {
                console.info('REST API unreachable, pulling sales from Cloud Firestore...');
                return from(this.firestoreService.getDashboardSummary(storeId, range.startDate, range.endDate, this.selectedDeviceId));
              })
            )
          : from(this.firestoreService.getDashboardSummary(storeId, range.startDate, range.endDate, this.selectedDeviceId));

        return source$;
      })
    ).subscribe({
      next: (data) => {
        this.summary = data;
        this.updateSummaryCards(data);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Unable to load dashboard data. Please check connection.';
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
