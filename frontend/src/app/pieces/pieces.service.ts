import { Injectable, inject, resource } from '@angular/core';
import { Api } from '../api/generated/api';
import { createPiece } from '../api/generated/fn/pieces/create-piece';
import { listPieces } from '../api/generated/fn/pieces/list-pieces';
import { Piece } from '../api/generated/models/piece';

/**
 * The only place in the app that talks to the generated client for pieces.
 * Components use this, never the generated code, so a generator swap or an API
 * change stays local to this file (docs/decisions/0008-angular-client-generator.md).
 */
@Injectable({ providedIn: 'root' })
export class PiecesService {
  private readonly api = inject(Api);

  /** All pieces, newest first, as a signal-based resource. */
  readonly pieces = resource({
    loader: async (): Promise<Piece[]> => (await this.api.invoke(listPieces)).items,
  });

  async create(title: string, composer: string | null): Promise<Piece> {
    const piece = await this.api.invoke(createPiece, { body: { title, composer } });
    this.pieces.reload();
    return piece;
  }
}
