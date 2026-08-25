import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { StockLevel } from '../../core/models/api-models';

@Component({
  selector: 'app-inventory-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="inventory-container">
      <h1>Inventory Reports</h1>

      <div *ngIf="loading" class="loading">
        <div class="spinner"></div>
        <p>Loading inventory data...</p>
      </div>

      <div *ngIf="error" class="error-message">
        <span class="error-icon">⚠️</span>
        <p>{{ error }}</p>
      </div>

      <div *ngIf="!loading && !error" class="inventory-content">
        <div class="section">
          <h2>Low Stock Alerts</h2>
          <div class="alert-cards">
            <div *ngFor="let item of lowStockItems" class="alert-card" [class.critical]="(item.daysRemaining ?? 999) < 3">
              <h3>{{ item.productName }}</h3>
              <p class="stock-info">Current Stock: {{ item.currentStock }}</p>
              <p class="days-remaining">{{ item.daysRemaining ?? 'N/A' }} days remaining</p>
            </div>
          </div>
          <p *ngIf="lowStockItems.length === 0" class="no-data">No low stock items</p>
        </div>

        <div class="section">
          <h2>All Stock Levels</h2>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Product Code</th>
                  <th>Product Name</th>
                  <th>Current Stock</th>
                  <th>Avg Daily Sales</th>
                  <th>Days Remaining</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of stockLevels">
                  <td>{{ item.productCode }}</td>
                  <td>{{ item.productName }}</td>
                  <td>{{ item.currentStock }}</td>
                  <td>{{ item.averageDailySales | number:'1.2-2' }}</td>
                  <td>{{ item.daysRemaining ?? 'N/A' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inventory-container {
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

      .alert-cards {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 1rem;
      }

      .alert-card {
        padding: 1rem;
        background: #fff3e0;
        border: 2px solid #ff9800;
        border-radius: 8px;

        &.critical {
          background: #ffebee;
          border-color: #f44336;
        }

        h3 {
          margin: 0 0 0.5rem 0;
          font-size: 1rem;
          color: #333;
        }

        p {
          margin: 0.25rem 0;
          font-size: 0.875rem;
          color: #666;
        }

        .days-remaining {
          font-weight: 600;
          color: #ff9800;
        }

        &.critical .days-remaining {
          color: #f44336;
        }
      }

      .no-data {
        text-align: center;
        color: #999;
        padding: 2rem;
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
      .inventory-container {
        padding: 1rem;

        h1 {
          font-size: 1.5rem;
        }

        .alert-cards {
          grid-template-columns: 1fr;
        }
      }
    }
  `]
})
export class InventoryReportsComponent implements OnInit {
  private apiService = inject(ApiService);

  stockLevels: StockLevel[] = [];
  lowStockItems: StockLevel[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.loadInventoryData();
  }

  private loadInventoryData(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getLowStock(7).subscribe({
      next: (data) => {
        this.lowStockItems = data;
        this.loadAllStockLevels();
      },
      error: (err) => {
        this.error = 'Failed to load inventory data. Please check if the API is running.';
        this.loading = false;
        console.error('Inventory error:', err);
      }
    });
  }

  private loadAllStockLevels(): void {
    this.apiService.getStockLevels().subscribe({
      next: (data) => {
        this.stockLevels = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Stock levels error:', err);
        this.loading = false;
      }
    });
  }
}
