import { HttpInterceptorFn } from '@angular/common/http';
import { APP_VERSION } from './app-version';

/** Tells the backend which frontend version sent the request. */
export const clientVersionInterceptor: HttpInterceptorFn = (req, next) =>
  next(req.clone({ setHeaders: { 'Client-Version': APP_VERSION } }));
