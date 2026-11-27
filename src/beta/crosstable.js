import { html, render } from 'lit-html';
import { dataBaseUrl, racecol } from './common.js';

const controlsNode = document.getElementById("crosstableControls");
const tableNode = document.getElementById("crosstable");

const presets = [[7, "7 days"], [30, "30 days"], [90, "90 days"], [180, "180 days"], [null, "All"]];
// Links like ?bot=A&bot=B&range=30 show only those bots; the bot page's close opposition links here.
const params = new URLSearchParams(location.search);
const pickedBots = new Set(params.getAll("bot"));
const rangeParam = Number(params.get("range"));
const state = {
    range: presets.some(([days]) => days && days === rangeParam) ? rangeParam : null,
    enabledOnly: true, minElo: "", maxElo: ""
};
const requests = new Map();

function load(range) {
    if (!requests.has(range)) {
        requests.set(range, fetch(dataBaseUrl + "stats/botVsBot" + (range ? "_" + range : "") + ".json")
            .then(r => { if (!r.ok) throw new Error(r.status); return r.json(); }));
    }
    return requests.get(range);
}

const escapeHtml = s => String(s).replace(/[&<>"]/g, c => "&#" + c.charCodeAt(0) + ";");

// Same shading as the classic crosstable, which people know how to read.
function cellColor(won, lost, isDark) {
    const w = won + 1;
    const l = lost + 1;
    const lossStr = Math.min(1, (l / w) * (l / w));
    const winStr = Math.min(1, (w / l) * (w / l));
    let c;
    if (isDark) {
        const [from, t] = w >= l ? [[51, 133, 255], lossStr] : [[255, 153, 51], winStr];
        c = from.map((v, i) => v * (1 - t) + [160, 160, 170][i] * t);
    } else {
        c = [255 * lossStr, 255 * winStr, 255 * Math.min(winStr, lossStr)].map(Math.ceil);
    }
    const base = isDark ? [26, 26, 46] : [255, 255, 255];
    return "rgb(" + c.map((v, i) => Math.round(0.8 * v + 0.2 * base[i])).join(",") + ")";
}

// A plain HTML string instead of lit templates: 14,000+ cells with a template part and two listeners each
// took ~0.9 s on desktop and several seconds on phones.
function tableHtml(data, bots) {
    const isDark = document.documentElement.getAttribute("data-theme") === "dark";
    let h = '<table class="crosstable-grid" style="--cols: ' + bots.length + '"><thead><tr><th class="corner">Bot</th><th class="elo">ELO</th>';
    for (const b of bots) {
        h += '<th class="colhead ' + racecol(b.race) + '"><div>' + escapeHtml(b.name) + "</div></th>";
    }
    h += "</tr></thead><tbody>";
    bots.forEach((a, pos) => {
        // Only position and name stay in place while scrolling; the ELO scrolls away to leave room on phones.
        h += '<tr><th class="rowhead ' + racecol(a.race) + '"><div><span class="pos">' + (pos + 1) + '</span><a href="/beta/bot.html?bot=' +
            encodeURIComponent(a.name) + '" title="' + escapeHtml(a.name) + '">' + escapeHtml(a.name) + '</a></div></th><th class="elo">' + (a.rating || "") + "</th>";
        for (const o of bots) {
            const won = data.botinfos[a.i].vsBotIdxWon[o.i];
            const lost = data.botinfos[o.i].vsBotIdxWon[a.i];
            if (a === o) h += '<td class="self"></td>';
            else if (won + lost === 0) h += "<td></td>";
            else h += '<td style="background:' + cellColor(won, lost, isDark) + '">' + won + "–" + lost + "</td>";
        }
        h += "</tr>";
    });
    return h + "</tbody></table>";
}

function selectedBots(data) {
    const min = Number(state.minElo) || 0;
    const max = Number(state.maxElo) || Infinity;
    const wanted = pickedBots.size
        ? b => pickedBots.has(b.name)
        : b => (!state.enabledOnly || b.enabled) && (!b.rating || (b.rating >= min && b.rating <= max));
    return data.botinfos
        .map((b, i) => ({ name: b.name, race: b.race, rating: b.rating, enabled: b.enabled, i }))
        .filter(wanted)
        .sort((a, b) => (b.enabled - a.enabled) || (b.rating - a.rating));
}

let shown = null;

function renderTable() {
    const range = state.range;
    load(range).then(data => {
        if (state.range !== range) return;
        const bots = selectedBots(data);
        shown = { data, bots };
        tableNode.innerHTML = bots.length ? tableHtml(data, bots) : '<p class="no-games">No bot matches.</p>';
        fitToWindow();
    }).catch(e => {
        console.error(e);
        tableNode.innerHTML = '<p class="no-games">Could not load the crosstable. Please try again later.</p>';
    });
}

function update(change) {
    Object.assign(state, change);
    renderControls();
    renderTable();
}

function renderControls() {
    render(html`
<div class="crosstable-controls">
    <span class="presets">${presets.map(([days, label]) => html`
        <button class=${state.range === days ? "active" : ""} @click=${() => update({ range: days })}>${label}</button>`)}
    </span>
    ${pickedBots.size ? html`<span>Selected bots only. <a href="/beta/crosstable.html">Show all bots</a></span>` : html`
    <label><input type="checkbox" .checked=${state.enabledOnly} @change=${e => update({ enabledOnly: e.target.checked })}> Enabled only</label>
    <label>ELO <input type="number" inputmode="numeric" placeholder="min" .value=${state.minElo}
        @change=${e => update({ minElo: e.target.value })}></label>
    <label>– <input type="number" inputmode="numeric" placeholder="max" .value=${state.maxElo}
        @change=${e => update({ maxElo: e.target.value })}></label>`}
</div>`, controlsNode);
}

// One listener for the whole grid: marks the hovered cell's row and column headings,
// and fills in the tooltip only for the cell under the mouse.
let marked = [];
tableNode.addEventListener("mouseover", e => {
    const cell = e.target.closest("td");
    marked.forEach(n => n.classList.remove("hl"));
    marked = [];
    if (!cell || !shown) return;
    const row = cell.parentElement;
    const col = cell.cellIndex;
    const a = shown.bots[row.sectionRowIndex];
    const o = shown.bots[col - 2];
    marked = [row.cells[0], tableNode.querySelector("thead tr").cells[col]];
    marked.forEach(n => n.classList.add("hl"));
    if (!cell.title && a !== o) cell.title = a.name + " vs " + o.name + (cell.textContent ? ": " + cell.textContent : ": no games");
});
tableNode.addEventListener("mouseleave", () => {
    marked.forEach(n => n.classList.remove("hl"));
    marked = [];
});

// A grid wider than the page column uses the whole window width: more opponents fit on screen.
// With a mouse it also ends at the bottom of the window, so its horizontal scroll bar is visible without scrolling
// the page. Touch screens swipe instead and keep the full screen height for the grid.
const mouse = window.matchMedia("(pointer: fine)");

function fitToWindow() {
    tableNode.style.marginLeft = "";
    tableNode.style.width = "";
    const box = tableNode.getBoundingClientRect();
    if (tableNode.scrollWidth > tableNode.clientWidth) {
        tableNode.style.marginLeft = -box.left + "px";
        tableNode.style.width = document.documentElement.clientWidth + "px";
    }
    tableNode.style.maxHeight = mouse.matches ? Math.max(320, window.innerHeight - (box.top + window.scrollY)) + "px" : "";
}
window.addEventListener("resize", fitToWindow);
fitToWindow();

// The header's theme button calls this; the cell colours are computed per theme, the data is already loaded.
window.basilUpdateChartsTheme = () => { if (shown) tableNode.innerHTML = tableHtml(shown.data, shown.bots); };

renderControls();
renderTable();
