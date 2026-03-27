export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  message: string;
  tokenType: string;
  expiresIn: number;
}

export interface ErrorResponse {
  error: boolean;
  status: number;
  message: string;
}

export interface SsoCallbackResponse {
  success: boolean;
  token?: string;
  tokenType?: string;
  message: string;
  expiresIn?: number;
  errorCode?: string;
}
