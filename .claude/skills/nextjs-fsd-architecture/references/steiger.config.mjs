import fsd from '@feature-sliced/steiger-plugin';
import { defineConfig } from 'steiger';

// Steiger config for the official FSD layout in Next.js (`src/_app`, `src/_pages`).
// The same file works for a single app (`src/`) and for a Turborepo checked from the root (`apps/*/src`, `packages/core/src`).
export default defineConfig([
  ...fsd.configs.recommended,
  {
    // Underscored layer folders are recognized as layers, but the typo rule compares raw folder names.
    files: ['./src/_app/**', './src/_pages/**', './apps/*/src/_app/**', './apps/*/src/_pages/**'],
    rules: {
      'fsd/typo-in-layer-name': 'off',
    },
  },
  {
    // `providers` is the conventional segment name in the app layer.
    files: ['./src/_app/providers/**', './apps/*/src/_app/providers/**'],
    rules: {
      'fsd/segments-by-purpose': 'off',
    },
  },
  {
    // Apps consume core slices through package exports, which are outside core's source root,
    // so Steiger would report every promoted slice as unused.
    files: [
      './packages/core/src/entities/**',
      './packages/core/src/features/**',
      './packages/core/src/widgets/**',
    ],
    rules: {
      'fsd/insignificant-slice': 'off',
    },
  },
]);
