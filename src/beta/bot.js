import Chart from 'chart.js'
import { applyThemeToChart, chartThemeColors, registerChartUpdater } from './charts.js'
import { dataBaseUrl, icon, racecol, racename, rankcol, sortByRank } from './common.js'
import { html, render } from 'lit-html'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import renderGameListing from './gamelisting';

const statsBaseUrl = dataBaseUrl + "stats/";
const botName = new URLSearchParams(location.search).get("bot") || "";
// Narrow screens get horizontal bars and no in-bar labels; they did not fit. Same width as $cards-below in basil.scss.
const narrow = matchMedia("(max-width: 950px)").matches;
const percent = new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: 1, maximumFractionDigits: 1 });
const dayFormat = new Intl.DateTimeFormat(undefined, { dateStyle: "medium" });
const DAY = 86400;

renderGameListing({
    filter: games => games.filter(game => game.botA.name === botName || game.botB.name === botName)
});

// --- Header ---

const stat = (label, value, title) => html`<div class="stat" title=${title || ""}><span>${label}</span>${value}</div>`;

const botHeader = (bot, maxElo) => html`
<div class="bot-title">
    <span class="rank ${rankcol(bot.rank)}">${bot.rank}</span>
    <span class="race-slot" title=${racename(bot.race)}>${racecol(bot.race) === "race_random"
        ? icon("dice", "race-dice") : html`<span class="race ${racecol(bot.race)}"></span>`}</span>
    <h1>${bot.botName}</h1>
</div>
<div class="bot-stats">
    ${stat("Race", racename(bot.race))}
    ${stat("ELO", bot.won + bot.lost >= 30 ? bot.rating : "pending")}
    ${stat("Max ELO", maxElo || "–")}
    ${stat("Win rate", bot.won + bot.lost > 0 ? percent.format(bot.won / (bot.won + bot.lost)) : "–")}
    ${stat("Won / Lost", html`${bot.won} / ${bot.lost}`)}
    ${stat("Crashes", bot.crashed, bot.crashesSinceUpdate + " since the last update")}
    ${stat("Updated", bot.lastUpdated ? dayFormat.format(bot.lastUpdated) : "–")}
</div>
${bot.enabled ? "" : html`<p class="bot-disabled">Disabled: ${bot.disabledReason || "Disabled on SSCAIT/locally"}</p>`}`;

// --- Date range ---

const now = Date.now() / 1000;
const selectedRange = { startSecond: now - 30 * DAY, endSecond: now, preset: 30 };
let firstGameSecond = 0;
let secondsToAggregate = (selectedRange.endSecond - selectedRange.startSecond) / 60;
const updaters = [];

const toInputDate = second => {
    const d = new Date(second * 1000);
    return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") + "-" + String(d.getDate()).padStart(2, "0");
};
const fromInputDate = value => new Date(value + "T00:00").getTime() / 1000;

function setRange(startSecond, endSecond, preset) {
    if (!(startSecond < endSecond)) return false;
    Object.assign(selectedRange, { startSecond, endSecond, preset });
    secondsToAggregate = (endSecond - startSecond) / 60;
    renderRangeBar();
    updaters.forEach(update => update());
    return true;
}

// An invalid date (empty, or end before start) puts the field back to the current range.
function onDateChange(input, toRange, current) {
    if (!input.value || !setRange(...toRange(fromInputDate(input.value)), null)) input.value = current;
}

const presetButton = (days, label) => html`
<button class=${selectedRange.preset === days ? "active" : ""}
    @click=${() => setRange(days === "all" ? firstGameSecond : now - days * DAY, now, days)}>${label}</button>`;

function renderRangeBar() {
    render(html`
<span class="range-bot">${botName}</span>
<span class="presets">${presetButton(7, "7 days")}${presetButton(30, "30 days")}${presetButton(90, "90 days")}${presetButton("all", "All")}</span>
<span class="dates">
    <input type="date" aria-label="Start date" .value=${toInputDate(selectedRange.startSecond)}
        @change=${e => onDateChange(e.target, start => [start, selectedRange.endSecond], toInputDate(selectedRange.startSecond))}>
    –
    <input type="date" aria-label="End date" .value=${toInputDate(selectedRange.endSecond - 1)}
        @change=${e => onDateChange(e.target, end => [selectedRange.startSecond, end + DAY], toInputDate(selectedRange.endSecond - 1))}>
</span>`, document.getElementById("rangeBar"));
}
renderRangeBar();

