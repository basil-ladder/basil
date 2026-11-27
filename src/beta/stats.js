import { html, render } from 'lit-html';
import { dataBaseUrl, formatMinutes, racecol } from './common.js';

const srcUrl = dataBaseUrl + "stats/stats.json";
const container = document.getElementById("stats");
const count = new Intl.NumberFormat();
const percent = new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: 1, maximumFractionDigits: 1 });
const hourFormat = new Intl.DateTimeFormat(undefined, { hour: "numeric" });
const races = ["terran", "protoss", "zerg", "random"];
const raceNames = { terran: "Terran", protoss: "Protoss", zerg: "Zerg", random: "Random" };

const tile = (label, value, note) => html`
<div class="tile"><span class="label">${label}</span><span class="value">${value}</span>${note ? html`<span class="note">${note}</span>` : ""}</div>`;

const raceBlock = race => html`<span class="race ${racecol(race.toUpperCase())}"></span>`;

const winRateRow = (label, won, lost) => html`
<div class="opp">
    <span class="name">${label}</span>
    <span class="wl">${count.format(won)}–${count.format(lost)}</span>
    <span class="wr-bar" title="${count.format(won)} won, ${count.format(lost)} lost"><span style="width: ${100 * won / (won + lost)}%"></span></span>
    <span class="wr">${percent.format(won / (won + lost))}</span>
</div>`;

const view = d => {
    // Each cell counts the wins of the row race against the column race.
    const wins = d.raceCrossTable;
    const bots = { terran: d.terranBots, protoss: d.protossBots, zerg: d.zergBots, random: d.randomBots };
    const totalBots = races.reduce((sum, r) => sum + bots[r], 0);
    const pairs = [];
    races.forEach((a, i) => races.slice(i + 1).forEach(b => pairs.push([a, b])));
    // Mirror games (e.g. Terran vs Terran) always split 50/50, so they would only pull every race toward 50%.
    const overall = races.map(r => {
        const others = races.filter(o => o !== r);
        return { race: r, won: others.reduce((s, o) => s + wins[r][o], 0), lost: others.reduce((s, o) => s + wins[o][r], 0) };
    });
    const update = new Date(d.nextUpdateTime);
    return html`
<div class="stat-tiles">
    ${tile("Games played", count.format(d.gamesPlayed))}
    ${tile("Bots", count.format(totalBots))}
    ${tile("Crashes", count.format(d.crashes), percent.format(d.crashes / d.gamesPlayed) + " of games")}
    ${tile("Average game", formatMinutes(d.averageGameRealtime), "minutes, real time")}
    ${tile("Bot update window", hourFormat.formatRange(update, new Date(update.getTime() + 3600 * 1000)), "your time")}
</div>

<h2>Bots per race</h2>
<div class="race-split">
    ${races.map(r => html`<span class=${racecol(r.toUpperCase())} style="flex: ${bots[r]}" title="${raceNames[r]}: ${bots[r]}"></span>`)}
</div>
<div class="race-split-legend">
    ${races.map(r => html`<span>${raceBlock(r)} ${raceNames[r]} <b>${bots[r]}</b> <small>(${percent.format(bots[r] / totalBots)})</small></span>`)}
</div>

<div class="side-by-side">
<section>
    <h2>Race matchups</h2>
    <small>Wins and losses of the first race against the second, all games since the ladder started. The line in each bar marks 50%.</small>
    <div class="winrate-list matchups">
        ${pairs.map(([a, b]) => winRateRow(raceNames[a] + " vs " + raceNames[b], wins[a][b], wins[b][a]))}
    </div>
</section>
<section>
    <h2>Win rate per race</h2>
    <small>Against the other races only: mirror games (e.g. Terran vs Terran) always split 50/50.</small>
    <div class="winrate-list matchups">
        ${overall.map(o => winRateRow(raceNames[o.race], o.won, o.lost))}
    </div>
</section>
</div>`;
};

render(html`Source: <a href=${srcUrl}>${srcUrl}</a>`, document.getElementById("srcRef"));

fetch(srcUrl).then(r => r.json()).then(d => render(view(d), container)).catch(e => {
    console.error(e);
    render(html`<p>Could not load the stats. Please try again later.</p>`, container);
});
