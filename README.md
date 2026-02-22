# Ruby/Jekyll mess
Ruby is not backwards-compatible, and the version used is no longer supported.

To develop or test the site locally, you can use Github's container:
```sh
docker run --rm -p 4000:4000 -v "$PWD:/src/site" ghcr.io/github/pages-gem:latest jekyll serve --host 0.0.0.0 --force_polling
```