// --- Charts ---

const baseOptions = { responsive: true, maintainAspectRatio: false };
const winRate = (wins, losses) => percent.format(wins / Math.max(1, wins + losses));

// Won/lost bar chart: vertical on wide screens, one row per category on narrow ones.
function winLossBarChart(canvasId, boxId, rotateLabels) {
    const options = Object.assign({}, baseOptions, {
        plugins: {
            datalabels: {
                display: !narrow,
                align: "top",
                rotation: rotateLabels ? -85 : 0,
                font: { size: 10 },
                formatter: (value, context) => {
                    const wins = context.chart.data.datasets[0].data[context.dataIndex];
                    const losses = context.chart.data.datasets[1].data[context.dataIndex];
                    return wins + losses > 0 ? winRate(wins, losses) : "";
                }
            }
        },
        tooltips: {
            mode: "index",
            callbacks: {
                footer: items => "Win rate: " + winRate(
                    Number(narrow ? items[0].xLabel : items[0].yLabel), Number(narrow ? items[1].xLabel : items[1].yLabel))
            }
        }
    });
    const chart = new Chart(document.getElementById(canvasId), {
        type: narrow ? "horizontalBar" : "bar",
        plugins: [ChartDataLabels],
        options: options
    });
    return (labels, won, lost) => {
        chart.data.datasets = [
            { label: "Won", data: won, backgroundColor: "#88AAEB" },
            { label: "Lost", data: lost, backgroundColor: "#EBAAAA", datalabels: { display: false } },
        ];
        const category = { stacked: true, type: "category", labels: labels, ticks: { autoSkip: false } };
        const value = { stacked: true };
        chart.options.scales = narrow ? { yAxes: [category], xAxes: [value] } : { xAxes: [category], yAxes: [value] };
        if (narrow) document.getElementById(boxId).style.height = (80 + 22 * labels.length) + "px";
        applyThemeToChart(chart);
        chart.update();
        chart.resize();
    };
}

function inRange(game) {
    return game.t >= selectedRange.startSecond && game.t <= selectedRange.endSecond;
}

function byWinRateThenLosses(a, b) {
    const dWR = a.won / Math.max(1, a.won + a.lost) - b.won / Math.max(1, b.won + b.lost);
    if (dWR !== 0) return dWR;
    return b.lost - a.lost;
}

function setupPerMapWinsChart(data) {
    const draw = winLossBarChart("perMapWinsChart", "perMapWinsBox", false);
    function updateChart() {
        const maps = data.maps.map(name => ({ won: 0, lost: 0, name: name }));
        data.results.filter(inRange).forEach(x => x.w === 1 ? maps[x.m].won++ : maps[x.m].lost++);
        const shown = maps.filter(x => x.won + x.lost > 0).sort(byWinRateThenLosses);
        draw(shown.map(x => x.name), shown.map(x => x.won), shown.map(x => x.lost));
    }
    updateChart();
    updaters.push(updateChart);
    registerChartUpdater(updateChart);
}

