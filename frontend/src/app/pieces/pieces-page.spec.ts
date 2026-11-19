import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PiecesPage } from './pieces-page';

describe('PiecesPage', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PiecesPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  it('lists the pieces returned by the API', async () => {
    const fixture = TestBed.createComponent(PiecesPage);
    fixture.detectChanges();
    // Let the resource loader start its request (it runs asynchronously).
    await new Promise((resolve) => setTimeout(resolve));

    http.expectOne('/api/v1/pieces').flush({
      items: [
        {
          id: '0195f3a0-0000-7000-8000-000000000001',
          title: 'Ballade No. 1',
          composer: 'Frédéric Chopin',
          createdAt: '2026-11-19T12:00:00Z',
        },
      ],
    });
    await fixture.whenStable();

    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Ballade No. 1');
    expect(text).toContain('by Frédéric Chopin');
  });
});
