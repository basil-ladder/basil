import { describe, expect, it } from 'vitest';
import { formatMinutes, prepareRankingBot, sortByRank } from '../src/beta/common.js';

describe('formatMinutes', () => {
    it('writes minutes and two-digit seconds', () => {
        expect(formatMinutes(0)).toBe('0:00');
        expect(formatMinutes(65)).toBe('1:05');
        expect(formatMinutes(3599.6)).toBe('60:00');
    });
});

describe('prepareRankingBot', () => {
    it('treats Java Instant.MIN as an unknown date', () => {
        // Regression: this value crashed the ranking page when it was turned into a Date.
        const bot = prepareRankingBot({ rank: 'A', lastUpdated: -31557014167219200, wonInWindow: 0, lostInWindow: 0 });
        expect(bot.lastUpdated).toBeNull();
    });

    it('shortens UNRANKED and computes the 7-day win rate', () => {
        const bot = prepareRankingBot({ rank: 'UNRANKED', previousRank: 'UNRANKED', lastUpdated: 1700000000, wonInWindow: 3, lostInWindow: 1 });
        expect(bot.rank).toBe('U');
        expect(bot.previousRank).toBe('U');
        expect(bot.win7).toBe(0.75);
        expect(bot.lastUpdated).toEqual(new Date(1700000000 * 1000));
    });

    it('has no 7-day win rate without games', () => {
        expect(prepareRankingBot({ rank: 'A', wonInWindow: 0, lostInWindow: 0 }).win7).toBeNull();
    });
});

describe('sortByRank', () => {
    it('puts enabled bots first, then bots with 30+ games, then rank and ELO', () => {
        const bots = [
            { botName: 'disabled', enabled: false, rank: 'S', rating: 3500, won: 100, lost: 0 },
            { botName: 'new', enabled: true, rank: 'S', rating: 3400, won: 5, lost: 5 },
            { botName: 'b-high', enabled: true, rank: 'B', rating: 2900, won: 50, lost: 50 },
            { botName: 'a-low', enabled: true, rank: 'A', rating: 2800, won: 50, lost: 50 },
            { botName: 'a-high', enabled: true, rank: 'A', rating: 3000, won: 50, lost: 50 },
        ];
        sortByRank(bots);
        expect(bots.map(b => b.botName)).toEqual(['a-high', 'a-low', 'b-high', 'new', 'disabled']);
    });
});
