import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PiecesService } from './pieces.service';

@Component({
  selector: 'app-pieces-page',
  imports: [ReactiveFormsModule],
  templateUrl: './pieces-page.html',
  styleUrl: './pieces-page.css',
})
export class PiecesPage {
  protected readonly service = inject(PiecesService);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    composer: ['', [Validators.maxLength(200)]],
  });

  protected async add(): Promise<void> {
    if (this.form.invalid) {
      return;
    }
    const { title, composer } = this.form.getRawValue();
    this.saving.set(true);
    this.error.set(null);
    try {
      await this.service.create(title, composer.trim() === '' ? null : composer);
      this.form.reset();
    } catch {
      this.error.set('Could not save the piece. Is the backend running?');
    } finally {
      this.saving.set(false);
    }
  }
}
