import axios from 'axios'
import { html, render } from 'lit-html';
import basil from './basil.js'
import tablesorter from 'tablesorter'
import $ from 'jquery'


let numberFormat = new Intl.NumberFormat(undefined, { minimumFractionDigits: 2, maximumFractionDigis: 2 }).format;
const rowTemplate = row => html`
        <tr>
        <th>Games played</th>
        <td>${row.gamesPlayed}</td>
        </tr>
        <tr>
        <th class="race_terran">Terran bots</th>
        <td>${row.terranBots}</td>
        </tr>
        <tr>
        <th class="race_protoss">Protoss bots</th>
        <td>${row.protossBots}</td>
        </tr>
        <tr>
        <th class="race_zerg">Zerg bots</th>
        <td>${row.zergBots}</td>
        </tr>
        <tr>
        <th class="race_random">Random bots</th>
        <td>${row.randomBots}</td>
        </tr>
        <tr>
        <th>Crashes</th>
        <td>${row.crashes}</td>
        </tr>
        <tr>
        <th>Avg. playtime (realtime)</th>
        <td>${numberFormat(row.averageGameRealtime)} seconds</td>
        </tr>
        <tr>
        <th>Next bot update</th>
        <td>${row.nextUpdateTime}</td>
        </tr>
        <tr>
        `;

const crossTableTemplate = x => html`
        <thead>
        <th>Race</th>
        <th>Won</th>
        <th>Lost</th>
        <th>% Win</th>
        ${Object.keys(x).map((key) => html`
        <th class="cap race_${key}">${key}</th>
            `)}
        </thead>
        <tbody>
        ${Object.entries(x).map(([key, vsrow]) => html`
        <tr>
        <th class="cap race_${key}">${key}</th>
        <td>${vsrow.won}</td>
        <td>${vsrow.lost}</td>
        <td>${vsrow.winRate}</td>
        <td>${vsrow.terran}</td>
        <td>${vsrow.protoss}</td>
        <td>${vsrow.zerg}</td>
        <td>${vsrow.random}</td>
        </tr>
            `)}
        </tbody>
        `;

const unitStatsTableTemplate = stats => html`
    <table id="unitStatsTable">
    <thead>
    <th>Unit</th>
    <th>Created</th>
    <th>Destroyed</th>
    </thead>
    <tbody>
        ${stats.map(unit => html`
    <tr each=${unit.unitStats}>
    <td>${unit.name}</td>
    <td>${unit.created}</td>
    <td>${unit.destroyed}</td>
    </tr>
            `)}
    </tbody>
    </table>	
`;

const srcUrl = basil.dataBaseUrl + "stats/stats.json";
const srcRef = html`Source: <a href=${srcUrl}>${srcUrl}</a>`;
render(srcRef, document.querySelector("#srcRef"));

const percentFormat = new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: 1 }).format;
const crossTableNode = document.querySelector("#crossTable");
//const unitStatsNode = document.querySelector("#unitStats");
const statsTable = document.querySelector("#statsTable tbody");


axios.get(srcUrl)
    .then(result => {
        const data = result.data;
        data.nextUpdateTime = new Date(data.nextUpdateTime).toLocaleString([], { hour: "numeric" }) + " - " + new Date(data.nextUpdateTime + 60 * 60 * 1000).toLocaleString([], { hour: "numeric" });
        render(rowTemplate(data), statsTable);
        let crossTableData = data.raceCrossTable;
        for (let key in crossTableData) {
            crossTableData[key].lost = 0;
        }
        for (let key in crossTableData) {
            let row = crossTableData[key];
            row.won = row.terran + row.protoss + row.zerg + row.random;
            for (let e in row) {
                if (typeof crossTableData[e] !== "undefined") crossTableData[e].lost += row[e];
            }
        }
        for (let key in crossTableData) {
            let row = crossTableData[key];
            row.winRate = (row.won + row.lost) > 0 ? percentFormat(row.won / (row.won + row.lost)) : "-";
        }

        render(crossTableTemplate(crossTableData), crossTableNode);
//        render(unitStatsTableTemplate(data.unitStats), unitStatsNode);
        $('#unitStatsTable').tablesorter();
    });

