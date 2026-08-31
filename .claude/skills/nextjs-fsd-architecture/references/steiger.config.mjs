import { defineConfig } from "steiger";

import fsd from "@feature-sliced/steiger-plugin";

export default defineConfig([
  ...fsd.configs.recommended,
  {
    files: ["src/app/providers.tsx"],
    rules: {
      "fsd/segments-by-purpose": "off",
    },
  },
  {
    files: ["src/{entities,features,widgets}/**/*.{ts,tsx}"],
    rules: {
      // `views` is a project extension, so Steiger cannot trace its references.
      "fsd/insignificant-slice": "off",
    },
  },
  {
    files: ["apps/*/src/app/providers.tsx"],
    rules: {
      "fsd/segments-by-purpose": "off",
    },
  },
  {
    files: ["apps/*/src/{entities,features,widgets}/**/*.{ts,tsx}"],
    rules: {
      "fsd/insignificant-slice": "off",
    },
  },
  {
    files: ["packages/core/src/{entities,shared}/index*.ts"],
    rules: {
      "fsd/no-layer-public-api": "off",
    },
  },
  {
    files: ["packages/core/src/entities/**/*.{ts,tsx}"],
    rules: {
      "fsd/insignificant-slice": "off",
    },
  },
]);
