import { html, render } from 'lit-html';
import { dataBaseUrl, icon, racecol, racename, rankcol } from './common.js';

const srcUrl = dataBaseUrl + "stats/games_24h.json";
const viewerUrl = "https://dgant.github.io/openbw-replay-viewer/?rep=";
const timeFormat = new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" });
const fullFormat = new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" });

function formatGameTime(frameCount) {
    const secs = Math.round(frameCount / 24);
    return Math.floor(secs / 60) + ":" + String(secs % 60).padStart(2, "0");
}

const botCell = bot => html`
<td class="bot">
    <div>
        <span class="rank ${rankcol(bot.rank)}">${bot.rank}</span>
        <span class="race-slot" title=${racename(bot.race) + (bot.randomBot ? " (random bot)" : "")}>
            ${bot.randomBot
                ? icon("dice", "race-dice " + racecol(bot.race))
                : html`<span class="race ${racecol(bot.race)}"></span>`}
        </span>
        <a class="name${bot.winner ? " winner" : ""}" href="/beta/bot.html?bot=${encodeURIComponent(bot.name)}" title=${bot.name}>${bot.name}</a>
        ${bot.crashed ? html`<span class="crashed" title="Crashed">${icon("car-crash")}</span>` : ""}
    </div>
</td>`;

const unitBadge = (show, name, text) => show ? html`<span title=${text}>${icon(name)}</span>` : "";

const row = (game, hidden, alt) => {
    // Winner first, so the eye finds the result without looking for an icon. Crashes can leave no winner.
    const [first, second] = game.botB.winner ? [game.botB, game.botA] : [game.botA, game.botB];
    const ended = new Date(game.timestamp * 1000);
    // Both replays show the same game, the winner's runs a bit longer. A crashed bot's replay may be missing.
    const replayUrl = (first.crashed && !second.crashed ? second : first).replayUrl;
    return html`
<tr ?hidden=${hidden} class=${alt ? "alt" : ""}>
    ${botCell(first)}
    ${botCell(second)}
    <td class="map" title=${game.map}>${game.map}</td>
    <td class="ended" title=${fullFormat.format(ended)}>${timeFormat.format(ended)}</td>
    <td class="time">
        <span class="badges">
            ${unitBadge(game.notPlayed, "poo-storm", "Did not start: a bot crashed early")}
            ${unitBadge(game.realTimeout, "fish", "Timed out (real time)")}
            ${unitBadge(game.frameTimeout, "stopwatch", "Timed out (in-game)")}
            ${unitBadge(game.nukes > 1, "radiation", game.nukes + " nukes")}
            ${unitBadge(game.mm >= 400, "biohazard", game.mm + " Marines + Medics")}
            ${unitBadge(game.cruisercarrier >= 20, "fighter-jet", game.cruisercarrier + " Battlecruisers + Carriers")}
            ${unitBadge(game.arbiter >= 10, "user-secret", game.arbiter + " Arbiters")}
            ${unitBadge(game.lurkers >= 100, "spider", game.lurkers + " Lurkers")}
            ${unitBadge(game.queens >= 80, "chess-queen", game.queens + " Queens")}
            ${unitBadge(game.guardians >= 10, "pastafarianism", game.guardians + " Guardians")}
            ${unitBadge(game.defilers >= 10, "smog", game.defilers + " Defilers")}
        </span>
        <span class="duration" title="Game length">${icon("hourglass-half")}</span>${game.gameTime || "–"}
    </td>
    <td class="actions">
        ${game.validGame ? html`
        <a href=${viewerUrl + encodeURIComponent(replayUrl)} target="_blank" title="Watch replay">${icon("eye")}</a>
        <a href=${replayUrl} title="Download replay">${icon("download")}</a>` : ""}
    </td>
</tr>`;
};

const filterControls = (maps, state, update) => html`
<div class="game-filters">
    <input type="search" placeholder="Search bot" .value=${state.search}
        @input=${e => update({ search: e.target.value })}>
    <select @change=${e => update({ map: e.target.value })}>
        <option value="">All maps</option>
        ${maps.map(m => html`<option ?selected=${m === state.map}>${m}</option>`)}
    </select>
</div>`;

// Laying out all ~1100 rows took 0.35 s on desktop and over a second on phones.
const initialRows = 100;

