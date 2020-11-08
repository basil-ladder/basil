import axios from 'axios'
import { html, render } from 'lit-html';
import basil from './basil.js'

function mouseEnter(x, y) {
    if (x >= 0) {
        document.querySelector("tr:first-child th:nth-child(" + (x + 4) + ")")
            .classList.add("highlight");
    }
    document.querySelector("tbody tr:nth-child(" + (y + 1) + ")")
        .classList.add("highlight");
    document.querySelector("tbody tr:nth-child(" + (y + 1) + ") th:nth-child(2)")
        .classList.add("highlight");
}

function mouseLeave(x, y) {
    if (x >= 0) {
        document.querySelector("tr:first-child th:nth-child(" + (x + 4) + ")")
            .classList.remove("highlight");
    }
    document.querySelector("tbody tr:nth-child(" + (y + 1) + ")")
        .classList.remove("highlight");
    document.querySelector("tbody tr:nth-child(" + (y + 1) + ") th:nth-child(2)")
        .classList.remove("highlight");
}

const crossTable = (bots) => html`
<table class="tiny crosstable">
<thead>
<tr>
    <th style="max-width: 1em; min-width: 1em;">#</th>
    <th style="max-width: 12em; min-width: 12em;">Bot</th>
    <th style="max-width: 3em; min-width: 3em;">ELO</th>
    ${bots.map(bot => html`<th class="rot30 ${basil.racecol(bot.race)}"><div>${bot.name}</div></th>`)}
</tr>
</thead>
<tbody>
${bots.map((bot, index) => html`
<tr>
    <th>${index + 1}</th>
    <th @mouseenter=${e => mouseEnter(-2, index)} @mouseleave=${e => mouseLeave(-2, index)} class=${basil.racecol(bot.race)}>${bot.name}</th>
    <th @mouseenter=${e => mouseEnter(-1, index)} @mouseleave=${e => mouseLeave(-1, index)}>${bot.rating}</th>
    ${bot.row.map((col, i) => html`
        <td style="background-color: rgba(${col.color});" @mouseenter=${e => mouseEnter(i, index)} @mouseleave=${e => mouseLeave(i, index)}>
        ${col.self ? "" : html`<span>${col.won} - ${col.lost}</span>`}
        </td>
    `)}
</tr>`)}
</tbody>
</table>`;

const ranges = [null, 7, 14, 30, 60, 90, 120, 150, 180];

let refs = {
    minElo: 1000,
    maxElo: 4000,
    showEnabledOnly: true,
    range: null
}

const toggleEnabledOnly = {
    handleEvent() {
        refs.showEnabledOnly = !refs.showEnabledOnly;
        update();
    }
};

const form = html`
<form onsumbit="" style="margin-top: 1em;">
<input type="checkbox" id="enabledOnly" name="enabledOnly" @click=${toggleEnabledOnly} checked=${refs.showEnabledOnly}><label for="enabledOnly"> Show enabled only</label>
<label for="minElo" style="margin-left: 0.8em;">Min ELO</label><input style="width: 5em;" type="number" id="minElo" value=${refs.minElo} min="0" max="5000" @change=${e => { refs.minElo = e.target.value; update() }}>	
<label for="maxElo" style="margin-left: 0.8em;">Max ELO</label><input style="width: 5em;" type="number" id="maxElo" value=${refs.maxElo} min="0" max="5000" @change=${e => { refs.maxElo = e.target.value; update() }}>	
<label for="range" style="margin-left: 0.8em;">Range</label>
<select id="range" ref="range" @change=${e => { refs.range = e.target.value; update() }}>
    ${ranges.map(days => html`
        <option value=${days ? days : ""}>${days == null ? "everything" : days + " days"}</option>
    `)}
</select>
</form > `;

const formNode = document.querySelector("#form");
const crossTableNode = document.querySelector("#tablePlaceholder");

render(form, formNode);

function update() {
    let suffix = refs.range;
    if (!suffix) suffix = ""; else suffix = "_" + suffix;
    axios.get(basil.dataBaseUrl + "stats/botVsBot" + suffix + ".json")
        .then(result => {
            const recv = result.data;
            let stats = {};
            function entry(a, b) {
                if (!stats[a]) stats[a] = {};
                if (!stats[a][b]) stats[a][b] = { won: 0, lost: 0 };
                return stats[a][b];
            }
            for (let i = 0; i < recv.botinfos.length; i++) {
                for (let j = 0; j < recv.botinfos.length; j++) {
                    let wonGames = recv.botinfos[i].vsBotIdxWon[j];
                    let winner = recv.botinfos[i].name;
                    let loser = recv.botinfos[j].name;
                    entry(winner, loser).won = wonGames;
                    entry(loser, winner).lost = wonGames;
                }
            }
            let bots = recv.botinfos.map(function (a) {
                return {
                    name: a.name,
                    enabled: a.enabled,
                    rating: a.rating,
                    race: a.race
                };
            }).sort(function (a, b) {
                if (a.enabled && !b.enabled) return -1;
                if (!a.enabled && b.enabled) return 1;
                return b.rating - a.rating;
            });
            let relevantBots = bots.filter(function (bot) {
                return (!refs.showEnabledOnly || bot.enabled)
                    && (!bot.rating || bot.rating >= refs.minElo)
                    && (!bot.rating || bot.rating <= refs.maxElo);
            });
            bots = relevantBots.map(function (a) {
                a.row = relevantBots.map(function (b) {
                    let e = entry(a.name, b.name);
                    if (a === b) {
                        e.self = true;
                    } else {
                        let w = e.won + 1;
                        let l = e.lost + 1;
                        let red = l / w;
                        red = Math.min(1, red * red);
                        let green = w / l;
                        green = Math.min(1, green * green);
                        let blue = Math.min(green, red);
                        e.color = Math.ceil(255 * red) + "," + Math.ceil(255 * green) + "," + Math.ceil(255 * blue) + ",0.8";
                    }
                    return e;
                });
                return a;
            });
            render(crossTable(bots), crossTableNode);
        });
}

update();

/*
let cells = $(crosstable.root).find("td");
cells.on("mouseover", function () {
    $(this).closest("table").find("td:nth-child(" + ($(this).index() + 1) + ")").addClass("highlight");
});
cells.on("mouseout", function () {
    $(this).closest("table").find("td:nth-child(" + ($(this).index() + 1) + ")").removeClass("highlight");
});
*/

