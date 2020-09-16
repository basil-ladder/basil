import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';
import tablesorter from 'tablesorter'
import $ from 'jquery'

const tableNode = document.querySelector("#rankingTable");

const activeBotRow = (bot, index) => html`
<tr>
<th>
    ${index + 1}
</th>
<td name=${bot.anchorName} class="${basil.rankcol(bot.rank)}">${bot.rank}</td>
<td class=${basil.racecol(bot.race)}><a class="normal" href="bot.html?bot=${bot.botName}" target="_blank">${bot.botName}<div class="float-right"><i class="fas fa-chart-line"></i></div></a></td>
    <td>${bot.rating ? html`${bot.basilRating || bot.rating}` : html`<small class="tiny">pending</small>`}</td>
<td>${bot.played}</td>
<td>${bot.won}</td>
<td>${bot.lost}</td>
<td>${bot.winRate}</td>
<td>${bot.crashesSinceUpdate}</td>
<td>${bot.lastUpdated}</td>
</tr>`;

const disabledBotRow = (bot, index) => html`
<tr
<th>
    ${index + 1}
</th>
<td name=${bot.anchorName} class="${basil.racecol(bot.race)}"><a class="normal" href="bot.html?bot=${bot.botName}" target="_blank">${bot.botName}<div class="float-right"><i class="fas fa-chart-line"></i></div></a></td>
    <td>${bot.rating ? html`${bot.rating}` : html`<small class="tiny">pending</small>`}</td>
<td>${bot.played}</td>
<td>${bot.won}</td>
<td>${bot.lost}</td>
<td>${bot.winRate}</td>
<td>${bot.crashed}</td>
<td><small>${bot.disabledReason}</small></td>
</tr>`;

const rankingTable = (data) => html`
<table>
<thead>
<tr>
<th>#</th>
<th data-sorter="false">Rank</th>
<th>Bot</th>
<th>ELO</th>
<th># Games</th>
<th># Won</th>
<th># Lost</th>
<th>% Win</th>
<th class="tooltip">&Delta; Crashes<span role="tooltip">Crashes since last update. Reset after the first game after an update.</span></th>
<th>Last updated</th>
</tr>
</thead>
<tbody>
${data.map((bot, index) => bot.enabled ? activeBotRow(bot, index) : "")}
</tbody >
</table >
<h3>Disabled bots</h3>
<table>
<thead>
    <tr>
        <th>#</th>
        <th>Bot</th>
        <th>ELO</th>
        <th># Games</th>
        <th># Won</th>
        <th># Lost</th>
        <th>% Win</th>
        <th># Crashes</th>
        <th data-sorter="false">Reason</th>
    </tr>
</thead>
<tbody>
    ${data.map((bot, index) => bot.showDisabled ? disabledBotRow(bot, index) : "")}
</tbody>
</table>

`;

render(rankingTable([]), tableNode);

async function update() {
    let { data } = await axios.get("https://basilicum.bytekeeper.org/stats/ranking.json");
    for (let i = 0; i < data.length; i++) {
        let played = data[i].won + data[i].lost;
        if (played > 0 && typeof data[i].won !== "undefined") {
            data[i].winRate = basil.percentFormat(data[i].won / played);
        } else {
            data[i].winRate = "-";
        }
        if (played < 30) data[i].rating = null;
        if (data[i].lastUpdated) data[i].lastUpdated = basil.formatDateTime(data[i].lastUpdated);
        data[i].anchorName = encodeURIComponent(data[i].botName);
        data[i].showDisabled = !data[i].enabled && (data[i].won > 0 || data[i].lost > 0);
        data[i].disabledReason = data[i].disabledReason || "Disabled on SSCAIT/locally";
    }
    // Required for index to work as expected
    basil.sortByRank(data);
    render(rankingTable(data), tableNode);
    $("#eloTable table").tablesorter();
    if (location.hash) {
        const headerHeight = $("thead tr").height();
        const dest = $("td[name='" + location.hash.substring(1) + "']");
        $("html, body").animate({ scrollTop: dest.offset().top - headerHeight }, 'slow');
    }


}

update()