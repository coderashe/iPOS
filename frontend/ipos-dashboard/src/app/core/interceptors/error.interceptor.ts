import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An error occurred';
      
      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        
        // Handle specific error codes
        switch (error.status) {
          case 401:
            errorMessage = 'Unauthorized. Please log in.';
            break;
          case 403:
            errorMessage = 'Access forbidden.';
            break;
          case 404:
            errorMessage = 'Resource not found.';
            break;
          case 500:
            errorMessage = 'Internal server error. Please try again later.';
            break;
        }
      }
      
      // Log error to console for debugging
      console.error('HTTP Error:', errorMessage, error);
      
      // You can add a notification service here to show user-friendly errors
      // this.notificationService.showError(errorMessage);
      
      return throwError(() => new Error(errorMessage));
    })
  );
};
