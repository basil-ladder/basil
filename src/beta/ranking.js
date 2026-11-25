import { html, render } from 'lit-html';
import { dataBaseUrl, icon, racecol, racename, rankcol, rankValue, sortByRank } from './common.js';

const srcUrl = dataBaseUrl + "stats/ranking.json";
const container = document.querySelector("#rankingTable");
const percent = new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: 1, maximumFractionDigits: 1 });
const fullFormat = new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" });
const thisYearFormat = new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric" });
const otherYearFormat = new Intl.DateTimeFormat(undefined, { dateStyle: "medium" });

function formatDay(date) {
    return (date.getFullYear() === new Date().getFullYear() ? thisYearFormat : otherYearFormat).format(date);
}

const rankChange = bot =>
    bot.downRanked ? html`<span class="rank-change down" title="Rank went down recently">${icon("arrow-down")}</span>`
        : bot.upRanked ? html`<span class="rank-change up" title="Rank went up recently">${icon("arrow-up")}</span>`
            : bot.rankProtection ? html`<span class="rank-change" title="Rank changed recently and is locked">${icon("lock")}</span>`
                : "";

const botCell = bot => html`
<td class="bot">
    <div>
        <span class="race-slot" title=${racename(bot.race)}>${racecol(bot.race) === "race_random"
            ? icon("dice", "race-dice") : html`<span class="race ${racecol(bot.race)}"></span>`}</span>
        <a class="name" href="/beta/bot.html?bot=${encodeURIComponent(bot.botName)}" title=${bot.botName}>${bot.botName}</a>
    </div>
</td>`;

const elo = bot => bot.rating
    ? html`<span title="About ${Math.max(100, bot.rating - 1300)} human MMR">${bot.rating}</span>`
    : html`<span class="pending" title="Needs 30 games">pending</span>`;

const activeRow = (bot, linked) => html`
<tr id=${bot.botName} class=${bot.botName === linked ? "linked" : ""}>
    <td class="pos">${bot.position}</td>
    <td class="rank-cell"><div><span class="rank ${rankcol(bot.rank)}">${bot.rank}</span>${rankChange(bot)}</div></td>
    ${botCell(bot)}
    <td class="num elo">${elo(bot)}</td>
    <td class="break"></td>
    <td class="num win" data-label="7d">${bot.win7 === null ? "–" : percent.format(bot.win7)}</td>
    <td class="num wl" data-label="W/L">${bot.won} / ${bot.lost}</td>
    <td class="num crashes">${bot.crashesSinceUpdate > 0
        ? html`<span title="Crashes since the last update">${icon("car-crash")} ${bot.crashesSinceUpdate}</span>` : ""}</td>
    <td class="num updated" title=${bot.lastUpdated ? fullFormat.format(bot.lastUpdated) : ""}>
        ${bot.lastUpdated ? formatDay(bot.lastUpdated) : "–"}</td>
</tr>`;

const disabledRow = (bot, linked) => html`
<tr id=${bot.botName} class=${bot.botName === linked ? "linked" : ""}>
    ${botCell(bot)}
    <td class="num elo">${elo(bot)}</td>
    <td class="num wl">${bot.won} / ${bot.lost}</td>
    <td class="num win">${percent.format(bot.won / (bot.won + bot.lost))}</td>
    <td class="num crashes">${bot.crashed || ""}</td>
    <td class="reason">${bot.disabledReason || "Disabled on SSCAIT/locally"}</td>
</tr>`;

const nullsLast = (a, b, dir) => a === b ? 0 : a === null ? 1 : b === null ? -1 : dir * (a < b ? -1 : 1);

// First click sorts the way people usually want to read that column.
const columns = [
    { key: "pos", label: "#", value: b => b.position, firstDir: 1, title: "Ladder position" },
    // No heading: the badge explains itself, and sorting by rank equals sorting by "#".
    { key: "rank" },
    { key: "bot", label: "Bot", value: b => b.botName.toLowerCase(), firstDir: 1 },
    { key: "elo", label: "ELO", value: b => b.rating, firstDir: -1, title: "Hover a value for a rough human MMR estimate" },
    { key: "break" },
    { key: "win", label: "Win 7d", value: b => b.win7, firstDir: -1, title: "Win rate in the last 7 days" },
    { key: "wl", label: "Won / Lost", value: b => b.won + b.lost, firstDir: -1, title: "Sorts by number of games" },
    { key: "crashes", label: "Crashes", value: b => b.crashesSinceUpdate, firstDir: -1, title: "Crashes since the last update" },
    { key: "updated", label: "Updated", value: b => b.lastUpdated && b.lastUpdated.getTime(), firstDir: -1 },
];

