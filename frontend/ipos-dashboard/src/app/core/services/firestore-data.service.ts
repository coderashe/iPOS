import { Injectable, inject } from '@angular/core';
import {
  collection,
  query,
  getDocs,
  onSnapshot,
  Timestamp
} from 'firebase/firestore';
import { FirebaseService } from './firebase.service';
import { DashboardSummary, TopProduct, StockLevel, CustomerInsights, SalesReport, SalesByCashier, RevenueTrend } from '../models/api-models';

export interface FirestoreInvoice {
  invoiceId?: number | string;
  totalAmount?: number;
  amount?: number;
  invoiceDate?: number | Timestamp | string;
  currencyCode?: string;
  currency?: string;
  status?: string;
  storeId?: string;
  deviceId?: string;
  deviceName?: string;
  items?: Array<{
    productId?: number;
    productName?: string;
    productPrice?: number;
    productQtySold?: number;
    totalAmount?: number;
  }>;
}

export interface FirestoreCurrency {
  code: string;
  name: string;
  rateToBase: number;
  storeId?: string;
}

export interface FirestoreDevice {
  deviceId: string;
  deviceName: string;
  storeId?: string;
  lastSeen?: number | string;
}

export interface FirestoreProduct {
  productId: number;
  productCode: string;
  productName: string;
  currentStock: number;
  sellingPrice: number;
  unitCost: number;
  markupPercentage?: number;
  storeId?: string;
  lastUpdated?: number;
}

@Injectable({ providedIn: 'root' })
export class FirestoreDataService {
  private fb = inject(FirebaseService);

