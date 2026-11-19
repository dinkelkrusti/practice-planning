import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { APP_VERSION } from './app-version';
import { clientVersionInterceptor } from './client-version.interceptor';

describe('clientVersionInterceptor', () => {
  it('adds the Client-Version header to every request', () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([clientVersionInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    TestBed.inject(HttpClient).get('/api/v1/pieces').subscribe();

    const request = TestBed.inject(HttpTestingController).expectOne('/api/v1/pieces');
    expect(request.request.headers.get('Client-Version')).toBe(APP_VERSION);
  });
});
