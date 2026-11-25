// Builds assets/icons.svg: only the icons the site uses, instead of the 78 KB Font Awesome font.
// To add an icon: put its Font Awesome (solid) name here and run `npm run icons`.
import { readFileSync, writeFileSync } from "fs";

const icons = [
    "arrow-down", "arrow-up", "biohazard", "car-crash", "caret-right", "chart-line", "chess-queen",
    "dice", "download", "eye", "fighter-jet", "fish", "hourglass-half", "lock", "moon",
    "pastafarianism", "poo-storm", "radiation", "sad-tear", "smog", "spider", "stopwatch", "sun",
    "trophy", "user-secret",
];

const dir = "node_modules/@fortawesome/fontawesome-free/svgs/solid/";
const symbols = icons.map(name => {
    const svg = readFileSync(dir + name + ".svg", "utf8");
    const viewBox = svg.match(/viewBox="([^"]+)"/)[1];
    const path = svg.match(/<path d="([^"]+)"/)[1];
    return `<symbol id="${name}" viewBox="${viewBox}"><path d="${path}"/></symbol>`;
});

writeFileSync("assets/icons.svg",
    '<svg xmlns="http://www.w3.org/2000/svg">' +
    "<!-- Font Awesome Free 5.15.4 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free (Icons: CC BY 4.0) -->\n" +
    symbols.join("\n") + "\n</svg>\n");
