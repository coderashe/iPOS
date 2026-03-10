import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { DateRange } from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class DashboardStateService {
  private dateRangeSubject = new BehaviorSubject<DateRange>({
    startDate: this.getDefaultStartDate(),
    endDate: new Date()
  });

  dateRange$: Observable<DateRange> = this.dateRangeSubject.asObservable();

  private getDefaultStartDate(): Date {
    const date = new Date();
    date.setDate(date.getDate() - 30); // Default to last 30 days
    return date;
  }

  updateDateRange(range: DateRange): void {
    this.dateRangeSubject.next(range);
  }

  getCurrentDateRange(): DateRange {
    return this.dateRangeSubject.value;
  }

  // Preset date ranges
  setToday(): void {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    this.updateDateRange({ startDate: today, endDate: tomorrow });
  }

  setYesterday(): void {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);
    const today = new Date(yesterday);
    today.setDate(today.getDate() + 1);
    
    this.updateDateRange({ startDate: yesterday, endDate: today });
  }

  setLast7Days(): void {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 7);
    
    this.updateDateRange({ startDate, endDate });
  }

  setLast30Days(): void {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 30);
    
    this.updateDateRange({ startDate, endDate });
  }

  setThisMonth(): void {
    const now = new Date();
    const startDate = new Date(now.getFullYear(), now.getMonth(), 1);
    const endDate = new Date();
    
    this.updateDateRange({ startDate, endDate });
  }

  setLastMonth(): void {
    const now = new Date();
    const startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const endDate = new Date(now.getFullYear(), now.getMonth(), 0);
    
    this.updateDateRange({ startDate, endDate });
  }
}