  /**
   * Fetch and calculate dashboard summary from Firestore for a given store, date range, and device
   */
  async getDashboardSummary(storeId: string, startDate?: Date, endDate?: Date, deviceId?: string): Promise<DashboardSummary> {
    const rawInvoices = await this.getStoreInvoices(storeId, startDate, endDate);
    
    // Filter by device if selected
    const invoices = (deviceId && deviceId.trim() !== '')
      ? rawInvoices.filter(inv => inv.deviceId === deviceId || (inv as any).deviceName === deviceId)
      : rawInvoices;

    let totalRevenue = 0;
    const currencyTotals: { [curr: string]: number } = {};
    let transactionCount = invoices.length;
    const productSalesMap = new Map<string, { id: number; name: string; qty: number; revenue: number }>();

    for (const inv of invoices) {
      const amount = Number(inv.totalAmount ?? inv.amount ?? 0) || 0;
      totalRevenue += amount;
      const currency = (inv.currencyCode || 'USD').toUpperCase().trim();
      currencyTotals[currency] = (currencyTotals[currency] || 0) + amount;

      if (inv.items && Array.isArray(inv.items)) {
        for (const item of inv.items) {
          const key = item.productName || `Product-${item.productId || 'Item'}`;
          const current = productSalesMap.get(key) || {
            id: item.productId || 0,
            name: key,
            qty: 0,
            revenue: 0
          };
          const qty = Number(item.productQtySold) || 1;
          const itemRev = Number(item.totalAmount) || (Number(item.productPrice || 0) * qty);
          current.qty += qty;
          current.revenue += itemRev;
          productSalesMap.set(key, current);
        }
      }
    }

    const formattedParts: string[] = [];
    const sortedCurrencies = Object.keys(currencyTotals).sort((a, b) => {
      if (a === 'USD') return -1;
      if (b === 'USD') return 1;
      return a.localeCompare(b);
    });

    for (const curr of sortedCurrencies) {
      const total = currencyTotals[curr];
      const rounded = Math.round(total * 100) / 100;
      if (curr === 'USD') {
        formattedParts.push(`$${rounded.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      } else if (curr === 'ZAR') {
        formattedParts.push(`R${rounded.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      } else if (curr === 'ZIG') {
        formattedParts.push(`ZiG ${rounded.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      } else {
        formattedParts.push(`${curr} ${rounded.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`);
      }
    }
    const revenueFormatted = formattedParts.length > 0 ? formattedParts.join(' · ') : '$0.00';
    const primaryRevenue = currencyTotals['USD'] || (Object.values(currencyTotals)[0] || 0);

    const topProducts: TopProduct[] = Array.from(productSalesMap.values())
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 5)
      .map(p => ({
        productId: p.id,
        productName: p.name,
        quantitySold: p.qty,
        revenue: Math.round(p.revenue * 100) / 100
      }));

    const avgValue = transactionCount > 0 ? (primaryRevenue / transactionCount) : 0;

    // Get live low stock count from Firestore products
    let lowStockCount = 0;
    try {
      const products = await this.getProducts(storeId);
      lowStockCount = products.filter(p => p.currentStock <= 5).length;
    } catch {
      // ignore
    }

    return {
      totalSales: transactionCount,
      totalRevenue: Math.round(primaryRevenue * 100) / 100,
      currencyTotals: currencyTotals,
      revenueFormatted: revenueFormatted,
      transactionCount: transactionCount,
      averageTransactionValue: Math.round(avgValue * 100) / 100,
      topProducts: topProducts,
      lowStockCount: lowStockCount,
      activeCustomersCount: transactionCount,
      lastSyncTime: new Date().toISOString()
    };
  }

  /**
   * Listen to live sales updates from Firestore
   */
  subscribeToLiveSales(storeId: string, callback: (invoices: FirestoreInvoice[]) => void): () => void {
    const resolvedStore = (storeId || 'ISTART').trim();
    const colRef = collection(this.fb.firestore, 'stores', resolvedStore, 'invoices');

    return onSnapshot(colRef, (snapshot) => {
      const list: FirestoreInvoice[] = [];
      snapshot.forEach(doc => {
        list.push(doc.data() as FirestoreInvoice);
      });
      callback(list);
    }, (error) => {
      console.warn('Firestore real-time subscription notice:', error);
    });
  }

  /**
   * Get registered devices for store
   */
  async getDevices(storeId: string): Promise<Array<{ deviceId: string; deviceName: string }>> {
    const resolvedStore = (storeId || 'ISTART').trim();
    const deviceMap = new Map<string, string>();

    try {
      const colRef = collection(this.fb.firestore, 'stores', resolvedStore, 'devices');
      const snap = await getDocs(colRef);
      snap.forEach(doc => {
        const data = doc.data() as FirestoreDevice;
        if (data.deviceId) {
          deviceMap.set(data.deviceId, data.deviceName || data.deviceId);
        }
      });
    } catch (e) {
      console.warn('Could not read devices collection:', e);
    }

    // Also extract devices from synced invoices if collection is empty
    try {
      const invoices = await this.getStoreInvoices(resolvedStore);
      for (const inv of invoices) {
        if (inv.deviceId && !deviceMap.has(inv.deviceId)) {
          deviceMap.set(inv.deviceId, inv.deviceName || inv.deviceId);
        }
      }
    } catch {}

    return Array.from(deviceMap.entries()).map(([deviceId, deviceName]) => ({
      deviceId,
      deviceName
    }));
  }

  /**
   * Get products/inventory stock levels from Firestore
   */
  async getProducts(storeId: string): Promise<StockLevel[]> {
    const resolvedStore = (storeId || 'ISTART').trim();
    try {
      const colRef = collection(this.fb.firestore, 'stores', resolvedStore, 'products');
      const snap = await getDocs(colRef);
      const list: StockLevel[] = [];

      snap.forEach(doc => {
        const data = doc.data() as FirestoreProduct;
        list.push({
          productId: data.productId || Number(doc.id) || 0,
          productCode: data.productCode || `P-${doc.id}`,
          productName: data.productName || 'Unnamed Product',
          currentStock: Number(data.currentStock) || 0,
          averageDailySales: 0,
          daysRemaining: data.currentStock <= 5 ? Math.max(1, Math.round(data.currentStock / 2)) : 30
        });
      });

      return list;
    } catch (e) {
      console.warn('Could not read products from Firestore:', e);
      return [];
    }
  }

  /**
   * Get customer insights from Firestore
   */
  async getCustomerInsights(storeId: string): Promise<CustomerInsights> {
    const resolvedStore = (storeId || 'ISTART').trim();
    try {
      const colRef = collection(this.fb.firestore, 'stores', resolvedStore, 'customers');
      const snap = await getDocs(colRef);
      const customers: any[] = [];
      snap.forEach(doc => customers.push(doc.data()));

      const totalCust = customers.length;
      return {
        totalCustomers: totalCust,
        newCustomers: totalCust,
        topCustomers: customers.slice(0, 10).map(c => ({
          customerId: c.customerId || 0,
          customerName: c.name || 'Walk-in Customer',
          customerPhone: c.phone || '',
          totalSpent: 0,
          purchaseCount: 1
        })),
        averageCustomerValue: 0,
        totalReceivables: 0
      };
    } catch {
      return {
        totalCustomers: 0,
        newCustomers: 0,
        topCustomers: [],
        averageCustomerValue: 0,
        totalReceivables: 0
      };
    }
  }

  /**
   * Get supported currencies and exchange rates from Firestore
   */
  async getCurrencies(storeId: string): Promise<FirestoreCurrency[]> {
    const resolvedStore = (storeId || 'ISTART').trim();
    try {
      const colRef = collection(this.fb.firestore, 'stores', resolvedStore, 'currencies');
      const snap = await getDocs(colRef);
      const list: FirestoreCurrency[] = [];
      snap.forEach(doc => {
        const d = doc.data() as FirestoreCurrency;
        list.push({
          code: d.code || doc.id,
          name: d.name || doc.id,
          rateToBase: Number(d.rateToBase) || 1.0,
          storeId: d.storeId || resolvedStore
        });
      });
      return list;
    } catch (e) {
      console.warn('Could not read currencies from Firestore:', e);
      return [];
    }
  }

  /**
   * Get sales report and cashier breakdown directly from Firestore
   */
  async getSalesReport(storeId: string, startDate?: Date, endDate?: Date, deviceId?: string): Promise<{ report: SalesReport, cashiers: SalesByCashier[], invoices: FirestoreInvoice[] }> {
    const rawInvoices = await this.getStoreInvoices(storeId, startDate, endDate);
    const invoices = (deviceId && deviceId.trim() !== '')
      ? rawInvoices.filter(inv => inv.deviceId === deviceId || (inv as any).deviceName === deviceId)
      : rawInvoices;

    const currencyTotals: { [curr: string]: number } = {};
    const cashierMap = new Map<string, { total: number; count: number; currencies: { [c: string]: number } }>();

    for (const inv of invoices) {
      const amount = Number(inv.totalAmount ?? inv.amount ?? 0) || 0;
      const currency = (inv.currencyCode || (inv as any).currency || 'USD').toUpperCase().trim();
      currencyTotals[currency] = (currencyTotals[currency] || 0) + amount;

      const cashier = inv.deviceName || inv.deviceId || 'Main Register';
      const current = cashierMap.get(cashier) || { total: 0, count: 0, currencies: {} };
      current.total += amount;
      current.count += 1;
      current.currencies[currency] = (current.currencies[currency] || 0) + amount;
      cashierMap.set(cashier, current);
    }

    const cashiers: SalesByCashier[] = Array.from(cashierMap.entries()).map(([name, data]) => ({
      cashierName: name,
      totalSales: Math.round(data.total * 100) / 100,
      transactionCount: data.count,
      averageTransactionValue: data.count > 0 ? Math.round((data.total / data.count) * 100) / 100 : 0
    }));

    const formattedParts: string[] = [];
    for (const [curr, total] of Object.entries(currencyTotals)) {
      const rounded = Math.round(total * 100) / 100;
      if (curr === 'USD') formattedParts.push(`$${rounded.toFixed(2)}`);
      else if (curr === 'ZAR') formattedParts.push(`R${rounded.toFixed(2)}`);
      else if (curr === 'ZIG') formattedParts.push(`ZiG ${rounded.toLocaleString()}`);
      else formattedParts.push(`${curr} ${rounded.toFixed(2)}`);
    }
    const revFormatted = formattedParts.length > 0 ? formattedParts.join(' · ') : '$0.00';
    const primaryTotal = currencyTotals['USD'] || (Object.values(currencyTotals)[0] || 0);

    return {
      report: {
        dataPoints: [],
        totalRevenue: Math.round(primaryTotal * 100) / 100,
        revenueFormatted: revFormatted,
        totalTransactions: invoices.length
      },
      cashiers,
      invoices
    };
  }

  /**
   * Get revenue trends directly from Firestore
   */
  async getRevenueTrends(storeId: string, startDate?: Date, endDate?: Date, deviceId?: string): Promise<RevenueTrend> {
    const rawInvoices = await this.getStoreInvoices(storeId, startDate, endDate);
    const invoices = (deviceId && deviceId.trim() !== '')
      ? rawInvoices.filter(inv => inv.deviceId === deviceId || (inv as any).deviceName === deviceId)
      : rawInvoices;

    const dayMap = new Map<string, number>();
    for (const inv of invoices) {
      const amount = Number(inv.totalAmount ?? inv.amount ?? 0) || 0;
      let dateStr = 'Recent';
      if (typeof inv.invoiceDate === 'number') {
        dateStr = new Date(inv.invoiceDate).toLocaleDateString();
      }
      dayMap.set(dateStr, (dayMap.get(dateStr) || 0) + amount);
    }

    const dataPoints = Array.from(dayMap.entries()).map(([dateStr, value]) => ({
      date: dateStr,
      value: Math.round(value * 100) / 100
    }));

    return {
      dataPoints,
      growthRate: 0,
      trend: 'stable'
    };
  }

  private async getStoreInvoices(storeId: string, startDate?: Date, endDate?: Date): Promise<FirestoreInvoice[]> {
    const storeCandidates = [
      (storeId || 'ISTART').trim(),
      (storeId || 'ISTART').trim().toUpperCase(),
      (storeId || 'ISTART').trim().toLowerCase(),
      'ISTART'
    ];
    const uniqueStores = Array.from(new Set(storeCandidates));

    for (const store of uniqueStores) {
      try {
        const colRef = collection(this.fb.firestore, 'stores', store, 'invoices');
        const snapshot = await getDocs(colRef);

        if (!snapshot.empty) {
          const allInvoices: FirestoreInvoice[] = [];
          snapshot.forEach(doc => {
            allInvoices.push(doc.data() as FirestoreInvoice);
          });

          // If no date range specified, return all
          if (!startDate || !endDate) {
            return allInvoices;
          }

          const startMs = startDate.getTime();
          const endMs = endDate.getTime();

          const filtered = allInvoices.filter(data => {
            let dateMs = 0;
            if (typeof data.invoiceDate === 'number') {
              dateMs = data.invoiceDate;
            } else if (data.invoiceDate && typeof (data.invoiceDate as any).toMillis === 'function') {
              dateMs = (data.invoiceDate as any).toMillis();
            } else if (typeof data.invoiceDate === 'string') {
              dateMs = new Date(data.invoiceDate).getTime();
            }

            // Normalize seconds to milliseconds if needed (e.g. 10-digit unix timestamp)
            if (dateMs > 0 && dateMs < 10000000000) {
              dateMs *= 1000;
            }

            if (dateMs > 0) {
              return dateMs >= startMs && dateMs <= endMs;
            }
            return false;
          });

          return filtered;
        }
      } catch (e) {
        console.warn(`Error querying Firestore for store ${store}:`, e);
      }
    }

    return [];
  }
}
