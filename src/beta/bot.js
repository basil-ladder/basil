import basil from './basil.js'
import Chart from 'chart.js'
import { icon, racecol, racename, rankcol, sortByRank } from './common.js'
import { html, render } from 'lit-html'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import renderGameListing from './gamelisting';

const statsBaseUrl = basil.dataBaseUrl + "stats/";
const botName = new URLSearchParams(location.search).get("bot") || "";
// Narrow screens get horizontal bars and no in-bar labels; they did not fit.
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
    <span class="race-name">${racename(bot.race)}</span>
</div>
<div class="bot-stats">
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

const CSS_COLOR_NAMES = ["Black", "Blue", "BlueViolet", "Brown", "BurlyWood", "CadetBlue", "Chartreuse", "Chocolate", "Coral", "CornflowerBlue", "Crimson", "Cyan", "DarkBlue", "DarkCyan", "DarkGoldenRod", "DarkGray", "DarkGreen", "DarkKhaki", "DarkMagenta", "DarkOliveGreen", "DarkOrange", "DarkOrchid", "DarkRed", "DarkSalmon", "DarkSeaGreen", "DarkSlateBlue", "DarkSlateGrey", "DarkTurquoise", "DarkViolet", "DeepPink", "DeepSkyBlue", "DimGrey", "DodgerBlue", "FireBrick", "ForestGreen", "Fuchsia", "Gainsboro", "Gold", "GoldenRod", "Grey", "Green", "GreenYellow", "HotPink", "IndianRed", "Indigo", "Ivory", "Khaki", "Lavender", "LawnGreen", "LemonChiffon", "LightBlue", "LightCoral", "LightCyan", "LightGoldenRodYellow", "LightGreen", "LightPink", "LightSalmon", "LightSeaGreen", "LightSkyBlue", "LightSlateGray", "LightSlateGrey", "LightSteelBlue", "Lime", "LimeGreen", "Linen", "Magenta", "Maroon", "MediumAquaMarine", "MediumBlue", "MediumOrchid", "MediumPurple", "MediumSeaGreen", "MediumSlateBlue", "MediumSpringGreen", "MediumTurquoise", "MediumVioletRed", "MidnightBlue", "MistyRose", "Moccasin", "Navy", "Olive", "OliveDrab", "Orange", "OrangeRed", "Orchid", "PaleGoldenRod", "PaleGreen", "PaleTurquoise", "PaleVioletRed", "PeachPuff", "Peru", "Pink", "Plum", "PowderBlue", "Purple", "RebeccaPurple", "Red", "RosyBrown", "RoyalBlue", "SaddleBrown", "Salmon", "SandyBrown", "SeaGreen", "Sienna", "Silver", "SkyBlue", "SlateBlue", "SlateGray", "SlateGrey", "SpringGreen", "SteelBlue", "Tan", "Teal", "Thistle", "Tomato", "Turquoise", "Violet", "Wheat", "Yellow", "YellowGreen"];
const DARK_INVISIBLE = { "black": 1, "navy": 1, "darkblue": 1, "midnightblue": 1 };
function getLineColor(i) {
    const color = CSS_COLOR_NAMES[i % CSS_COLOR_NAMES.length];
    if (basil.chartThemeColors().isDark && DARK_INVISIBLE[color.toLowerCase()]) return "White";
    return color;
}

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
        basil.applyThemeToChart(chart);
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
    basil.registerChartUpdater(updateChart);
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
    basil.registerChartUpdater(updateChart);
}

function aggWinLossFilter(data) {
    const steplen = Math.ceil(data.length / 30);
    const result = [];
    let step = 0;
    let y = 0;
    for (let i = 0; i < data.length; i++) {
        const d = data[i];
        y += d.y;
        step++;
        if (step === steplen || i === data.length - 1) {
            result.push({ x: d.x, y: Math.round(100 * y / step) / 100, w: d.w, l: d.l });
            y = 0;
            step = 0;
        }
    }
    return result;
}