const view = (games, maps, state, update, hideFilters) => {
    const search = state.search.trim().toLowerCase();
    const matches = g => (!state.map || g.map === state.map) &&
        (!search || g.botA.name.toLowerCase().includes(search) || g.botB.name.toLowerCase().includes(search));
    // Rows are hidden rather than removed: rebuilding 1000+ rows per keystroke took ~0.5 s.
    let matchCount = 0;
    const shown = games.map(g => matches(g) && (++matchCount <= initialRows || state.showAll));
    let visibleCount = 0;
    const alt = shown.map(s => s && ++visibleCount % 2 === 0);
    return html`
${hideFilters ? "" : filterControls(maps, state, update)}
<table class="games">
    <thead>
        <tr>
            <th colspan="2">Bots</th>
            <th>Map</th>
            <th class="ended">Ended</th>
            <th class="time">Time</th>
            <th class="actions"><span class="sr-only">Replay</span></th>
        </tr>
    </thead>
    <tbody>${games.map((g, i) => row(g, !shown[i], alt[i]))}</tbody>
</table>
${matchCount === 0 ? html`<p class="no-games">No games match.</p>` : ""}
${matchCount > initialRows && !state.showAll ? html`
<button class="show-all" @click=${() => update({ showAll: true })}>Show all ${matchCount} games</button>` : ""}`;
};

function parseGames(data) {
    const { results, maps, bots } = data;
    return results.map(g => {
        const [botA, botB] = [g.botA, g.botB].map(botResult => {
            const bot = bots[botResult.botIndex];
            return {
                name: bot.name,
                winner: botResult.winner,
                crashed: botResult.crashed,
                race: botResult.race || bot.race,
                randomBot: botResult.race && botResult.race !== bot.race,
                rank: bot.rank
            };
        });
        const map = maps[g.mapIndex];
        const events = g.gameEvents && g.gameEvents.reduce((acc, e) => {
            const m = acc[e.unit] = (acc[e.unit] || {});
            m[e.event] = e.amount;
            return acc;
        }, {}) || {};
        botA.replayUrl = dataBaseUrl + "bots/" + botA.name + "/" + botA.name + " vs " + botB.name + " " + map + " " + g.gameHash + ".rep";
        botB.replayUrl = dataBaseUrl + "bots/" + botB.name + "/" + botB.name + " vs " + botA.name + " " + map + " " + g.gameHash + ".rep";
        return {
            botA: botA,
            botB: botB,
            timestamp: g.endedAt,
            gameTime: g.frameCount ? formatGameTime(g.frameCount) : undefined,
            notPlayed: !g.frameCount,
            validGame: !g.invalidGame && !g.realTimeout,
            realTimeout: g.realTimeout,
            frameTimeout: g.frameTimeout,
            map: map,
            nukes: events[14] && events[14][6],
            mm: (events[0] ? events[0][2] : 0) + (events[34] ? events[34][2] : 0),
            cruisercarrier: (events[72] ? events[72][2] : 0) + (events[12] ? events[12][2] : 0),
            arbiter: events[71] && events[71][2],
            lurkers: events[103] && events[103][2],
            queens: events[45] && events[45][2],
            guardians: events[44] && events[44][2],
            defilers: events[46] && events[46][2],
        };
    }).sort((a, b) => b.timestamp - a.timestamp);
}

const sourceRefText = document.querySelector("#srcRef");
if (sourceRefText) {
    render(html`Source: <a href=${srcUrl}>${srcUrl}</a>`, sourceRefText);
}

let gamesRequest;

function renderGameListing(options) {
    const filter = options && options.filter;
    const hideFilters = options && options.hideFilters;
    const container = document.querySelector("#gamesTable");
    gamesRequest = gamesRequest || fetch(srcUrl).then(r => r.json());
    gamesRequest.then(data => {
        let games = parseGames(data);
        if (filter) games = filter(games);
        const maps = [...new Set(games.map(g => g.map))].sort();
        const state = { search: "", map: "", showAll: false };
        const update = change => {
            Object.assign(state, change);
            render(view(games, maps, state, update, hideFilters), container);
        };
        update({});
    }).catch(e => {
        console.error(e);
        render(html`<p class="no-games">Could not load the games. Please try again later.</p>`, container);
    });
}

export default renderGameListing;
