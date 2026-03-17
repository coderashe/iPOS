export interface LoginRequest {
  email: string;
  password: string;
  storeId: string;
}

export interface LoginResponse {
  token: string;
}

export interface AuthUser {
  sub: string;
  storeId: string;
  role: string;
  exp: number;
}
