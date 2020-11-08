import Chart from 'chart.js'
import ChartDataLabels from 'chartjs-plugin-datalabels'
import 'flatpickr'
import moment from 'moment'
import '@fortawesome/fontawesome-free/css/all.css'
import 'flatpickr/dist/flatpickr.css'

Chart.plugins.unregister(ChartDataLabels);


export default {
    dataBaseUrl: "https://data.basil-ladder.net/",
    formatDateTime: function (epochSecond) {
        return moment.unix(epochSecond).format("YYYY.MM.DD hh:mm a");
    },
    formatDate: function (epochSecond) {
        return moment.unix(epochSecond).format("YYYY.MM.DD");
    },
    racecol: function (race) {
        switch (race) {
            case "PROTOSS":
            case "P":
                return "race_protoss";
            case "ZERG":
            case "Z":
                return "race_zerg";
            case "TERRAN":
            case "T":
                return "race_terran";
            case "RANDOM":
            case "R":
                return "race_random";
            default:
                return "race_unknown";
        }
    },
    racename: function (race) {
        switch (race) {
            case "PROTOSS":
            case "P":
                return "Protoss";
            case "ZERG":
            case "Z":
                return "Zerg";
            case "TERRAN":
            case "T":
                return "Terran";
            case "RANDOM":
            case "R":
                return "Random";
            default:
                return "Unknown";
        }
    },
    percentFormat: function (value, digits) {
        return new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: digits || 2 }).format(value);
    },
    sortByRank: function (data) {
        data.sort(function (a, b) {
            if (a.enabled != b.enabled) {
                if (a.enabled) return -1; else return 1;
            }
            let aPlayed = a.won + a.lost;
            let bPlayed = b.won + b.lost;
            if (aPlayed < 30 && bPlayed >= 30) return 1;
            if (bPlayed < 30 && aPlayed >= 30) return -1;
            if (a.rating !== b.rating) return b.rating - a.rating;
            if (a.winRate !== b.winRate) return b.winRate - a.winRate;
            return b.won - a.won;
        });
    },
    rankcol: function (rank) {
        return "rank_" + rank.toLowerCase();
    },
    percentFormat: function (value, digits) {
        return new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: digits || 2 }).format(value);
    },
    rankValue: function (rank) {
        switch (rank) {
            case "S": return 0;
            case "A": return 1;
            case "B": return 2;
            case "C": return 3;
            case "D": return 4;
            case "E": return 5;
            case "F": return 6;
            case "U":
            case "UNRANKED": return 99999;
        }
    },
    sortByRank: function (data) {
        let self = this;
        data.sort(function (a, b) {
            if (a.enabled != b.enabled) {
                if (a.enabled) return -1; else return 1;
            }
            let aPlayed = a.won + a.lost;
            let bPlayed = b.won + b.lost;
            if (aPlayed < 30 && bPlayed >= 30) return 1;
            if (bPlayed < 30 && aPlayed >= 30) return -1;
            if (self.rankValue(a.rank) != self.rankValue(b.rank)) return self.rankValue(a.rank) - self.rankValue(b.rank);
            if (a.rating !== b.rating) return b.rating - a.rating;
            if (a.winRate !== b.winRate) return b.winRate - a.winRate;
            return b.won - a.won;

        });
    }
}