import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';
import tablesorter from 'tablesorter'
import $ from 'jquery'

const botBadge = (bot, game) => html`
<td class=${basil.racecol(bot.race) + (bot.randomBot ? "_random" : "")} style="line-height: 2em">
<span style="display: inline-block; width: 2em;" class=${basil.rankcol(bot.rank)}>${bot.rank}</span>
${bot.winner ? html`<i class="fas fa-trophy"></i>` : ""}${bot.crashed ? html`<i class="fas fa-car-crash"></i>`
        : bot.loser ? html`<i class="fas fa-sad-tear"></i>` : ""} 
<a class="normal" href="/bot.html?bot=${bot.name}">${bot.name}</a>
<div class="float-right normal">
${game.validGame ? html`
<a class="normal" href=${bot.replayUrl}><i class="fas fa-download"></i></a>
<a class="normal" href="http://www.openbw.com/replay-viewer/?rep=${bot.replayUrl}" target="_blank"><i class="fas fa-eye"></i></a>
` : ""}
<a class="normal" href="ranking.html#${bot.name}"><i class="fas fa-align-left"></i></a>
</div>
</td>
`;

const table = (games) => html`
    ${games.map(game => html`
        <tr>
        ${botBadge(game.botA, game)}
        ${botBadge(game.botB, game)}
        <td>${game.map}</td>
        <td class="nowrap">${game.time}</td>
        <td class="overlayed">${html`${game.notPlayed ? html`<i class="fas fa-poo-storm"</i> ` : ""}${game.realTimeout ? html`<i class="fas fa-fish"></i> ` : ""}${game.frameTimeout ? html`<i class="fas fa-stopwatch"></i> ` : ""}${game.gameTime}`}<div class="overlay">
        ${game.nukes > 1 ? html`<i class="tooltip fas fa-radiation"><span role="tooltip">${game.nukes} nukes</span></i>` : ""}
        ${game.mm >= 400 ? html`<i class="tooltip fas fa-biohazard"><span role="tooltip">${game.mm} M&M</span></i>` : ""}
        ${game.cruisercarrier >= 20 ? html`<i class="tooltip fas fa-fighter-jet"><span role="tooltip">${game.cruisercarrier} BC + Carrier</span></i>` : ""}
        ${game.arbiter >= 10 ? html`<i class="tooltip fas fa-user-secret"><span role="tooltip">${game.arbiter} Arbiter</span></i>` : ""}
        ${game.lurkers >= 100 ? html`<i class="tooltip fas fa-spider"><span role="tooltip">${game.lurkers} Lurkers</span></i>` : ""}
        ${game.queens >= 80 ? html`<i class="tooltip fas fa-chess-queen"><span role="tooltip">${game.queens} Queens</span></i>` : ""}
        ${game.guardians >= 10 ? html`<i class="tooltip fas fa-pastafarianism"><span role="tooltip">${game.guardians} Guardians</span></i>` : ""}
        ${game.defilers >= 10 ? html`<i class="tooltip fas fa-smog"><span role="tooltip">${game.defilers} Defilers</span></i>` : ""}
        </div></td>
        <!--
        <td>${game.validGame ? html`<a href="${game.replayUrl}"><i class="fas fa-download"></i></a>` : ""}</td>
        <td>${game.validGame ? html`<a href="http://www.openbw.com/replay-viewer/?rep=${game.replayUrl}" target="_blank">OpenBW</a>` : ""}</td>
        !-->
        </tr>
        `)}
    `;

function renderGameListing(options) {
    let filter = options && options.filter;
    axios.get("https://basilicum.bytekeeper.org/stats/games_24h.json", undefined, undefined, "text")
        .then(function (result) {
            let data = eval('(' + result.data + ')');
            let g = data.results;
            let maps = data.maps;
            let bots = data.bots;
            let games = [];
            for (let i = 0; i < g.length; i++) {
                let gameTime = undefined;
                if (g[i].fc) {
                    let secs = Math.round(g[i].fc / 24);
                    if (secs > 59) gameTime = Math.floor(secs / 60) + "m "; else gameTime = "";
                    gameTime += Math.ceil(secs % 60) + "s";
                }
                let [botA, botB] = [g[i].a, g[i].b].map(desc => {
                    let bot = bots[desc.b];
                    return {
                        name: bot.name,
                        winner: desc.w === 1,
                        loser: desc.l === 1,
                        crashed: desc.c === 1,
                        race: desc.r || bot.race,
                        randomBot: desc.r && desc.r !== bot.race,
                        rank: bot.rank
                    };
                });
                let map = maps[g[i].m];
                let events = g[i].ev && g[i].ev.reduce((acc, e) => {
                    let m = acc[e.u] = (acc[e.u] || {});
                    m[e.e] = e.c;
                    return acc;
                }, {}) || {};

                botA.replayUrl = "https://basilicum.bytekeeper.org/bots/" + botA.name + "/" + botA.name + " vs " + botB.name + " " + map + " " + g[i].h + ".rep";
                botB.replayUrl = "https://basilicum.bytekeeper.org/bots/" + botB.name + "/" + botB.name + " vs " + botA.name + " " + map + " " + g[i].h + ".rep";

                const game = {
                    botA: botA,
                    botB: botB,
                    time: basil.formatDateTime(parseInt(g[i].e, 16) * 60),
                    timestamp: parseInt(g[i].e, 16),
                    gameTime: gameTime,
                    notPlayed: !g[i].fc,
                    validGame: g[i].iv !== 1 && g[i].to != 1,
                    realTimeout: g[i].to === 1,
                    frameTimeout: g[i].fo === 1,
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
                if (!filter || filter(game)) {
                    games.push(game);
                }
            }
            // Required for index to work as expected
            games.sort(function (a, b) {
                return b.timestamp - a.timestamp;
            });
            render(table(games), document.querySelector("#gamesTable tbody"));
            $("#gamesTable").tablesorter({
                widgets: ["filter"],
                widgetOptions: {
                }
            });
        });
}

export default renderGameListing;