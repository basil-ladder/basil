import { describe, expect, it } from 'vitest';
import { $, $$, loadPage } from './helpers.js';

const bots = [
    { name: 'Alpha', race: 'PROTOSS', rank: 'A' },
    { name: 'Bravo', race: 'TERRAN', rank: 'B' },
    { name: 'Charlie', race: 'RANDOM', rank: 'C' },
];

const game = (i, a, b, extra = {}) => ({
    botA: { botIndex: a, winner: false, crashed: false },
    botB: { botIndex: b, winner: true, crashed: false },
    mapIndex: 0,
    gameHash: 'hash' + i,
    endedAt: 1700000000 + i * 60,
    frameCount: 24 * 125,
    ...extra,
});

async function showGames(results) {
    const { default: renderGameListing } = await loadPage('../src/beta/gamelisting.js', {
        html: '<div id="gamesTable"></div>',
        data: { 'stats/games_24h.json': { results, maps: ['Benzene'], bots } },
    });
    renderGameListing();
    await expect.poll(() => $$('table.games tbody tr').length).toBeGreaterThan(0);
}

const visibleRows = () => $$('table.games tbody tr').filter(tr => !tr.hidden);

describe('game list', () => {
    it('lists the winner first, in bold, with the game time as m:ss', async () => {
        await showGames([game(1, 0, 1)]);
        const [first, second] = $$('td.bot .name');
        expect(first.textContent).toBe('Bravo');
        expect(first.classList.contains('winner')).toBe(true);
        expect(second.classList.contains('winner')).toBe(false);
        expect($('td.time').textContent).toContain('2:05');
    });

    it("links the other bot's replay when the winner crashed", async () => {
        const crashed = game(1, 0, 1);
        crashed.botB.crashed = true;
        await showGames([crashed]);
        const download = $('td.actions a[title="Download replay"]');
        expect(decodeURIComponent(download.getAttribute('href'))).toContain('bots/Alpha/Alpha vs Bravo Benzene hash1.rep');
    });

    it("marks a random bot with a dice and the race it played", async () => {
        const g = game(1, 2, 0);
        g.botA.race = 'ZERG';
        await showGames([g]);
        const cell = $$('td.bot').find(td => td.textContent.includes('Charlie'));
        expect(cell.classList.contains('race_zerg')).toBe(true);
        expect(cell.querySelector('.race-dice').getAttribute('title')).toBe('Random race, played Zerg');
    });

    it('shows 100 games until asked for all', async () => {
        await showGames(Array.from({ length: 120 }, (_, i) => game(i, 0, 1)));
        expect(visibleRows()).toHaveLength(100);
        $('button.show-all').click();
        await expect.poll(() => visibleRows().length).toBe(120);
    });

    it('keeps the stripes alternating on the rows left after a search', async () => {
        // Regression: striping by position counted hidden rows, so neighbours could share a colour.
        await showGames([game(1, 0, 1), game(2, 0, 2), game(3, 1, 2), game(4, 0, 2), game(5, 0, 1)]);
        const search = $('.game-filters input');
        search.value = 'Charlie';
        search.dispatchEvent(new Event('input'));
        await expect.poll(() => visibleRows().length).toBe(3);
        expect(visibleRows().map(tr => tr.classList.contains('alt'))).toEqual([false, true, false]);
    });
});
