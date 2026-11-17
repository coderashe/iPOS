import { Injectable, inject } from '@angular/core';
import {
  collection,
  query,
  getDocs,
  onSnapshot,
  Timestamp
} from 'firebase/firestore';
import { FirebaseService } from './firebase.service';
import { DashboardSummary, TopProduct, StockLevel, CustomerInsights } from '../models/api-models';

export interface FirestoreInvoice {
  invoiceId?: number | string;
  totalAmount?: number;
  amount?: number;
  invoiceDate?: number | Timestamp | string;
  currencyCode?: string;
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
    let transactionCount = invoices.length;
    const productSalesMap = new Map<string, { id: number; name: string; qty: number; revenue: number }>();

    for (const inv of invoices) {
      const amount = Number(inv.totalAmount ?? inv.amount ?? 0) || 0;
      totalRevenue += amount;

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

    const topProducts: TopProduct[] = Array.from(productSalesMap.values())
      .sort((a, b) => b.revenue - a.revenue)
      .slice(0, 5)
      .map(p => ({
        productId: p.id,
        productName: p.name,
        quantitySold: p.qty,
        revenue: Math.round(p.revenue * 100) / 100
      }));

    const avgValue = transactionCount > 0 ? totalRevenue / transactionCount : 0;

    // Get live low stock count from Firestore products
    let lowStockCount = 0;
    try {
      const products = await this.getProducts(storeId);
      lowStockCount = products.filter(p => p.currentStock <= 5).length;
    } catch {
      // ignore
    }

    return {
      totalSales: Math.round(totalRevenue * 100) / 100,
      totalRevenue: Math.round(totalRevenue * 100) / 100,
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

            if (dateMs >= startMs && dateMs <= endMs) {
              return true;
            }
            // If date is invalid or missing, still include it
            return !dateMs;
          });

          // If filtered matched invoices, return them; otherwise return all to ensure data is visible
          return filtered.length > 0 ? filtered : allInvoices;
        }
      } catch (e) {
        console.warn(`Error querying Firestore for store ${store}:`, e);
      }
    }

    return [];
  }
}
