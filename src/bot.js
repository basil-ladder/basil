import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html'
import flatpickr from 'flatpickr'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import renderGameListing from './gamelisting';
import stringify from 'csv-stringify/lib/sync'

const botNameAndRank = (bot) => html`
<h1 style="font-size: 1.5em; line-height: 1.5em; margin: 0em;" class=${bot.race ?
        basil.racecol(bot.race) : ""}>
    <span style="display: inline-block; width: 3em;" class="${bot.rank ? basil.rankcol(bot.rank) : ""}">${bot.rank || ""}</span>
    ${bot.botName} 
    <span class="float-right">${basil.racename && bot.race ? basil.racename(bot.race) : "-"}</span ></h1>
    `;

const statsSection = (bot) => html`
<div class="row">
<div class="col-6 row">
<div class="col-1">
<i class="fas fa-trophy tooltip space-right"><span role="tooltip">Wins</span></i>
${bot.won}
</div>
<div class="col-1">
<i class="fas fa-sad-tear tooltip space-right"><span role="tooltip">Losses</span></i>
${bot.lost}
</div>
<div class="col-1">
<i class="fas fa-car-crash tooltip space-right"><span role="tooltip">Crashes</span></i>
${bot.crashed}
</div>
</div>
</div>
<div class="row">
<div class="col-1">
Last updated:
</div>
<div class="col-1">
${basil.formatDate(bot.lastUpdated)}
</div>
</div>
<div class="row">
<div class="col-1">
ELO
</div>
<div class="col-1">
${bot.basilRating || bot.rating}
</div>
</div>
<div class="row">
<div class="col-1">
max-ELO
</div>
<div class="col-1">
${bot.maxElo}
</div>
</div>
<div class="row">
<div class="col-1">
Win rate
</div>
<div class="col-1">
${(bot.won + bot.lost > 0) ? basil.percentFormat(bot.won / (bot.won + bot.lost)) : ""}
</div>
</div>
</div>
`;

const header = (bot) => html`
${bot.won ? statsSection(bot) : ""}
${!bot.enabled ? html`
<div class="row">
<div class="col-1">
Disabled:
</div>
<div class="col-3">
${bot.disabledReason}
</div>
</div>
`: ""}`;

const CSS_COLOR_NAMES = ["Black", "Blue", "BlueViolet", "Brown", "BurlyWood", "CadetBlue", "Chartreuse", "Chocolate", "Coral", "CornflowerBlue", "Crimson", "Cyan", "DarkBlue", "DarkCyan", "DarkGoldenRod", "DarkGray", "DarkGreen", "DarkKhaki", "DarkMagenta", "DarkOliveGreen", "DarkOrange", "DarkOrchid", "DarkRed", "DarkSalmon", "DarkSeaGreen", "DarkSlateBlue", "DarkSlateGrey", "DarkTurquoise", "DarkViolet", "DeepPink", "DeepSkyBlue", "DimGrey", "DodgerBlue", "FireBrick", "ForestGreen", "Fuchsia", "Gainsboro", "Gold", "GoldenRod", "Grey", "Green", "GreenYellow", "HotPink", "IndianRed", "Indigo", "Ivory", "Khaki", "Lavender", "LawnGreen", "LemonChiffon", "LightBlue", "LightCoral", "LightCyan", "LightGoldenRodYellow", "LightGreen", "LightPink", "LightSalmon", "LightSeaGreen", "LightSkyBlue", "LightSlateGray", "LightSlateGrey", "LightSteelBlue", "Lime", "LimeGreen", "Linen", "Magenta", "Maroon", "MediumAquaMarine", "MediumBlue", "MediumOrchid", "MediumPurple", "MediumSeaGreen", "MediumSlateBlue", "MediumSpringGreen", "MediumTurquoise", "MediumVioletRed", "MidnightBlue", "MistyRose", "Moccasin", "Navy", "Olive", "OliveDrab", "Orange", "OrangeRed", "Orchid", "PaleGoldenRod", "PaleGreen", "PaleTurquoise", "PaleVioletRed", "PeachPuff", "Peru", "Pink", "Plum", "PowderBlue", "Purple", "RebeccaPurple", "Red", "RosyBrown", "RoyalBlue", "SaddleBrown", "Salmon", "SandyBrown", "SeaGreen", "Sienna", "Silver", "SkyBlue", "SlateBlue", "SlateGray", "SlateGrey", "SpringGreen", "SteelBlue", "Tan", "Teal", "Thistle", "Tomato", "Turquoise", "Violet", "Wheat", "Yellow", "YellowGreen"];

