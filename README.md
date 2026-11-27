# Ruby/Jekyll mess
Ruby is not backwards-compatible, and the version used is no longer supported.

To develop or test the site locally, run `./serve`. It uses GitHub's Pages container, serves http://localhost:4000 and
rebuilds when a file changes. Scripts in `src/` need `npm run build` (or `npm run dev`) as well.


# Beta pages
New versions of pages live under `beta/` (HTML) and `src/beta/` (JS, built to `assets/js/beta/`), next to the classic pages, until they replace them.
Beta pages use `_layouts/beta.html` with `_includes/beta-head.html` and `beta-header.html`: no w3.css and no Font Awesome font.

Icons on beta pages come from `assets/icons.svg`. To add one, put its Font Awesome name in `scripts/build-icons.mjs` and run `npm run icons`.

Tests for the beta scripts are in `test/` and run with `npm test` (Vitest with a simulated browser page).
Most tests load a whole beta script with sample data, because the scripts render as soon as they load.
