import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./pieces/pieces-page').then((m) => m.PiecesPage) },
];
