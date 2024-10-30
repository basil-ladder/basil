import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';
import tablesorter from 'tablesorter'
import $ from 'jquery'


const botBadge = (bot, game) => html`
<td class="botbadge ${basil.racecol(bot.race) + (bot.randomBot ? "_random" : "")}">
    <span class=${basil.rankcol(bot.rank)}>${bot.rank}</span>
    ${bot.winner ? html`<i class="fas fa-trophy"></i>` : ""}${bot.crashed ? html`<i class="fas fa-car-crash"></i>`
        : bot.loser ? html`<i class="fas fa-sad-tear"></i>` : ""}
    <a href="/bot.html?bot=${bot.name}">${bot.name}</a>
    <div class="float-right normal">
        ${game.validGame ? html`
        <a href=${bot.replayUrl}><i class="fas fa-download"></i></a>
        <!--<a href="http://www.openbw.com/replay-viewer/?rep=${bot.replayUrl}" target="_blank"><i-->
        <a href="https://bmnielsen.github.io/openbw-replay-viewer/?rep=${bot.replayUrl}" target="_blank"><i
                class="fas fa-eye"></i></a>
        ` : ""}
        <a href="ranking.html#${bot.name}"><i class="fas fa-align-left"></i></a>
    </div>
</td>
`;

const tableHeader = (rowStyle) => html`
<thead>
    <tr>
        <th style="width: 20em;">Bot</th>
        <th style="width: 20em;">Opponent</th>
        <th style="width: 16em;" class="filter-select">Map</th>
        <th style="width: 12em;" class="filter-false">Ended At</th>
        <th class="filter-false" data-sorter="false">Game time</th>
    </tr>
</thead>
`;

const table = (games, rowStyle) => html`
${tableHeader(rowStyle)}
<tbody>
    ${games.map(game => html`
    <tr>
        ${botBadge(game.botA, game)}
        ${botBadge(game.botB, game)}
        <td>${game.map}</td>
        <td class="nowrap">${game.time}</td>
        <td class="overlayed">${html`${game.notPlayed ? html`<i class="fas fa-poo-storm" </i> ` : ""}${game.realTimeout
        ? html`<i class="fas fa-fish"></i> ` : ""}${game.frameTimeout ? html`<i class="fas fa-stopwatch"></i> `
            : ""}${game.gameTime}`}<div class="overlay">
                ${game.nukes > 1 ? html`<i class="tooltip fas fa-radiation"><span role="tooltip">${game.nukes}
                        nukes</span></i>` : ""}
                ${game.mm >= 400 ? html`<i class="tooltip fas fa-biohazard"><span role="tooltip">${game.mm}
                        M&M</span></i>` : ""}
                ${game.cruisercarrier >= 20 ? html`<i class="tooltip fas fa-fighter-jet"><span
                        role="tooltip">${game.cruisercarrier} BC + Carrier</span></i>` : ""}
                ${game.arbiter >= 10 ? html`<i class="tooltip fas fa-user-secret"><span role="tooltip">${game.arbiter}
                        Arbiter</span></i>` : ""}
                ${game.lurkers >= 100 ? html`<i class="tooltip fas fa-spider"><span role="tooltip">${game.lurkers}
                        Lurkers</span></i>` : ""}
                ${game.queens >= 80 ? html`<i class="tooltip fas fa-chess-queen"><span role="tooltip">${game.queens}
                        Queens</span></i>` : ""}
                ${game.guardians >= 10 ? html`<i class="tooltip fas fa-pastafarianism"><span
                        role="tooltip">${game.guardians} Guardians</span></i>` : ""}
                ${game.defilers >= 10 ? html`<i class="tooltip fas fa-smog"><span role="tooltip">${game.defilers}
                        Defilers</span></i>` : ""}
            </div>
        </td>
        <!--
        <td>${game.validGame ? html`<a href="${game.replayUrl}"><i class="fas fa-download"></i></a>` : ""}</td>
        <td>${game.validGame ? html`<a href="https://bmnielsen.github.io/openbw-replay-viewer/?rep=${game.replayUrl}" target="_blank">OpenBW</a>` : ""}</td>
        !-->
    </tr>
    `)}
</tbody>
    `;

const srcUrl = basil.dataBaseUrl + "stats/games_24h.json";
const sourceRefText = document.querySelector("#srcRef");
if (sourceRefText) {
    const srcRef = html`Source: <a href=${srcUrl}>${srcUrl}</a>`;
    render(srcRef, sourceRefText);
}


function renderGameListing(options) {
    const filter = options && options.filter;
    const rowStyle = options && options.rowStyle;
    const hideTableSorter = options && options.hideTableSorter;
    axios.get(srcUrl)
        .then(function (result) {
            let data = result.data;
            let g = data.results;
            let maps = data.maps;
            let bots = data.bots;
            let games = [];
            for (let i = 0; i < g.length; i++) {
                const frameCount = g[i].frameCount;
                let gameTime = undefined;
                if (frameCount) {
                    let secs = Math.round(frameCount / 24);
                    if (secs > 59) gameTime = Math.floor(secs / 60) + "m "; else gameTime = "";
                    gameTime += Math.ceil(secs % 60) + "s";
                }
                let [botA, botB] = [g[i].botA, g[i].botB].map(botResult => {
                    let bot = bots[botResult.botIndex];
                    return {
                        name: bot.name,
                        winner: botResult.winner,
                        loser: botResult.loser,
                        crashed: botResult.crashed,
                        race: botResult.race || bot.race,
                        randomBot: botResult.race && botResult.race !== bot.race,
                        rank: bot.rank
                    };
                });
                let map = maps[g[i].mapIndex];
                let events = g[i].gameEvents && g[i].gameEvents.reduce((acc, e) => {
                    let m = acc[e.unit] = (acc[e.unit] || {});
                    m[e.event] = e.amount;
                    return acc;
                }, {}) || {};
                const gameHash = g[i].gameHash;
                botA.replayUrl = basil.dataBaseUrl + "bots/" + botA.name + "/" + botA.name + " vs " + botB.name + " " + map + " " + gameHash + ".rep";
                botB.replayUrl = basil.dataBaseUrl + "bots/" + botB.name + "/" + botB.name + " vs " + botA.name + " " + map + " " + gameHash + ".rep";

                const game = {
                    botA: botA,
                    botB: botB,
                    time: basil.formatDateTime(g[i].endedAt),
                    timestamp: g[i].endedAt,
                    gameTime: gameTime,
                    notPlayed: !frameCount,
                    validGame: !g[i].invalidGame && !g[i].realTimeout,
                    realTimeout: g[i].realTimeout,
                    frameTimeout: g[i].frameTimeout,
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
                games.push(game);
            }
            // Required for index to work as expected
            games.sort(function (a, b) {
                return b.timestamp - a.timestamp;
            });
            games = (!filter && games) || filter(games)
            render(table(games, rowStyle), document.querySelector("#gamesTable"));
            if (!hideTableSorter) {
                $("#gamesTable").tablesorter({
                    widgets: ["filter"],
                    widgetOptions: {
                    }
                });
            } else {
                $.tablesorter.destroy($("#gamesTable"), true);
            }
        });
}

export default renderGameListing;
