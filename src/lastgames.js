import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';
import renderGameListing from './gamelisting';

renderGameListing();

function rollGames() {
    function shuffle(a) {
        for (let i = a.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            const x = a[i];
            a[i] = a[j];
            a[j] = x;
        }
        return a;
    }
    renderGameListing({
        filter: games =>
            shuffle(games.filter(g => g.validGame))
                .slice(0, 10)
                .map(g => {
                    g.botA.winner = false;
                    g.botA.loser = false;
                    g.botB.winner = false;
                    g.botB.loser = false;
                    if (Math.random() < 0.5) {
                        const tmp = g.botA;
                        g.botA = g.botB;
                        g.botB = tmp;
                    }
                    return g;
                })
    });
}

const luckyButton = html`<button class="w3-button w3-blue" @click=${rollGames}>I'm feeling lucky</button>`;

render(luckyButton, document.querySelector("#luckyButton"));