let statsBaseUrl = basil.dataBaseUrl + "stats/";
let botName = decodeURIComponent(window.location.search.substring(1).split("=")[1]);
renderGameListing({
    rowStyle: "position: sticky; top: 2em; z-index: 900;",
    filter: games => games.filter(game => game.botA.name === botName || game.botB.name == botName)
});
let myData;

const botNameNode = document.querySelector("#botName");
const botDataNode = document.querySelector("#botData");
const initialBot = { botName: botName };
render(botNameAndRank(initialBot), botNameNode);
render(header(initialBot), botDataNode);
let now = Date.now() / 1000;
let selectedRange = { startSecond: now - 30 * 86400, endSecond: now };
let secondsToAggregate = (selectedRange.endSecond - selectedRange.startSecond) / 60;
let updaters = [];

flatpickr("#startDate", {
    defaultDate: new Date(selectedRange.startSecond * 1000),
    onChange: function (selectedDates) {
        selectedRange.startSecond = selectedDates[0].getTime() / 1000;
        secondsToAggregate = (selectedRange.endSecond - selectedRange.startSecond) / 60;
        updaters.forEach(function (x) { x.apply(); });
    }
});

flatpickr("#endDate", {
    defaultDate: new Date(selectedRange.endSecond * 1000),
    onChange: function (selectedDates) {
        selectedRange.endSecond = selectedDates[0].getTime() / 1000 + 86400;
        secondsToAggregate = (selectedRange.endSecond - selectedRange.startSecond) / 60;
        updaters.forEach(function (x) { x.apply(); });
    }

});
axios.get(statsBaseUrl + "ranking.json")
    .then(function (response) {
        let data = response.data;
        basil.sortByRank(data);

        let topBots = data.slice(0, 5).map(function (bot) { return bot.botName; });
        let myIndex = topBots.indexOf(botName);
        if (myIndex === -1) topBots.unshift(botName);
        else {
            let tmp = topBots[0];
            topBots[0] = topBots[myIndex];
            topBots[myIndex] = tmp;
        }
        myData = data.filter(function (a) { return a.botName === botName; })[0];
        myData.maxElo = "-";
        if (!myData.enabled) myData.disabledReason = myData.disabledReason || "Disabled on SSCAIT/locally";
        render(botNameAndRank(myData), botNameNode);
        render(header(myData), botDataNode);
        return Promise.all(topBots.map(bot =>
            axios.get(statsBaseUrl + bot + "/eloHistory.json")
                .then(function (response) { let data = response.data; return { name: bot, data: data }; }
                )));
    }).then(function (bots) {
        const colors = [
            "rgba(200, 0, 0, 0.8)",
            "rgba(0, 200, 0, 0.4)",
            "rgba(200, 200, 0, 0.4)",
            "rgba(0, 0, 200, 0.4)",
            "rgba(200, 0, 200, 0.4)",
            "rgba(200, 200, 200, 0.4)",
        ];
        let maxElo = 0;
        let myElos = bots[0].data;
        for (let i = 0; i < myElos.length; i++) {
            maxElo = Math.max(maxElo, myElos[i].rating);
        }

        myData.maxElo = maxElo;
        render(botNameAndRank(myData), botNameNode);
        render(header(myData), botDataNode);

        function prepareDataSets() {
            let startSecond = selectedRange.startSecond;
            let endSecond = selectedRange.endSecond;
            if (typeof startSecond === "undefined" || typeof endSecond === "undefined" || startSecond >= endSecond)
                return { label: unescape(bot.name), fill: false, data: [] };
            return bots.map(function (bot, index) {
                function filter(data) {
                    data.sort(function (a, b) { return a.epochSecond - b.epochSecond; });
                    let chartData = [];
                    let lastSecond = 0;
                    let updated = false;
                    for (let i = 0; i < data.length && lastSecond < endSecond; i++) {
                        let entry = data[i];
                        if (entry.epochSecond >= startSecond && entry.epochSecond <= endSecond) {
                            updated |= entry.updated || false;
                            if (entry.epochSecond - lastSecond >= secondsToAggregate) {
                                chartData.push({ x: new Date(entry.epochSecond * 1000), y: entry.rating, updated: updated });
                                updated = false;
                                lastSecond = entry.epochSecond;
                            }
                        }
                    }
                    return chartData;
                }
                let data = filter(bot.data);
                return {
                    label: unescape(bot.name),
                    fill: false,
                    borderColor: colors[index],
                    data: data,
                    cubicInterpolationMode: "monotone",
                    pointBackgroundColor: data.map(e => e.updated ? colors[index] : 'transparent'),
                    pointRadius: data.map(e => e.updated ? 5 : 3)
                };
            });
        }

        let datasets = prepareDataSets();
        let ctx = document.getElementById("eloChart");
        let eloChart = new Chart(ctx, {
            type: "line",
            data: {
                datasets: datasets
            },
            options: {
                scales: {
                    xAxes: [{
                        type: "time",
                        time: {
                            unit: "day"
                        }
                    }]
                },
                tooltips: {
                    mode: "nearest",
                    callbacks: {
                        footer: function (tooltipItems, data) {
                            const item = tooltipItems[0];
                            const updated = data.datasets[item.datasetIndex].data[item.index].updated;
                            let elo = item.yLabel;
                            return updated ? "Updated version" : null;
                        }
                    }

                }
            }
        });
        updaters.push(function () {
            eloChart.data.datasets = prepareDataSets();
            eloChart.update();
        });
    });