const header = (state, update) => html`
<tr>${columns.map(c => c.key === "break" ? html`<th class="break"></th>`
    : c.key === "rank" ? html`<th class="rank-head"><span class="sr-only">Rank</span></th>` : html`
    <th class="${c.key === "pos" || c.key === "bot" ? "" : "num"} ${state.sort === c.key ? "sorted" : ""}"
        title=${c.title || ""} aria-sort=${state.sort === c.key ? (state.dir > 0 ? "ascending" : "descending") : "none"}>
        <button @click=${() => update(state.sort === c.key
            ? { dir: -state.dir } : { sort: c.key, dir: c.firstDir })}>${c.label}${state.sort === c.key
                ? icon(state.dir > 0 ? "arrow-up" : "arrow-down", "sort-arrow") : ""}</button>
    </th>`)}
</tr>`;

const view = (active, disabled, state, update) => {
    const column = columns.find(c => c.key === state.sort);
    const rows = active.slice().sort((a, b) => nullsLast(column.value(a), column.value(b), state.dir));
    return html`
<table class="ranking">
    <thead>${header(state, update)}</thead>
    <tbody>${rows.map(bot => activeRow(bot, state.linked))}</tbody>
</table>
<details class="disabled-bots" ?open=${disabled.some(b => b.botName === state.linked)}>
    <summary>Disabled bots (${disabled.length})</summary>
    <table class="ranking disabled">
        <thead><tr><th>Bot</th><th class="num">ELO</th><th class="num">Won / Lost</th><th class="num">Win</th>
            <th class="num">Crashes</th><th>Reason</th></tr></thead>
        <tbody>${disabled.map(bot => disabledRow(bot, state.linked))}</tbody>
    </table>
</details>`;
};

// Links like ranking.html#BotName point at one bot; a malformed link is ignored.
function linkedBotName() {
    try {
        return decodeURIComponent(location.hash.substring(1));
    } catch (e) {
        return "";
    }
}

render(html`Source: <a href=${srcUrl}>${srcUrl}</a>`, document.querySelector("#srcRef"));

fetch(srcUrl).then(r => r.json()).then(bots => {
    for (const bot of bots) {
        if (bot.rank === "UNRANKED") bot.rank = "U";
        if (bot.previousRank === "UNRANKED") bot.previousRank = "U";
        const inWindow = bot.wonInWindow + bot.lostInWindow;
        bot.win7 = inWindow > 0 ? bot.wonInWindow / inWindow : null;
        if (bot.won + bot.lost < 30) bot.rating = null;
        // The data uses Java's Instant.MIN (a huge negative number) for "unknown".
        bot.lastUpdated = bot.lastUpdated > 0 ? new Date(bot.lastUpdated * 1000) : null;
        const change = rankValue(bot.rank) - rankValue(bot.previousRank);
        bot.downRanked = bot.previousRank && change > 0;
        bot.upRanked = bot.previousRank && change < 0;
    }
    sortByRank(bots);
    const active = bots.filter(b => b.enabled);
    active.forEach((b, i) => b.position = i + 1);
    const disabled = bots.filter(b => !b.enabled && b.won + b.lost > 0);

    const state = { sort: "pos", dir: 1, linked: linkedBotName() };
    const update = change => {
        Object.assign(state, change);
        render(view(active, disabled, state, update), container);
    };
    update({});
    const linkedRow = state.linked && document.getElementById(state.linked);
    if (linkedRow) linkedRow.scrollIntoView({ block: "center" });
}).catch(e => {
    console.error(e);
    render(html`<p class="no-games">Could not load the ranking. Please try again later.</p>`, container);
});
