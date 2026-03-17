import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component')
      .then(m => m.DashboardComponent)
  },
  {
    path: 'sales',
    canActivate: [authGuard],
    loadComponent: () => import('./features/sales/sales-analytics.component')
      .then(m => m.SalesAnalyticsComponent)
  },
  {
    path: 'inventory',
    canActivate: [authGuard],
    loadComponent: () => import('./features/inventory/inventory-reports.component')
      .then(m => m.InventoryReportsComponent)
  },
  {
    path: 'customers',
    canActivate: [authGuard],
    loadComponent: () => import('./features/customers/customer-insights.component')
      .then(m => m.CustomerInsightsComponent)
  },
  {
    path: 'financial',
    canActivate: [authGuard],
    loadComponent: () => import('./features/financial/financial-reports.component')
      .then(m => m.FinancialReportsComponent)
  },
  { path: '**', redirectTo: '/login' }
];
