// Kept apart from bot.js, which draws charts on load, so the selection can be tested on its own.

// With fewer games, a 50% win rate is mostly luck (2-2, 3-3), not an even match.
export const CLOSE_OPPONENT_MIN_GAMES = 10;

// results are this bot's games: e is the opponent's index in botNames, w is 1 for a win.
export function closestOpponents(results, botNames) {
    const vs = {};
    results.forEach(x => {
        const s = vs[x.e] = (vs[x.e] || { won: 0, lost: 0, name: botNames[x.e] });
        if (x.w === 1) s.won++; else s.lost++;
    });
    const rate = s => s.won / (s.won + s.lost);
    const games = s => s.won + s.lost;
    // Pick the 10 closest to 50%, then show them by win rate so the bars read in order across the 50% line.
    // On ties, more games first: that win rate is more reliable.
    return Object.values(vs)
        .filter(s => games(s) >= CLOSE_OPPONENT_MIN_GAMES)
        .sort((a, b) => Math.abs(rate(a) - 0.5) - Math.abs(rate(b) - 0.5) || games(b) - games(a))
        .slice(0, 10)
        .sort((a, b) => rate(b) - rate(a) || games(b) - games(a));
}

// The crosstable has 7, 30 and 90 days like the bot page's presets; "All" and custom dates show all games.
export function crosstableUrl(botName, opponentNames, preset) {
    const params = new URLSearchParams();
    [botName, ...opponentNames].forEach(name => params.append("bot", name));
    if ([7, 30, 90].includes(preset)) params.set("range", preset);
    return "/beta/crosstable.html?" + params;
}
