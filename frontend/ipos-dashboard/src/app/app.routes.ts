import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { 
    path: 'dashboard', 
    loadComponent: () => import('./features/dashboard/dashboard.component')
      .then(m => m.DashboardComponent)
  },
  { 
    path: 'sales', 
    loadComponent: () => import('./features/sales/sales-analytics.component')
      .then(m => m.SalesAnalyticsComponent)
  },
  { 
    path: 'inventory', 
    loadComponent: () => import('./features/inventory/inventory-reports.component')
      .then(m => m.InventoryReportsComponent)
  },
  { 
    path: 'customers', 
    loadComponent: () => import('./features/customers/customer-insights.component')
      .then(m => m.CustomerInsightsComponent)
  },
  { 
    path: 'financial', 
    loadComponent: () => import('./features/financial/financial-reports.component')
      .then(m => m.FinancialReportsComponent)
  },
  { path: '**', redirectTo: '/dashboard' }
];
