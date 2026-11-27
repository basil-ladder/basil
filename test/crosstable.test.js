import { beforeEach, describe, expect, it, vi } from 'vitest';
import { $, $$, loadPage } from './helpers.js';

// vsBotIdxWon[j] is how often this bot beat bot j.
const data = {
    botinfos: [
        { name: 'Alpha', race: 'PROTOSS', rating: 3000, enabled: true, vsBotIdxWon: [0, 12, 7] },
        { name: 'Bravo', race: 'TERRAN', rating: 3100, enabled: true, vsBotIdxWon: [5, 0, 1] },
        { name: 'Charlie', race: 'ZERG', rating: 2800, enabled: false, vsBotIdxWon: [3, 9, 0] },
    ],
};

async function showCrosstable(url = '/beta/crosstable.html') {
    await loadPage('../src/beta/crosstable.js', {
        html: '<div id="crosstableControls"></div><div id="crosstable"></div>',
        url,
        data: { 'stats/botVsBot.json': data, 'stats/botVsBot_30.json': data },
    });
    await expect.poll(() => $$('#crosstable tbody tr').length).toBeGreaterThan(0);
}

const rowNames = () => $$('#crosstable th.rowhead a').map(a => a.textContent);

beforeEach(() => {
    window.matchMedia = () => ({ matches: true });
});

describe('crosstable', () => {
    it('shows enabled bots by ELO with wins and losses from the row bot', async () => {
        await showCrosstable();
        expect(rowNames()).toEqual(['Bravo', 'Alpha']);
        // Bravo's row, Alpha's column: Bravo beat Alpha 5 times and lost 12 times.
        expect($$('#crosstable tbody tr')[0].cells[3].textContent).toBe('5–12');
    });

    it('names the right two bots in the tooltip of the hovered cell', async () => {
        // Regression guard: the ELO column shifted the column index by one.
        await showCrosstable();
        const cell = $$('#crosstable tbody tr')[1].cells[2];
        cell.dispatchEvent(new MouseEvent('mouseover', { bubbles: true }));
        expect(cell.title).toBe('Alpha vs Bravo: 12–5');
        const marked = $$('#crosstable th.hl');
        expect(marked.find(th => th.classList.contains('rowhead')).textContent).toContain('Alpha');
        expect(marked.find(th => th.classList.contains('colhead')).textContent).toBe('Bravo');
    });

    it('shows only the bots from the link, disabled ones included, and their range', async () => {
        await showCrosstable('/beta/crosstable.html?bot=Charlie&bot=Alpha&bot=Unknown&range=30');
        expect(rowNames()).toEqual(['Alpha', 'Charlie']);
        expect(fetch).toHaveBeenCalledWith(expect.stringMatching(/botVsBot_30\.json$/));
        expect($('.presets .active').textContent).toBe('30 days');
        expect($('#crosstableControls input[type="checkbox"]')).toBeNull();
        expect($('#crosstableControls a').getAttribute('href')).toBe('/beta/crosstable.html');
    });

    it('escapes bot names', async () => {
        data.botinfos[0].name = '<b>Alpha</b>';
        try {
            await showCrosstable();
            expect($('#crosstable b')).toBeNull();
            expect(rowNames()).toContain('<b>Alpha</b>');
        } finally {
            data.botinfos[0].name = 'Alpha';
        }
    });
});