function winRateLineChart(canvasId) {
    return new Chart(document.getElementById(canvasId), {
        type: "line",
        options: Object.assign({}, baseOptions, {
            legend: { labels: { boxWidth: narrow ? 12 : 40 } },
            tooltips: {
                callbacks: {
                    footer: (items, chartData) => {
                        const e = chartData.datasets[items[0].datasetIndex].data[items[0].index];
                        return "W-L: " + e.w + " - " + e.l;
                    }
                }
            },
            scales: {
                xAxes: [{ type: "time", time: { unit: "day" } }],
                yAxes: [{ ticks: { callback: value => value + "%" } }]
            }
        })
    });
}

function setupRaceMatchupChart(data) {
    const chart = winRateLineChart("perRaceWinsChart");
    function updateChart() {
        const rvr = {};
        const races = ["T", "P", "Z", "R", "A"];
        const raceLabel = { "T": "Terran", "P": "Protoss", "R": "Unknown", "Z": "Zerg", "A": "Overall" };
        races.forEach(a => races.forEach(b => {
            rvr[a + b] = { label: raceLabel[a] + " vs " + raceLabel[b], won: 0, lost: 0, data: [] };
        }));
        const count = (key, x) => {
            const s = rvr[key];
            if (x.w === 1) s.won++; else s.lost++;
            s.data.push({ x: new Date(x.t * 1000), y: 100 * s.won / (s.won + s.lost), w: s.won, l: s.lost });
        };
        data.results.filter(inRange).forEach(x => {
            count(x.r + x.eR, x);
            count("A" + x.eR, x);
            count(x.r + "A", x);
        });
        // An "Overall" line equal to a single race's line adds nothing.
        const rvrList = [];
        races.forEach(a => races.forEach(b => {
            const s = rvr[a + b];
            const same = other => rvr[other + b].won === s.won && rvr[other + b].lost === s.lost;
            if (a !== "A" || !["T", "P", "Z", "R"].some(same)) rvrList.push(s);
        }));
        chart.data.datasets = rvrList.filter(r => r.data.length > 0).map((r, i) => ({
            label: r.label,
            data: aggWinLossFilter(r.data),
            fill: false,
            borderColor: getLineColor(i),
            cubicInterpolationMode: "monotone",
        }));
        basil.applyThemeToChart(chart);
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
    basil.registerChartUpdater(updateChart);
}

function setupBotMatchupChart(data) {
    const chart = winRateLineChart("vs2Chart");
    function updateChart() {
        const vs = {};
        data.results.filter(inRange).forEach(x => {
            const s = vs[x.e] = (vs[x.e] || { won: 0, lost: 0, data: [], label: data.bots[x.e] });
            if (x.w === 1) s.won++; else s.lost++;
            s.data.push({ x: new Date(x.t * 1000), y: 100 * s.won / (s.won + s.lost), w: s.won, l: s.lost });
        });
        const closest = Object.values(vs)
            .sort((a, b) => Math.abs(a.won / (a.won + a.lost) - 0.5) - Math.abs(b.won / (b.won + b.lost) - 0.5))
            .slice(0, 10);
        chart.data.datasets = closest.map((r, i) => ({
            label: r.label,
            data: aggWinLossFilter(r.data),
            fill: false,
            borderColor: getLineColor(i),
            cubicInterpolationMode: "monotone",
        }));
        basil.applyThemeToChart(chart);
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
    basil.registerChartUpdater(updateChart);
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
        basil.applyThemeToChart(chart);
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
    basil.registerChartUpdater(updateChart);
}

// --- ELO chart ---

function eloChartColors() {
    return basil.chartThemeColors().isDark ? [
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
        basil.applyThemeToChart(chart);
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
    basil.registerChartUpdater(updateChart);

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
    .then(r => r.text())
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
        setupRaceMatchupChart(data);
        setupBotMatchupChart(data);
        setupPerPlayLengthChart(data);
    }).catch(e => console.error(e));
