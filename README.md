# Ruby/Jekyll mess
Ruby is not backwards-compatible, and the version used is no longer supported.

To develop or test the site locally, you can use Github's container:
```sh
docker run --rm -p 4000:4000 -v "$PWD:/src/site" ghcr.io/github/pages-gem:latest jekyll serve --host 0.0.0.0 --force_polling
```


# Beta pages
New versions of pages live under `beta/` (HTML) and `src/beta/` (JS, built to `assets/js/beta/`), next to the classic pages, until they replace them.
Beta pages use `_layouts/beta.html` with `_includes/beta-head.html` and `beta-header.html`: no w3.css and no Font Awesome font.

Icons on beta pages come from `assets/icons.svg`. To add one, put its Font Awesome name in `scripts/build-icons.mjs` and run `npm run icons`.
