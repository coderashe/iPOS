import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { from } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ApiService } from '../../core/services/api.service';
import { DashboardStateService } from '../../core/services/dashboard-state.service';
import { FirestoreDataService, FirestoreInvoice } from '../../core/services/firestore-data.service';
import { AuthService } from '../../core/services/auth.service';
import { DateRangePickerComponent } from '../../shared/components/date-range-picker/date-range-picker.component';
import { DeviceFilterComponent } from '../../shared/components/device-filter/device-filter.component';
import { SalesReport, SalesByCashier } from '../../core/models/api-models';

@Component({
  selector: 'app-sales-analytics',
  standalone: true,
  imports: [CommonModule, DateRangePickerComponent, DeviceFilterComponent],
  template: `
    <div class="sales-container">
      <h1>Sales Analytics</h1>
      
      <app-date-range-picker></app-date-range-picker>
      
      <app-device-filter (deviceChange)="onDeviceChange($event)"></app-device-filter>

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
              <p class="stat-value">{{ salesReport?.revenueFormatted || (salesReport?.totalRevenue | currency) }}</p>
            </div>
            <div class="stat-card">
              <h3>Total Transactions</h3>
              <p class="stat-value">{{ salesReport?.totalTransactions }}</p>
            </div>
          </div>
        </div>

        <div class="section">
          <h2>Transactions & Purchased Items</h2>
          <div *ngIf="invoices.length === 0" class="empty-state">
            <p>No transactions found for the selected date range.</p>
          </div>
          <div *ngIf="invoices.length > 0" class="table-container">
            <table class="transactions-table">
              <thead>
                <tr>
                  <th style="width: 40px;"></th>
                  <th>Invoice #</th>
                  <th>Date & Time</th>
                  <th>Register / Device</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <ng-container *ngFor="let inv of invoices">
                  <tr class="transaction-row" (click)="toggleExpand(inv.invoiceId)">
                    <td class="expand-icon">
                      <span class="arrow" [class.expanded]="expandedInvoiceId === inv.invoiceId">
                        {{ expandedInvoiceId === inv.invoiceId ? '▼' : '▶' }}
                      </span>
                    </td>
                    <td class="invoice-id"><strong>#{{ inv.invoiceId }}</strong></td>
                    <td>{{ formatDate(inv.invoiceDate) }}</td>
                    <td>{{ inv.deviceName || inv.deviceId || 'Main Register' }}</td>
                    <td>
                      <span class="badge">{{ inv.items?.length || 0 }} items</span>
                    </td>
                    <td class="amount-col">
                      <strong>{{ formatCurrency(inv.totalAmount, inv.currencyCode || inv.currency) }}</strong>
                    </td>
                    <td>
                      <span class="status-badge" [class.paid]="(inv.status || 'Paid').toLowerCase() === 'paid'">
                        {{ inv.status || 'Paid' }}
                      </span>
                    </td>
                  </tr>

                  <!-- Expandable Line Items Accordion -->
                  <tr *ngIf="expandedInvoiceId === inv.invoiceId" class="details-row">
                    <td colspan="7">
                      <div class="receipt-details">
                        <div class="receipt-header">
                          <h4>Purchased Items Breakdown — Invoice #{{ inv.invoiceId }}</h4>
                          <span class="receipt-date">{{ formatDate(inv.invoiceDate) }}</span>
                        </div>
                        <table *ngIf="inv.items && inv.items.length > 0" class="items-table">
                          <thead>
                            <tr>
                              <th>Product</th>
                              <th>Qty Sold</th>
                              <th>Unit Price</th>
                              <th>Line Total</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr *ngFor="let item of inv.items">
                              <td class="item-name">{{ item.productName || 'Product' }}</td>
                              <td>{{ item.productQtySold || 1 }}</td>
                              <td>{{ formatCurrency(item.productPrice, inv.currencyCode || inv.currency) }}</td>
                              <td class="item-total">
                                <strong>{{ formatCurrency(item.totalAmount || ((item.productPrice || 0) * (item.productQtySold || 1)), inv.currencyCode || inv.currency) }}</strong>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                        <div *ngIf="!inv.items || inv.items.length === 0" class="no-items">
                          <p>No itemized lines available for this invoice.</p>
                        </div>
                      </div>
                    </td>
                  </tr>
                </ng-container>
              </tbody>
            </table>
          </div>
        </div>

        <div class="section">
          <h2>Sales by Cashier</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Cashier / Register</th>
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

      .empty-state {
        padding: 2rem;
        text-align: center;
        color: #777;
        font-style: italic;
      }

      .table-container {
        overflow-x: auto;

        table {
          width: 100%;
          border-collapse: collapse;

          th, td {
            padding: 0.875rem 1rem;
            text-align: left;
            border-bottom: 1px solid #eee;
          }

          th {
            background: #f8f9fa;
            font-weight: 600;
            color: #555;
            text-transform: uppercase;
            font-size: 0.8rem;
            letter-spacing: 0.5px;
          }

          tbody tr:hover {
            background: #fcfcfc;
          }
        }

        .transaction-row {
          cursor: pointer;
          transition: background 0.15s ease;

          &:hover {
            background: #f0f7ff !important;
          }
        }

        .expand-icon {
          text-align: center;
          color: #1976d2;
          font-size: 0.85rem;
        }

        .badge {
          display: inline-block;
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 0.75rem;
          background: #e3f2fd;
          color: #1976d2;
          font-weight: 500;
        }

        .status-badge {
          display: inline-block;
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 0.75rem;
          background: #f5f5f5;
          color: #666;

          &.paid {
            background: #e8f5e9;
            color: #2e7d32;
            font-weight: 600;
          }
        }

        .details-row {
          background: #fafbfc !important;

          td {
            padding: 0.5rem 1rem 1rem 1rem;
          }
        }

        .receipt-details {
          background: white;
          border: 1px solid #e1e4e8;
          border-radius: 6px;
          padding: 1rem 1.25rem;
          margin: 0.25rem 0;
          box-shadow: 0 1px 3px rgba(0,0,0,0.05);

          .receipt-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid #eee;
            padding-bottom: 0.5rem;
            margin-bottom: 0.75rem;

            h4 {
              margin: 0;
              font-size: 0.95rem;
              color: #1976d2;
            }

            .receipt-date {
              font-size: 0.8rem;
              color: #777;
            }
          }

          .items-table {
            width: 100%;
            border-collapse: collapse;

            th {
              background: #f1f3f5;
              padding: 0.5rem 0.75rem;
              font-size: 0.75rem;
              color: #444;
            }

            td {
              padding: 0.5rem 0.75rem;
              font-size: 0.85rem;
              border-bottom: 1px solid #f0f0f0;
            }

            .item-name {
              font-weight: 500;
              color: #222;
            }

            .item-total {
              color: #2e7d32;
            }
          }

          .no-items {
            padding: 0.75rem;
            color: #888;
            font-size: 0.85rem;
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
  private firestoreService = inject(FirestoreDataService);
  private authService = inject(AuthService);

  salesReport: SalesReport | null = null;
  salesByCashier: SalesByCashier[] = [];
  invoices: FirestoreInvoice[] = [];
  expandedInvoiceId: number | string | null = null;
  loading = true;
  error: string | null = null;
  selectedDeviceId: string = '';

  ngOnInit(): void {
    this.loadSalesData();
  }

  private loadSalesData(): void {
    const storeId = this.authService.getStoreId() || 'ISTART';
    this.stateService.dateRange$.pipe(
      switchMap(range => {
        this.loading = true;
        this.error = null;
        return from(this.firestoreService.getSalesReport(
          storeId,
          range.startDate,
          range.endDate,
          this.selectedDeviceId || undefined
        ));
      })
    ).subscribe({
      next: (res) => {
        this.salesReport = res.report;
        this.salesByCashier = res.cashiers;
        this.invoices = res.invoices || [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load sales data.';
        this.loading = false;
        console.error('Sales error:', err);
      }
    });
  }

  onDeviceChange(deviceId: string): void {
    this.selectedDeviceId = deviceId;
    this.loadSalesData();
  }

  toggleExpand(id: number | string | undefined): void {
    if (id === undefined || id === null) return;
    this.expandedInvoiceId = this.expandedInvoiceId === id ? null : id;
  }

  formatDate(dateVal: any): string {
    if (!dateVal) return 'N/A';
    let ms = 0;
    if (typeof dateVal === 'number') ms = dateVal;
    else if (typeof dateVal?.toMillis === 'function') ms = dateVal.toMillis();
    else if (typeof dateVal === 'string') ms = new Date(dateVal).getTime();
    if (ms > 0 && ms < 10000000000) ms *= 1000;
    return ms > 0 ? new Date(ms).toLocaleString() : 'N/A';
  }

  formatCurrency(amount: any, currencyCode?: string): string {
    const val = Number(amount) || 0;
    const curr = (currencyCode || 'USD').toUpperCase();
    if (curr === 'USD') return `$${val.toFixed(2)}`;
    if (curr === 'ZAR') return `R${val.toFixed(2)}`;
    if (curr === 'ZIG') return `ZiG ${val.toLocaleString()}`;
    return `${curr} ${val.toFixed(2)}`;
  }
}
