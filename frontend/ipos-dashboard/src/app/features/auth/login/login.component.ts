import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>iPOS Dashboard</h1>
        <h2>Sign In</h2>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="field">
            <label for="email">Email</label>
            <input
              id="email"
              type="email"
              formControlName="email"
              placeholder="admin@store.com"
              autocomplete="email"
            />
            <span class="error" *ngIf="form.get('email')?.invalid && form.get('email')?.touched">
              Email is required
            </span>
          </div>

          <div class="field">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              formControlName="password"
              placeholder="••••••••"
              autocomplete="current-password"
            />
            <span class="error" *ngIf="form.get('password')?.invalid && form.get('password')?.touched">
              Password is required
            </span>
          </div>

          <div class="field">
            <label for="storeId">Store ID</label>
            <input
              id="storeId"
              type="text"
              formControlName="storeId"
              placeholder="STORE-001"
            />
            <span class="error" *ngIf="form.get('storeId')?.invalid && form.get('storeId')?.touched">
              Store ID is required
            </span>
          </div>

          <div class="error-banner" *ngIf="errorMessage">
            {{ errorMessage }}
          </div>

          <button type="submit" [disabled]="loading">
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: #f5f5f5;
    }
    .login-card {
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 2px 12px rgba(0,0,0,0.1);
      width: 100%;
      max-width: 400px;
    }
    h1 { margin: 0 0 0.25rem; font-size: 1.5rem; color: #333; }
    h2 { margin: 0 0 1.5rem; font-size: 1rem; color: #666; font-weight: 400; }
    .field { display: flex; flex-direction: column; margin-bottom: 1rem; }
    label { font-size: 0.875rem; font-weight: 500; margin-bottom: 0.25rem; color: #444; }
    input {
      padding: 0.5rem 0.75rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 1rem;
      outline: none;
    }
    input:focus { border-color: #4a90e2; box-shadow: 0 0 0 2px rgba(74,144,226,0.2); }
    .error { font-size: 0.75rem; color: #e53e3e; margin-top: 0.25rem; }
    .error-banner {
      background: #fff5f5;
      border: 1px solid #fed7d7;
      color: #c53030;
      padding: 0.75rem;
      border-radius: 4px;
      margin-bottom: 1rem;
      font-size: 0.875rem;
    }
    button {
      width: 100%;
      padding: 0.625rem;
      background: #4a90e2;
      color: white;
      border: none;
      border-radius: 4px;
      font-size: 1rem;
      cursor: pointer;
    }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    button:hover:not(:disabled) { background: #357abd; }
  `]
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
    storeId: ['', Validators.required]
  });

  errorMessage = '';
  loading = false;
  private returnUrl = '/dashboard';

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard';
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { email, password, storeId } = this.form.value;

    this.auth.login({ email: email!, password: password!, storeId: storeId! }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (err) => {
        this.loading = false;
        if (err?.status === 401 || err?.message?.includes('401')) {
          this.errorMessage = 'Invalid credentials or access denied';
        } else {
          this.errorMessage = 'Unable to connect. Please try again.';
        }
      }
    });
  }
}
