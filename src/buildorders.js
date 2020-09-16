import axios from 'axios'
import basil from './basil.js'
import { html, render } from 'lit-html';
import tablesorter from 'tablesorter'
import $ from 'jquery'

const boRow = bo => html`        
<tr>
<td>
${bo.boString}
</td>
<td>
${bo.name || ""}
</td>
<td>
${bo.amount}
</td>
<td>
${bo.won}
</td>
<td>
${bo.lost}
</td>
<td>
${basil.percentFormat(bo.won / (bo.won + bo.lost))}
</td>
</tr>`;

const boRows = (bos) => bos.map(bo => boRow(bo));

const boRowsNode = document.querySelector("#boRows");

axios.get("https://basilicum.bytekeeper.org/stats/top_bos.json")
    .then(result => {
        let data = result.data;

        render(boRows(data), boRowsNode);
        $("#boTable").tablesorter();
    });
