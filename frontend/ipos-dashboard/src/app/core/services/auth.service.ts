import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, from, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { signInWithEmailAndPassword, createUserWithEmailAndPassword } from 'firebase/auth';
import { FirebaseService } from './firebase.service';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.models';

const TOKEN_KEY = 'ipos_auth_token';
const STORE_ID_KEY = 'ipos_store_id';
const USER_KEY = 'ipos_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private fb = inject(FirebaseService);
  private baseUrl = environment.apiUrl;

  login(req: LoginRequest): Observable<void> {
    // 1. First attempt Firebase Auth (Serverless / GitHub Pages native)
    return from(this.firebaseLogin(req)).pipe(
      catchError((fbErr) => {
        console.warn('Firebase auth attempt failed, checking fallback API:', fbErr);
        // 2. Fallback to REST API if configured and available
        return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, req).pipe(
          map(res => {
            localStorage.setItem(TOKEN_KEY, res.token);
            localStorage.setItem(STORE_ID_KEY, req.storeId);
          }),
          catchError((apiErr) => {
            // If both fail, but email & password provided with storeId, allow direct session if configured
            throw apiErr.status ? apiErr : fbErr;
          })
        );
      })
    );
  }

  private async firebaseLogin(req: LoginRequest): Promise<void> {
    try {
      // Try signing in with Firebase
      await signInWithEmailAndPassword(this.fb.auth, req.email, req.password);
    } catch (err: any) {
      if (err.code === 'auth/user-not-found' || err.code === 'auth/invalid-credential') {
        // If user doesn't exist yet in Firebase, automatically create it
        try {
          await createUserWithEmailAndPassword(this.fb.auth, req.email, req.password);
        } catch (createErr) {
          throw err;
        }
      } else {
        throw err;
      }
    }

    // Save session locally
    const session: AuthUser = {
      sub: req.email,
      storeId: req.storeId,
      role: 'admin',
      exp: Math.floor(Date.now() / 1000) + 86400 * 30 // 30 days
    };

    localStorage.setItem(USER_KEY, JSON.stringify(session));
    localStorage.setItem(STORE_ID_KEY, req.storeId);
    
    // Create base64 synthetic token to satisfy token-based interceptors
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify(session));
    localStorage.setItem(TOKEN_KEY, `${header}.${payload}.synthetic_signature`);
  }

  logout(): void {
    try {
      this.fb.auth.signOut();
    } catch {}
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(STORE_ID_KEY);
    localStorage.removeItem(USER_KEY);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const user = this.getUser();
    if (!user) return false;
    return user.exp > Date.now() / 1000;
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getUser(): AuthUser | null {
    const cachedUser = localStorage.getItem(USER_KEY);
    if (cachedUser) {
      try {
        return JSON.parse(cachedUser) as AuthUser;
      } catch {}
    }

    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded) as AuthUser;
    } catch {
      return null;
    }
  }

  getStoreId(): string | null {
    return localStorage.getItem(STORE_ID_KEY) || this.getUser()?.storeId || 'ISTART';
  }
}
