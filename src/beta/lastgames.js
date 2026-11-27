import { html, render } from 'lit-html';
import renderGameListing from './gamelisting.js';

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
        hideFilters: true,
        filter: games =>
            shuffle(games.filter(g => !g.botA.crashed && !g.botB.crashed && !g.realTimeout))
                .slice(0, 10)
                .map(g => {
                    g.botA.winner = false;
                    g.botB.winner = false;
                    if (Math.random() < 0.5) {
                        const tmp = g.botA;
                        g.botA = g.botB;
                        g.botB = tmp;
                    }
                    return g;
                })
    });
}

const luckyButton = html`<button class="lucky" @click=${rollGames}>I'm feeling lucky</button>`;

render(luckyButton, document.querySelector("#luckyButton"));
