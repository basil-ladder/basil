import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';
import renderGameListing from './gamelisting';

renderGameListing();

const luckyButton = html`<button class="w3-button w3-blue" @onclick=#{}>I am feeling lucky</button>`;

//render(luckyButton, document.querySelector("#luckyButton"));
