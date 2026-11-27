import { vi } from 'vitest';

// The beta scripts fetch and render as soon as they load, so each test sets up the page and the
// data first, then loads a fresh copy of the script.
export async function loadPage(script, { html, url = '/', data = {} }) {
    vi.resetModules();
    document.body.innerHTML = html;
    window.history.replaceState(null, '', url);
    vi.stubGlobal('fetch', vi.fn(async requested => {
        const key = Object.keys(data).find(k => requested.endsWith(k));
        if (!key) return { ok: false, status: 404, json: async () => { throw new Error('404'); } };
        return { ok: true, status: 200, json: async () => structuredClone(data[key]) };
    }));
    vi.spyOn(console, 'error').mockImplementation(() => {});
    return import(script);
}

export const $ = selector => document.querySelector(selector);
export const $$ = selector => [...document.querySelectorAll(selector)];
