import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';


const lastUpdatedTemplate = bots => html`
        <h2>Recent Bot Updates</h2>

        <div style="max-height: 20em; overflow: auto;">
        <ul style="list-style-type: none; margin: 0; padding: 0;">
        ${bots.map(bot => html`
            <li class=${basil.racecol(bot.race)}>
            ${bot.lastUpdated} <i class="fas fa-caret-right"></i> <a class="normal " href="bot.html?bot=${bot.botName}">${bot.botName}</a>${bot.won + bot.lost <= 50 ? html`<span>&#x1F195;</span>` : ""}
            </li>
            `)}
        </ul>
        </div>
        <hr>
        `;

const botsAfter = Date.now() / 1000 - 86400 * 5;
axios.get("https://basilicum.bytekeeper.org/stats/ranking.json")
    .then(result => {
        const data = result.data;
        const bots = data.filter(function (x) {
            return x.lastUpdated >= botsAfter && x.enabled;
        }).sort(function (a, b) { return b.lastUpdated - a.lastUpdated; })
            .map(function (bot) {
                bot.lastUpdated = basil.formatDate(bot.lastUpdated);
                return bot;
            });
        render(lastUpdatedTemplate(bots), document.querySelector("#lastUpdated"));
    });
