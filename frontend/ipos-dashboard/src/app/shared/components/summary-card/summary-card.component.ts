import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-summary-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="summary-card">
      <div class="card-header">
        <span class="icon" *ngIf="icon">{{ icon }}</span>
        <h3 class="title">{{ title }}</h3>
      </div>
      <div class="card-body">
        <div class="value">{{ formattedValue }}</div>
        <div class="trend" *ngIf="trend" [class.positive]="trend > 0" [class.negative]="trend < 0">
          <span class="trend-icon">{{ trend > 0 ? '↑' : '↓' }}</span>
          <span class="trend-value">{{ Math.abs(trend) }}%</span>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .summary-card {
      background: white;
      border-radius: 8px;
      padding: 1.5rem;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .summary-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .icon {
      font-size: 1.5rem;
    }

    .title {
      font-size: 0.875rem;
      font-weight: 500;
      color: #666;
      margin: 0;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    .card-body {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
    }

    .value {
      font-size: 2rem;
      font-weight: 700;
      color: #333;
    }

    .trend {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.875rem;
      font-weight: 600;
    }

    .trend.positive {
      color: #4caf50;
    }

    .trend.negative {
      color: #f44336;
    }

    .trend-icon {
      font-size: 1.25rem;
    }

    @media (max-width: 768px) {
      .summary-card {
        padding: 1rem;
      }

      .value {
        font-size: 1.5rem;
      }
    }
  `]
})
export class SummaryCardComponent {
  @Input() title: string = '';
  @Input() value: number | string = 0;
  @Input() icon?: string;
  @Input() trend?: number;
  @Input() format: 'number' | 'currency' | 'percent' = 'number';

  Math = Math;

  get formattedValue(): string {
    if (typeof this.value === 'string') {
      return this.value;
    }

    switch (this.format) {
      case 'currency':
        return new Intl.NumberFormat('en-US', {
          style: 'currency',
          currency: 'USD'
        }).format(this.value);
      case 'percent':
        return `${this.value}%`;
      default:
        return new Intl.NumberFormat('en-US').format(this.value);
    }
  }
}
