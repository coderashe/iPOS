import { Injectable, inject } from '@angular/core';
import {
  collection,
  query,
  where,
  getDocs,
  onSnapshot,
  orderBy,
  limit,
  Timestamp
} from 'firebase/firestore';
import { FirebaseService } from './firebase.service';
import { DashboardSummary, TopProduct, SalesReport, SalesDataPoint } from '../models/api-models';

export interface FirestoreInvoice {
  invoiceId?: number | string;
  totalAmount?: number;
  invoiceDate?: number | Timestamp | string;
  currencyCode?: string;
  status?: string;
  storeId?: string;
  items?: Array<{
    productId?: number;
    productName?: string;
    productPrice?: number;
    productQtySold?: number;
    totalAmount?: number;
  }>;
}

@Injectable({ providedIn: 'root' })
export class FirestoreDataService {
  private fb = inject(FirebaseService);

  /**
   * Fetch and calculate dashboard summary from Firestore for a given store and date range
   */
  async getDashboardSummary(storeId: string, startDate: Date, endDate: Date): Promise<DashboardSummary> {
    const invoices = await this.getStoreInvoices(storeId, startDate, endDate);

    let totalRevenue = 0;
    let transactionCount = invoices.length;
    const productSalesMap = new Map<string, { id: number; name: string; qty: number; revenue: number }>();

    for (const inv of invoices) {
      const amount = Number(inv.totalAmount) || 0;
      totalRevenue += amount;

      if (inv.items && Array.isArray(inv.items)) {
        for (const item of inv.items) {
          const key = item.productName || `Prod-${item.productId || 'unknown'}`;
          const current = productSalesMap.get(key) || {
            id: item.productId || 0,
            name: key,
            qty: 0,
            revenue: 0
          };
          current.qty += Number(item.productQtySold) || 1;
          current.revenue += Number(item.totalAmount) || (Number(item.productPrice || 0) * (Number(item.productQtySold) || 1));
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

    return {
      totalSales: Math.round(totalRevenue * 100) / 100,
      totalRevenue: Math.round(totalRevenue * 100) / 100,
      transactionCount: transactionCount,
      averageTransactionValue: Math.round(avgValue * 100) / 100,
      topProducts: topProducts,
      lowStockCount: 0,
      activeCustomersCount: transactionCount,
      lastSyncTime: new Date().toISOString()
    };
  }

  /**
   * Listen to live sales updates from Firestore
   */
  subscribeToLiveSales(storeId: string, callback: (invoices: FirestoreInvoice[]) => void): () => void {
    const colRef = collection(this.fb.firestore, 'stores', storeId, 'invoices');
    const q = query(colRef, orderBy('invoiceDate', 'desc'), limit(50));

    return onSnapshot(q, (snapshot) => {
      const list: FirestoreInvoice[] = [];
      snapshot.forEach(doc => {
        list.push(doc.data() as FirestoreInvoice);
      });
      callback(list);
    }, (error) => {
      console.warn('Firestore real-time subscription error:', error);
    });
  }

  private async getStoreInvoices(storeId: string, startDate: Date, endDate: Date): Promise<FirestoreInvoice[]> {
    try {
      const colRef = collection(this.fb.firestore, 'stores', storeId, 'invoices');
      const startMs = startDate.getTime();
      const endMs = endDate.getTime();

      // Query invoices
      const q = query(colRef);
      const snapshot = await getDocs(q);

      const invoices: FirestoreInvoice[] = [];
      snapshot.forEach(doc => {
        const data = doc.data() as FirestoreInvoice;
        let dateMs = 0;
        if (typeof data.invoiceDate === 'number') {
          dateMs = data.invoiceDate;
        } else if (data.invoiceDate && typeof (data.invoiceDate as any).toMillis === 'function') {
          dateMs = (data.invoiceDate as any).toMillis();
        } else if (typeof data.invoiceDate === 'string') {
          dateMs = new Date(data.invoiceDate).getTime();
        }

        if (dateMs >= startMs && dateMs <= endMs) {
          invoices.push(data);
        } else if (!data.invoiceDate) {
          // If no date field, include it
          invoices.push(data);
        }
      });

      return invoices;
    } catch (e) {
      console.error('Error reading invoices from Firestore:', e);
      return [];
    }
  }
}
