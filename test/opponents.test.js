import { describe, expect, it } from 'vitest';
import { closestOpponents, crosstableUrl } from '../src/beta/opponents.js';

// won/lost games against opponent index e
const games = (e, won, lost) => [
    ...Array.from({ length: won }, () => ({ e, w: 1 })),
    ...Array.from({ length: lost }, () => ({ e, w: 0 })),
];

describe('closestOpponents', () => {
    const names = Array.from({ length: 14 }, (_, i) => 'bot' + i);

    it('ignores opponents with fewer than 10 games', () => {
        const result = closestOpponents([...games(0, 5, 4), ...games(1, 8, 7)], names);
        expect(result.map(s => s.name)).toEqual(['bot1']);
    });

    it('keeps the 10 closest to 50% and lists them by win rate, highest first', () => {
        const results = [
            ...games(0, 5, 5),   // 50%
            ...games(1, 6, 4),   // 60%
            ...games(2, 4, 6),   // 40%
            ...games(3, 10, 0),  // 100%, farthest: dropped
            ...games(4, 0, 10),  // 0%, farthest: dropped
            ...games(5, 7, 3),
            ...games(6, 3, 7),
            ...games(7, 11, 9),
            ...games(8, 9, 11),
            ...games(9, 12, 8),
            ...games(10, 8, 12),
            ...games(11, 13, 7),
        ];
        const result = closestOpponents(results, names);
        expect(result).toHaveLength(10);
        expect(result.map(s => s.name)).not.toContain('bot3');
        expect(result.map(s => s.name)).not.toContain('bot4');
        const rates = result.map(s => s.won / (s.won + s.lost));
        expect(rates).toEqual([...rates].sort((a, b) => b - a));
    });

    it('lists the opponent with more games first on equal win rates', () => {
        const result = closestOpponents([...games(0, 5, 5), ...games(1, 20, 20)], names);
        expect(result.map(s => s.name)).toEqual(['bot1', 'bot0']);
    });
});

describe('crosstableUrl', () => {
    it('lists the bot first, then its opponents, and keeps a matching range', () => {
        const url = new URL(crosstableUrl('Me', ['A & B', 'C,D'], 30), 'https://example.org');
        expect(url.pathname).toBe('/beta/crosstable.html');
        expect(url.searchParams.getAll('bot')).toEqual(['Me', 'A & B', 'C,D']);
        expect(url.searchParams.get('range')).toBe('30');
    });

    it('shows all games for "All" and custom date ranges', () => {
        expect(new URL(crosstableUrl('Me', [], 'all'), 'https://x').searchParams.has('range')).toBe(false);
        expect(new URL(crosstableUrl('Me', [], null), 'https://x').searchParams.has('range')).toBe(false);
    });
});
