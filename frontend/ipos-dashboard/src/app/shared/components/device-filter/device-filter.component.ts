import { Component, OnInit, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';

export interface DeviceOption {
  deviceId: string;
  deviceName: string;
}

@Component({
  selector: 'app-device-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="device-filter-container">
      <label for="device-select" class="filter-label">
        <span class="label-icon">🖥️</span>
        Filter by Device/Store:
      </label>
      <select 
        id="device-select"
        [(ngModel)]="selectedDeviceId" 
        (ngModelChange)="onDeviceChange()"
        class="device-select"
      >
        <option value="">All Devices/Stores</option>
        <option *ngFor="let device of devices" [value]="device.deviceId">
          {{ device.deviceName }} ({{ device.deviceId }})
        </option>
      </select>
    </div>
  `,
  styles: [`
    .device-filter-container {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      margin-bottom: 1.5rem;

      .filter-label {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-weight: 600;
        color: #333;
        font-size: 0.95rem;
        white-space: nowrap;

        .label-icon {
          font-size: 1.2rem;
        }
      }

      .device-select {
        flex: 1;
        padding: 0.75rem 1rem;
        border: 2px solid #e0e0e0;
        border-radius: 6px;
        font-size: 0.95rem;
        color: #333;
        background: white;
        cursor: pointer;
        transition: all 0.2s ease;
        min-width: 250px;

        &:hover {
          border-color: #1976d2;
        }

        &:focus {
          outline: none;
          border-color: #1976d2;
          box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
        }

        option {
          padding: 0.5rem;
        }
      }
    }

    @media (max-width: 768px) {
      .device-filter-container {
        flex-direction: column;
        align-items: stretch;

        .filter-label {
          justify-content: center;
        }

        .device-select {
          width: 100%;
          min-width: unset;
        }
      }
    }
  `]
})
export class DeviceFilterComponent implements OnInit {
  private apiService = inject(ApiService);

  devices: DeviceOption[] = [];
  selectedDeviceId: string = '';

  @Output() deviceChange = new EventEmitter<string>();

  ngOnInit(): void {
    this.loadDevices();
  }

  private loadDevices(): void {
    this.apiService.getDevices().subscribe({
      next: (devices) => {
        this.devices = devices;
      },
      error: (err) => {
        console.error('Failed to load devices:', err);
        // Fallback to mock data if API fails
        this.devices = [];
      }
    });
  }

  onDeviceChange(): void {
    this.deviceChange.emit(this.selectedDeviceId);
  }
}
