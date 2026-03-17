import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, AuthUser } from '../models/auth.models';

const TOKEN_KEY = 'ipos_auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private baseUrl = environment.apiUrl;

  login(req: LoginRequest): Observable<void> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, req).pipe(
      map(res => {
        localStorage.setItem(TOKEN_KEY, res.token);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
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
    return this.getUser()?.storeId ?? null;
  }
}
