import { afterEach, describe, expect, it, vi } from 'vitest';
import { $, $$, loadPage } from './helpers.js';

const NOW = new Date('2026-11-27T12:00:00Z');
const day = 86400;

const bot = (botName, extra = {}) => ({
    botName, race: 'PROTOSS', rank: 'A', previousRank: 'A', rating: 3000, won: 60, lost: 40,
    wonInWindow: 6, lostInWindow: 4, crashed: 0, crashesSinceUpdate: 0, enabled: true,
    lastUpdated: NOW.getTime() / 1000 - 30 * day, ...extra,
});

async function showRanking(bots, url = '/beta/ranking.html') {
    vi.useFakeTimers({ toFake: ['Date'] });
    vi.setSystemTime(NOW);
    Element.prototype.scrollIntoView = () => {};
    await loadPage('../src/beta/ranking.js', {
        html: '<div id="rankingTable"></div><small id="srcRef"></small>',
        url,
        data: { 'stats/ranking.json': bots },
    });
    await expect.poll(() => $$('table.ranking:not(.disabled) tbody tr').length).toBeGreaterThan(0);
}

const updatedOf = name => $(`tr[id="${name}"] td.updated`).textContent.trim();

afterEach(() => vi.useRealTimers());

describe('ranking', () => {
    it('shows "–" for an unknown update date instead of crashing', async () => {
        // Regression: Java's Instant.MIN broke the whole page.
        await showRanking([bot('Alpha', { lastUpdated: -31557014167219200 })]);
        expect(updatedOf('Alpha')).toBe('–');
    });

    it('shows the time for recent updates and the full date with year otherwise', async () => {
        await showRanking([
            bot('Recent', { lastUpdated: NOW.getTime() / 1000 - 2 * day }),
            bot('Old', { lastUpdated: Date.UTC(2021, 11, 22) / 1000 }),
            bot('Off', { enabled: false, lastUpdated: NOW.getTime() / 1000 - day }),
        ]);
        expect(updatedOf('Recent')).toMatch(/\d:\d\d/);
        expect(updatedOf('Recent')).not.toMatch(/2026/);
        expect(updatedOf('Old')).toMatch(/2021/);
        expect(updatedOf('Old')).not.toMatch(/\d:\d\d/);
    });

    it('keeps the linked bot highlighted after sorting', async () => {
        // Regression: the highlight stayed on the row position instead of following the bot.
        await showRanking([bot('Alpha', { rating: 3100 }), bot('Bravo', { rating: 2900 }), bot('Charlie', { rating: 3000 })],
            '/beta/ranking.html#Bravo');
        expect($('tr.linked').id).toBe('Bravo');
        $$('th button').find(b => b.textContent.includes('ELO')).click();
        await expect.poll(() => $('tbody tr').id).toBe('Alpha');
        expect($$('tr.linked').map(tr => tr.id)).toEqual(['Bravo']);
    });

    it('ignores a malformed link instead of failing', async () => {
        await showRanking([bot('Alpha')], '/beta/ranking.html#%E0%A4%A');
        expect($$('tr.linked')).toHaveLength(0);
        expect($('tr[id="Alpha"]')).not.toBeNull();
    });

    it('puts disabled bots with games into the collapsed list', async () => {
        await showRanking([bot('Alpha'), bot('Gone', { enabled: false, disabledReason: 'Crashes' }), bot('Empty', { enabled: false, won: 0, lost: 0 })]);
        expect($$('table.disabled tbody tr').map(tr => tr.id)).toEqual(['Gone']);
        expect($('details.disabled-bots summary').textContent).toContain('(1)');
    });
});
