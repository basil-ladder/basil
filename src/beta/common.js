// Kept free of chart/date libraries so light pages can import it without pulling them in.
import { html } from 'lit-html';

export const dataBaseUrl = "https://data.basil-ladder.net/";

export function racecol(race) {
    switch (race) {
        case "PROTOSS":
        case "P":
            return "race_protoss";
        case "ZERG":
        case "Z":
            return "race_zerg";
        case "TERRAN":
        case "T":
            return "race_terran";
        case "RANDOM":
        case "R":
            return "race_random";
        default:
            return "race_unknown";
    }
}

export function racename(race) {
    switch (race) {
        case "PROTOSS":
        case "P":
            return "Protoss";
        case "ZERG":
        case "Z":
            return "Zerg";
        case "TERRAN":
        case "T":
            return "Terran";
        case "RANDOM":
        case "R":
            return "Random";
        default:
            return "Unknown";
    }
}

export function rankcol(rank) {
    return "rank_" + rank.toLowerCase();
}

// Icons come from one cached sprite file; see scripts/build-icons.mjs.
export const icon = (name, cls) => html`<svg class="icon ${cls || ""}" aria-hidden="true"><use href="/assets/icons.svg#${name}"></use></svg>`;

const rankOrder = { S: 0, A: 1, B: 2, C: 3, D: 4, E: 5, F: 6 };

export function rankValue(rank) {
    return rank in rankOrder ? rankOrder[rank] : 99999;
}

// Ladder order: enabled first, then bots with 30+ games, then rank, ELO, 7-day win rate, wins.
export function sortByRank(bots) {
    bots.sort((a, b) => {
        if (a.enabled !== b.enabled) return a.enabled ? -1 : 1;
        const aPlayed = a.won + a.lost;
        const bPlayed = b.won + b.lost;
        if (aPlayed < 30 && bPlayed >= 30) return 1;
        if (bPlayed < 30 && aPlayed >= 30) return -1;
        if (rankValue(a.rank) !== rankValue(b.rank)) return rankValue(a.rank) - rankValue(b.rank);
        if (a.rating !== b.rating) return b.rating - a.rating;
        if (a.win7 !== b.win7) return (b.win7 ?? -1) - (a.win7 ?? -1);
        return b.won - a.won;
    });
}
