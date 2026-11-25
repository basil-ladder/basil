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
