import { Component, Output, EventEmitter, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DateRange } from '../../../core/models/api-models';
import { DashboardStateService } from '../../../core/services/dashboard-state.service';

@Component({
  selector: 'app-date-range-picker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="date-range-picker">
      <div class="preset-buttons">
        <button (click)="setToday()" [class.active]="selectedPreset === 'today'">Today</button>
        <button (click)="setYesterday()" [class.active]="selectedPreset === 'yesterday'">Yesterday</button>
        <button (click)="setLast7Days()" [class.active]="selectedPreset === 'last7days'">Last 7 Days</button>
        <button (click)="setLast30Days()" [class.active]="selectedPreset === 'last30days'">Last 30 Days</button>
        <button (click)="setThisMonth()" [class.active]="selectedPreset === 'thismonth'">This Month</button>
        <button (click)="setLastMonth()" [class.active]="selectedPreset === 'lastmonth'">Last Month</button>
        <button (click)="setCustom()" [class.active]="selectedPreset === 'custom'">Custom</button>
      </div>
      
      <div class="date-inputs" *ngIf="selectedPreset === 'custom'">
        <div class="input-group">
          <label for="startDate">Start Date:</label>
          <input 
            type="date" 
            id="startDate"
            [(ngModel)]="startDateStr" 
            (change)="onDateChange()"
          />
        </div>
        <div class="input-group">
          <label for="endDate">End Date:</label>
          <input 
            type="date" 
            id="endDate"
            [(ngModel)]="endDateStr" 
            (change)="onDateChange()"
          />
        </div>
      </div>
    </div>
  `,
  styles: [`
    .date-range-picker {
      padding: 1rem;
      background: #f5f5f5;
      border-radius: 8px;
      margin-bottom: 1.5rem;
    }

    .preset-buttons {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      margin-bottom: 1rem;
    }

    .preset-buttons button {
      padding: 0.5rem 1rem;
      border: 1px solid #ddd;
      background: white;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;
    }

    .preset-buttons button:hover {
      background: #e0e0e0;
    }

    .preset-buttons button.active {
      background: #3f51b5;
      color: white;
      border-color: #3f51b5;
    }

    .date-inputs {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .input-group {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .input-group label {
      font-size: 0.875rem;
      font-weight: 500;
      color: #666;
    }

    .input-group input {
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 0.875rem;
    }

    @media (max-width: 768px) {
      .preset-buttons {
        flex-direction: column;
      }

      .preset-buttons button {
        width: 100%;
      }

      .date-inputs {
        flex-direction: column;
      }
    }
  `]
})
export class DateRangePickerComponent implements OnInit {
  @Output() rangeChange = new EventEmitter<DateRange>();
  
  private stateService = inject(DashboardStateService);
  
  selectedPreset: string = 'last30days';
  startDateStr: string = '';
  endDateStr: string = '';

  ngOnInit(): void {
    // Initialize with last 30 days
    this.setLast30Days();
  }

  setToday(): void {
    this.selectedPreset = 'today';
    this.stateService.setToday();
    this.emitChange();
  }

  setYesterday(): void {
    this.selectedPreset = 'yesterday';
    this.stateService.setYesterday();
    this.emitChange();
  }

  setLast7Days(): void {
    this.selectedPreset = 'last7days';
    this.stateService.setLast7Days();
    this.emitChange();
  }

  setLast30Days(): void {
    this.selectedPreset = 'last30days';
    this.stateService.setLast30Days();
    this.emitChange();
  }

  setThisMonth(): void {
    this.selectedPreset = 'thismonth';
    this.stateService.setThisMonth();
    this.emitChange();
  }

  setLastMonth(): void {
    this.selectedPreset = 'lastmonth';
    this.stateService.setLastMonth();
    this.emitChange();
  }

  setCustom(): void {
    this.selectedPreset = 'custom';
    const range = this.stateService.getCurrentDateRange();
    this.startDateStr = this.formatDateForInput(range.startDate);
    this.endDateStr = this.formatDateForInput(range.endDate);
  }

  onDateChange(): void {
    if (this.startDateStr && this.endDateStr) {
      const range: DateRange = {
        startDate: new Date(this.startDateStr),
        endDate: new Date(this.endDateStr)
      };
      this.stateService.updateDateRange(range);
      this.emitChange();
    }
  }

  private emitChange(): void {
    const range = this.stateService.getCurrentDateRange();
    this.rangeChange.emit(range);
  }

  private formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