const csvField = value => /[",\n]/.test(value) ? '"' + String(value).replace(/"/g, '""') + '"' : String(value);

function setupVsChart(data) {
    const draw = winLossBarChart("vsChart", "vsBox", true);
    function updateChart() {
        const bots = data.bots.map(name => ({ won: 0, lost: 0, name: name }));
        data.results.filter(inRange).forEach(x => x.w === 1 ? bots[x.e].won++ : bots[x.e].lost++);
        const played = bots.filter(x => x.won + x.lost > 0).sort(byWinRateThenLosses);
        const csv = "bot,won,lost\n" + played.map(x => [x.name, x.won, x.lost].map(csvField).join(",")).join("\n") + "\n";
        render(html`<a download="top_contenders.csv" href="data:text/csv;charset=utf-8,${encodeURIComponent(csv)}">Download all as CSV</a>`,
            document.getElementById("vsChartData"));
        const shown = played.slice(0, 70);
        draw(shown.map(x => x.name), shown.map(x => x.won), shown.map(x => x.lost));
    }
    updateChart();
    updaters.push(updateChart);
    registerChartUpdater(updateChart);
}

const raceNames = { T: "Terran", P: "Protoss", Z: "Zerg", R: "Unknown" };

const winRateBar = (won, lost) => html`
    <span class="wl">${won}–${lost}</span>
    <span class="wr-bar" title="${won} won, ${lost} lost"><span style="width: ${100 * won / (won + lost)}%"></span></span>
    <span class="wr">${percent.format(won / (won + lost))}</span>`;

function setupRaceMatchups(data) {
    const node = document.getElementById("raceMatchups");
    function update() {
        const pairs = {};
        const all = { won: 0, lost: 0 };
        data.results.filter(inRange).forEach(x => {
            const s = pairs[x.r + x.eR] = pairs[x.r + x.eR] || { won: 0, lost: 0 };
            if (x.w === 1) { s.won++; all.won++; } else { s.lost++; all.lost++; }
        });
        const ownRaces = ["P", "T", "Z", "R"].filter(own => Object.keys(pairs).some(k => k[0] === own));
        // A bot that always plays one race needs no "Protoss vs" in every row.
        const label = (own, enemy) => ownRaces.length > 1 ? raceNames[own] + " vs " + raceNames[enemy] : "vs " + raceNames[enemy];
        const rows = [];
        for (const own of ownRaces) {
            const total = { won: 0, lost: 0 };
            for (const enemy of ["T", "P", "Z", "R"]) {
                const s = pairs[own + enemy];
                if (!s) continue;
                rows.push({ label: label(own, enemy), won: s.won, lost: s.lost });
                total.won += s.won;
                total.lost += s.lost;
            }
            if (ownRaces.length > 1) rows.push({ label: "as " + raceNames[own] + ", all games", won: total.won, lost: total.lost, total: true });
        }
        if (rows.length) rows.push({ label: "All games", won: all.won, lost: all.lost, total: true });
        render(rows.length
            ? html`<div class="winrate-list matchups">${rows.map(r => html`
                <div class="opp ${r.total ? "total" : ""}"><span class="name">${r.label}</span>${winRateBar(r.won, r.lost)}</div>`)}</div>`
            : html`<p><small>No games in this range.</small></p>`, node);
    }
    update();
    updaters.push(update);
}

// With fewer games, a 50% win rate is mostly luck (2-2, 3-3), not an even match.
const CLOSE_OPPONENT_MIN_GAMES = 10;

const closeOpponentRow = (s, info) => html`
<div class="opp">
    ${info ? html`<span class="rank ${rankcol(info.rank === "UNRANKED" ? "U" : info.rank)}">${info.rank === "UNRANKED" ? "U" : info.rank}</span>
    <span class="race-slot" title=${racename(info.race)}>${racecol(info.race) === "race_random"
        ? icon("dice", "race-dice") : html`<span class="race ${racecol(info.race)}"></span>`}</span>` : html`<span></span><span></span>`}
    <a class="name" href="/beta/bot.html?bot=${encodeURIComponent(s.name)}" title=${s.name}>${s.name}</a>
    ${winRateBar(s.won, s.lost)}
</div>`;

function setupCloseOpposition(data, botsByName) {
    const node = document.getElementById("closeOpposition");
    function update() {
        const vs = {};
        data.results.filter(inRange).forEach(x => {
            const s = vs[x.e] = (vs[x.e] || { won: 0, lost: 0, name: data.bots[x.e] });
            if (x.w === 1) s.won++; else s.lost++;
        });
        const rate = s => s.won / (s.won + s.lost);
        const games = s => s.won + s.lost;
        // Pick the 10 closest to 50%, then show them by win rate so the bars read in order across the 50% line.
        // On ties, more games first: that win rate is more reliable.
        const closest = Object.values(vs)
            .filter(s => games(s) >= CLOSE_OPPONENT_MIN_GAMES)
            .sort((a, b) => Math.abs(rate(a) - 0.5) - Math.abs(rate(b) - 0.5) || games(b) - games(a))
            .slice(0, 10)
            .sort((a, b) => rate(b) - rate(a) || games(b) - games(a));
        render(closest.length
            ? html`<div class="winrate-list close-opposition">${closest.map(s => closeOpponentRow(s, botsByName.get(s.name)))}</div>`
            : html`<p><small>No opponent has ${CLOSE_OPPONENT_MIN_GAMES} or more games in this range, too few to tell an even match from luck. Try 30 days or more.</small></p>`, node);
    }
    update();
    updaters.push(update);
}

// BWAPI runs at about 23.8 frames per second, so 1429 frames are one game minute.
const FRAMES_PER_MINUTE = 1429;

function setupPerPlayLengthChart(data) {
    const chart = new Chart(document.getElementById("gameLenChart"), {
        type: "bar",
        plugins: [ChartDataLabels],
        options: Object.assign({}, baseOptions, {
            plugins: {
                datalabels: {
                    display: !narrow,
                    align: "top",
                    rotation: -85,
                    font: { size: 10 },
                    formatter: (value, context) => {
                        const wins = context.chart.data.datasets[0].data[context.dataIndex];
                        const losses = context.chart.data.datasets[1].data[context.dataIndex];
                        return wins > 0 && losses > 0 ? winRate(wins, losses) : "";
                    }
                }
            },
            tooltips: {
                mode: "index",
                callbacks: { footer: items => "Win rate: " + winRate(items[0].yLabel, items[1].yLabel) }
            }
        })
    });
    function updateChart() {
        const minutes = Array.from({ length: 60 }, () => ({ won: 0, lost: 0 }));
        data.results.filter(inRange).forEach(x => {
            if (x.fc && x.fc < 60 * FRAMES_PER_MINUTE) {
                const s = minutes[Math.trunc(x.fc / FRAMES_PER_MINUTE)];
                if (x.w === 1) s.won++; else s.lost++;
            }
        });
        chart.data.datasets = [
            { label: "Won", data: minutes.map(a => a.won), backgroundColor: "#88AAEB" },
            { label: "Lost", data: minutes.map(a => a.lost), backgroundColor: "#EBAAAA", datalabels: { display: false } },
        ];
        chart.options.scales = {
            xAxes: [{ stacked: true, type: "category", labels: minutes.map((_, i) => i), ticks: { autoSkip: narrow } }],
            yAxes: [{ stacked: true }]
        };
        applyThemeToChart(chart);
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
    registerChartUpdater(updateChart);
}

// --- ELO chart ---

function eloChartColors() {
    return chartThemeColors().isDark ? [
        "rgba(255, 80, 80, 0.9)", "rgba(80, 220, 80, 0.7)", "rgba(220, 220, 80, 0.7)",
        "rgba(100, 100, 255, 0.8)", "rgba(220, 80, 220, 0.7)", "rgba(200, 200, 200, 0.7)",
    ] : [
        "rgba(200, 0, 0, 0.8)", "rgba(0, 200, 0, 0.4)", "rgba(200, 200, 0, 0.4)",
        "rgba(0, 0, 200, 0.4)", "rgba(200, 0, 200, 0.4)", "rgba(200, 200, 200, 0.4)",
    ];
}

function eloDataset(bot, index, colors) {
    const chartData = [];
    let lastSecond = 0;
    let updated = false;
    for (const entry of bot.data) {
        if (entry.epochSecond > selectedRange.endSecond) break;
        if (entry.epochSecond < selectedRange.startSecond) continue;
        updated = updated || entry.updated || false;
        if (entry.epochSecond - lastSecond >= secondsToAggregate) {
            chartData.push({ x: new Date(entry.epochSecond * 1000), y: entry.rating, updated: updated });
            updated = false;
            lastSecond = entry.epochSecond;
        }
    }
    return {
        label: bot.name,
        fill: false,
        borderColor: colors[index],
        data: chartData,
        cubicInterpolationMode: "monotone",
        pointBackgroundColor: chartData.map(e => e.updated ? colors[index] : "transparent"),
        pointRadius: chartData.map(e => e.updated ? 5 : 3)
    };
}

const fetchEloHistory = name => fetch(statsBaseUrl + encodeURIComponent(name) + "/eloHistory.json")
    .then(r => r.json())
    .then(data => ({ name: name, data: data.sort((a, b) => a.epochSecond - b.epochSecond) }));

function setupEloChart(own, topBotNames) {
    const eloBots = [own];
    const chart = new Chart(document.getElementById("eloChart"), {
        type: "line",
        data: { datasets: [] },
        options: Object.assign({}, baseOptions, {
            scales: { xAxes: [{ type: "time", time: { unit: "day" } }] },
            tooltips: {
                mode: "nearest",
                callbacks: {
                    footer: (items, chartData) => chartData.datasets[items[0].datasetIndex].data[items[0].index].updated
                        ? "Updated version" : null
                }
            }
        })
    });
    function updateChart() {
        const colors = eloChartColors();
        chart.data.datasets = eloBots.map((bot, index) => eloDataset(bot, index, colors));
        applyThemeToChart(chart);
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
    registerChartUpdater(updateChart);

    // The comparison histories are ~2 MB together, so they load only on request.
    const compareNode = document.getElementById("eloCompare");
    const compareButton = (label, onClick) => html`<button class="compare" ?disabled=${!onClick} @click=${onClick}>${label}</button>`;
    render(compareButton("Compare with the top 5 bots", () => {
        render(compareButton("Loading…"), compareNode);
        Promise.all(topBotNames.map(fetchEloHistory)).then(others => {
            eloBots.push(...others);
            updateChart();
            render("", compareNode);
        }).catch(e => {
            console.error(e);
            render(html`<small>Could not load the other bots' ELO.</small>`, compareNode);
        });
    }), compareNode);
}

// --- Loading ---

const headerNode = document.getElementById("botHeader");

const rankingRequest = fetch(statsBaseUrl + "ranking.json").then(r => r.json());
const ownEloRequest = fetchEloHistory(botName);
// Handled where it is used; this only silences the error for unknown bots, whose header says so.
ownEloRequest.catch(() => {});

rankingRequest.then(bots => {
    for (const bot of bots) {
        if (bot.rank === "UNRANKED") bot.rank = "U";
        const inWindow = bot.wonInWindow + bot.lostInWindow;
        bot.win7 = inWindow > 0 ? bot.wonInWindow / inWindow : null;
        // The data uses Java's Instant.MIN (a huge negative number) for "unknown".
        bot.lastUpdated = bot.lastUpdated > 0 ? new Date(bot.lastUpdated * 1000) : null;
    }
    const me = bots.find(b => b.botName === botName);
    if (!me) {
        render(html`<h1>${botName || "No bot selected"}</h1><p>This bot is not in the ranking.</p>`, headerNode);
        return;
    }
    render(botHeader(me), headerNode);
    sortByRank(bots);
    const topBotNames = bots.filter(b => b.enabled && b.botName !== botName).slice(0, 5).map(b => b.botName);
    return ownEloRequest.then(own => {
        const maxElo = own.data.reduce((max, e) => Math.max(max, e.rating), 0);
        render(botHeader(me, maxElo), headerNode);
        setupEloChart(own, topBotNames);
    });
}).catch(e => {
    console.error(e);
    render(html`<p>Could not load this bot's data. Please try again later.</p>`, headerNode);
});

fetch(statsBaseUrl + encodeURIComponent(botName) + "/allGameResults.json")
    .then(r => {
        // Never evaluate an error page.
        if (!r.ok) throw new Error("allGameResults.json: HTTP " + r.status);
        return r.text();
    })
    .then(text => {
        // Not valid JSON (keys are unquoted), so it has to be evaluated; it comes from our own data server.
        const data = eval('(' + text + ')');
        data.results.forEach(d => {
            d.t = parseInt(d.t, 16) * 3600;
            d.e--;
        });
        data.results.sort((a, b) => a.t - b.t);
        if (data.results.length) firstGameSecond = data.results[0].t;
        setupPerMapWinsChart(data);
        setupVsChart(data);
        setupRaceMatchups(data);
        rankingRequest
            .then(bots => setupCloseOpposition(data, new Map(bots.map(b => [b.botName, b]))))
            .catch(() => setupCloseOpposition(data, new Map()));
        setupPerPlayLengthChart(data);
    }).catch(e => console.error(e));