function setupPerMapWinsChart(data) {
    let winCharts = new Chart(document.getElementById("perMapWinsChart"), {
        type: "bar",
        plugins: [ChartDataLabels],
        options: {
            plugins: {
                datalabels: {
                    align: "top",
                    formatter: function (value, context) {
                        let wins = context.chart.data.datasets[0].data[context.dataIndex];
                        let losses = context.chart.data.datasets[1].data[context.dataIndex];
                        let wr = wins / Math.max(1, wins + losses);
                        return basil.percentFormat(wr, 1);
                    }
                }
            },
            tooltips: {
                mode: "index",
                callbacks: {
                    footer: function (tooltipItems, data) {
                        let wins = tooltipItems[0].yLabel;
                        let losses = tooltipItems[1].yLabel;
                        let wr = wins / Math.max(1, wins + losses);
                        return "WR: " + basil.percentFormat(wr);
                    }
                }
            }
        }
    });
    function updateChart() {
        let map = data.maps.map(function (x) {
            return { won: 0, lost: 0, map: x };
        });
        data.results.forEach(function (x) {
            let epochSecond = x.t;
            if (epochSecond >= selectedRange.startSecond && epochSecond <= selectedRange.endSecond) {
                let stat = map[x.m];
                if (x.w === 1) stat.won++; else stat.lost++;
            }
        }, {});
        map = map.filter(function (x) { return x.lost + x.won > 0; })
            .sort(function (a, b) {
                let dWR = a.won / Math.max(1, a.won + a.lost) - b.won / Math.max(1, b.won + b.lost);
                if (dWR != 0) return dWR;
                if (a.lost > b.lost) return -1;
                if (a.lost < b.lost) return 1;
                return 0;
            });
        let wonData = map.map(function (a) { return a.won; });
        let lostData = map.map(function (a) { return a.lost; });
        let labels = map.map(function (a) { return a.map; });
        winCharts.data.datasets = [{
            label: "Won",
            data: wonData,
            backgroundColor: "#88AAEB",
        }, {
            label: "Lost",
            data: lostData,
            backgroundColor: "#EBAAAA",
            datalabels: {
                display: false
            }
        }];
        winCharts.options.scales = {
            xAxes: [{
                stacked: true,
                type: "category",
                labels: labels,
                ticks: {
                    autoSkip: false
                }
            }],
            yAxes: [{
                stacked: true
            }]
        };

        winCharts.update();
    }
    updateChart();
    updaters.push(updateChart);
}
function setupVsChart(data) {
    let vsChart = new Chart(document.getElementById("vsChart"), {
        type: "bar",
        plugins: [ChartDataLabels,
            {
                beforeEvent: function (chartInstance, chartEvent) {
                    var xAxis = chartInstance.scales['x-axis-0'];

                    // If mouse is over the legend, change cursor style to pointer, else don't show it
                    var x = chartEvent.x;
                    var y = chartEvent.y;

                    if (chartEvent.type === 'click' &&
                        x <= xAxis.right && x >= xAxis.left &&
                        y <= xAxis.bottom && y >= xAxis.top) {
                        // category scale returns index here for some reason
                        var index = xAxis.getValueForPixel(x);
                        console.log(chartInstance.data.labels[index]);
                    }
                }
            }],
        options: {
            plugins: {
                datalabels: {
                    rotation: -85,
                    font: {
                        size: 10
                    },
                    align: "top",
                    formatter: function (value, context) {
                        let wins = context.chart.data.datasets[0].data[context.dataIndex];
                        let losses = context.chart.data.datasets[1].data[context.dataIndex];
                        let wr = wins / Math.max(1, wins + losses);
                        return basil.percentFormat(wr, 1);
                    },
                }
            },
            tooltips: {
                mode: "index",
                callbacks: {
                    footer: function (tooltipItems, data) {
                        let wins = tooltipItems[0].yLabel;
                        let losses = tooltipItems[1].yLabel;
                        let wr = wins / Math.max(1, wins + losses);
                        return "WR: " + basil.percentFormat(wr);
                    }
                }
            }
        }
    });
    function updateChart() {
        let bots = data.bots.map(function (x) {
            return { won: 0, lost: 0, bot: x };
        });
        data.results.forEach(function (x) {
            let epochSecond = x.t;
            if (epochSecond >= selectedRange.startSecond && epochSecond <= selectedRange.endSecond) {
                let stat = bots[x.e];
                if (x.w === 1) stat.won++; else stat.lost++;
            }
        }, {});
        bots = bots.filter(function (x) { return x.won + x.lost > 0; });
        bots = bots.sort(function (a, b) {
            let dWR = a.won / (a.won + a.lost) - b.won / (b.won + b.lost);
            if (dWR != 0) return dWR;
            if (a.lost > b.lost) return -1;
            if (a.lost < b.lost) return 1;
            return 0;
        })
        const csv = stringify(bots, { columns: ['bot', 'won', 'lost'], header: true });
        bots = bots.slice(0, 70);
        const vsChartData = html`
            <a download="top_contenders.csv" target="_blank" href="data:text/csv;charset=utf-8,${encodeURI(csv)}"><small>Download
                    as CSV</small></a>`;
        render(vsChartData, document.getElementById("vsChartData"));
        let wonData = bots.map(function (a) { return a.won; });
        let lostData = bots.map(function (a) { return a.lost; });
        let labels = bots.map(function (x) { return x.bot; });
        vsChart.data.datasets = [{
            label: "Won",
            data: wonData,
            backgroundColor: "#88AAEB"
        }, {
            label: "Lost",
            data: lostData,
            backgroundColor: "#EBAAAA",
            datalabels: {
                display: false
            }
        }];
        vsChart.options.scales = {
            xAxes: [{
                stacked: true,
                type: "category",
                labels: labels,
                ticks: {
                    autoSkip: false
                }
            }],
            yAxes: [{
                stacked: true
            }]
        };
        vsChart.update();
    }
    updateChart();
    updaters.push(updateChart);
}
function aggWinLossFilter(data) {
    let steplen = Math.ceil(data.length / 30);
    let step = 0;
    let result = [];
    let y = 0;
    for (let i = 0; i < data.length; i++) {
        let d = data[i];
        y += d.y;
        step++;
        if (step == steplen || i == data.length - 1) {
            result.push({ x: d.x, y: Math.round(100 * y / step) / 100, w: d.w, l: d.l });
            y = 0;
            step = 0;
        }
    }
    return result;
}
function setupRaceMatchupChart(data) {
    let chart = new Chart(document.getElementById("perRaceWinsChart"), {
        type: "line",
        options: {
            tooltips: {
                callbacks: {
                    footer: function (tooltipItem, data) {
                        let e = data.datasets[tooltipItem[0].datasetIndex].data[tooltipItem[0].index];
                        return "W-L: " + e.w + " - " + e.l;
                    }
                }
            },
            scales: {
                xAxes: [{
                    type: "time",
                    time: {
                        unit: "day"
                    }
                }],
                yAxes: [{
                    ticks: {
                        callback: function (value, index, values) {
                            return value + "%";
                        }
                    }
                }]
            }
        }
    });
    function updateChart() {
        let rvr = {};
        let races = ["T", "P", "Z", "R", "A"];
        let raceLabel = { "T": "Terran", "P": "Protoss", "R": "Unknown", "Z": "Zerg", "A": "Overall" };
        races.forEach(function (a, i) {
            races.forEach(function (b, j) {
                rvr[a + b] = { label: raceLabel[a] + " vs " + raceLabel[b], won: 0, lost: 0, data: [], a: i, b: j };
            });
        });
        data.results.forEach(function (x) {
            let epochSecond = x.t;
            if (epochSecond >= selectedRange.startSecond && epochSecond <= selectedRange.endSecond) {
                let stat = rvr[x.r + x.eR];
                if (x.w === 1) stat.won++; else stat.lost++;
                if (stat.won + stat.lost > 0) {
                    stat.data.push({ x: new Date(epochSecond * 1000), y: 100 * stat.won / (stat.won + stat.lost), w: stat.won, l: stat.lost });
                }
                stat = rvr["A" + x.eR];
                if (x.w === 1) stat.won++; else stat.lost++;
                if (stat.won + stat.lost > 0) {
                    stat.data.push({ x: new Date(epochSecond * 1000), y: 100 * stat.won / (stat.won + stat.lost), w: stat.won, l: stat.lost });
                }
                stat = rvr[x.r + "A"];
                if (x.w === 1) stat.won++; else stat.lost++;
                if (stat.won + stat.lost > 0) {
                    stat.data.push({ x: new Date(epochSecond * 1000), y: 100 * stat.won / (stat.won + stat.lost), w: stat.won, l: stat.lost });
                }
            }
        }, {});
        let rvrList = [];
        races.forEach(function (a) {
            races.forEach(function (b) {
                let stat = rvr[a + b];
                if (a != "A" || rvr["T" + b].won != stat.won && rvr["P" + b].won != stat.won && rvr["Z" + b].won != stat.won && rvr["R" + b].won != stat.won
                    || rvr["T" + b].lost != stat.lost && rvr["P" + b].lost != stat.lost && rvr["Z" + b].lost != stat.lost && rvr["R" + b].lost != stat.lost) {
                    rvrList.push(stat);
                }
            });
        });

        chart.data.datasets = rvrList
            .filter(r => r.data.length > 0)
            .map((r, i) => {
                return {
                    label: r.label,
                    data: aggWinLossFilter(r.data),
                    fill: false,
                    borderColor: CSS_COLOR_NAMES[i],
                    cubicInterpolationMode: "monotone",
                }
            });
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
}
function setupBotMatchupChart(data) {
    let chart = new Chart(document.getElementById("vs2Chart"), {
        type: "line",
        options: {
            tooltips: {
                callbacks: {
                    footer: function (tooltipItem, data) {
                        let e = data.datasets[tooltipItem[0].datasetIndex].data[tooltipItem[0].index];
                        return "W-L: " + e.w + " - " + e.l;
                    }
                }
            },
            scales: {
                xAxes: [{
                    type: "time",
                    time: {
                        unit: "day"
                    }
                }],
                yAxes: [{
                    ticks: {
                        callback: function (value, index, values) {
                            return value + "%";
                        }
                    }
                }]
            }
        }
    });
    function updateChart() {
        let vs = {};
        data.results.forEach(function (x) {
            let epochSecond = x.t;
            if (epochSecond >= selectedRange.startSecond && epochSecond <= selectedRange.endSecond) {
                let stat = vs[x.e] = (vs[x.e] || { won: 0, lost: 0, data: [], label: data.bots[x.e] });
                if (x.w === 1) stat.won++; else stat.lost++;
                if (stat.won + stat.lost > 0) {
                    stat.data.push({ x: new Date(epochSecond * 1000), y: 100 * stat.won / (stat.won + stat.lost), w: stat.won, l: stat.lost, label: stat.label });
                }
            }
        }, {});
        let relevantSet = Object.values(vs)
            .filter(r => r.data.length > 0);
        relevantSet.sort((a, b) => Math.abs(a.won / (a.won + a.lost) - 0.5) - Math.abs(b.won / (b.won + b.lost) - 0.5));
        relevantSet = relevantSet.slice(0, 10);

        chart.data.datasets = relevantSet
            .map((r, i) => {
                return {
                    label: r.label,
                    data: aggWinLossFilter(r.data),
                    fill: false,
                    borderColor: CSS_COLOR_NAMES[i],
                    cubicInterpolationMode: "monotone",
                }
            });
        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
}

function setupPerPlayLengthChart(data) {
    let chart = new Chart(document.getElementById("gameLenChart"), {
        type: "bar",
        plugins: [ChartDataLabels],
        options: {
            plugins: {
                datalabels: {
                    align: "top",
                    formatter: function (value, context) {
                        let wins = context.chart.data.datasets[0].data[context.dataIndex];
                        let losses = context.chart.data.datasets[1].data[context.dataIndex];
                        let wr = wins / Math.max(1, wins + losses);
                        return wr > 0.0 && wr < 1.0 ? basil.percentFormat(wr, 1) : "";
                    }
                }
            },
            tooltips: {
                mode: "index",
                callbacks: {
                    footer: function (tooltipItems, data) {
                        let wins = tooltipItems[0].yLabel;
                        let losses = tooltipItems[1].yLabel;
                        let wr = wins / Math.max(1, wins + losses);
                        return "WR: " + basil.percentFormat(wr);
                    }
                }
            }
        }
    });
    function updateChart() {
        let chartData = new Array(60);
        for (let i = 0; i < chartData.length; i++) {
            chartData[i] = {won: 0, lost: 0};
        }
        data.results.forEach(function (x) {
            let epochSecond = x.t;
            if (epochSecond >= selectedRange.startSecond && epochSecond <= selectedRange.endSecond) {
                if (x.fc && x.fc < 60 * 1429) {
                    let stat = chartData[Math.trunc(x.fc / 1429)];
                    if (x.w === 1) stat.won++; else stat.lost++;
                }
            }
        }, {});
        let wonData = chartData.map(function (a) { return a.won; });
        let lostData = chartData.map(function (a) { return a.lost; });
        let labels = chartData.map((_, i) => i);
        chart.data.datasets = [{
            label: "Won",
            data: wonData,
            backgroundColor: "#88AAEB",
            datalabels: {
                rotation: -85,
                font: {
                    size: 10
                }
            }
        }, {
            label: "Lost",
            data: lostData,
            backgroundColor: "#EBAAAA",
            datalabels: {
                display: false
            }
        }];
        chart.options.scales = {
            xAxes: [{
                stacked: true,
                type: "category",
                labels: labels,
                ticks: {
                    autoSkip: false
                }
            }],
            yAxes: [{
                stacked: true
            }]
        };

        chart.update();
    }
    updateChart();
    updaters.push(updateChart);
}

axios.get(statsBaseUrl + botName + "/allGameResults.json", undefined, undefined, "text")
    .then(function (response) {
        let data = eval('(' + response.data + ')');
        data.results.forEach(d => {
            d.t = parseInt(d.t, 16) * 3600;
            d.e--;
        });
        data.results.sort((a, b) => a.t - b.t);
        setupPerMapWinsChart(data);
        setupVsChart(data);
        setupRaceMatchupChart(data);
        setupBotMatchupChart(data);
	setupPerPlayLengthChart(data);
    });
