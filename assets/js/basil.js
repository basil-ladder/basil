import html from "../lib/html-template-tag.js"

window.html = html;

window.basil = function (basil) {
	basil.formatDateTime = function (epochSecond) {
		return moment.unix(epochSecond).format("YYYY.MM.DD hh:mm a");
	};
	basil.formatDate = function (epochSecond) {
		return moment.unix(epochSecond).format("YYYY.MM.DD");
	};
	basil.racecol = function (race) {
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
	};
	basil.racename = function (race) {
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
	};
	basil.rankcol = function (rank) {
		return "rank_" + rank.toLowerCase();
	};
	basil.percentFormat = function (value, digits) {
		return new Intl.NumberFormat(undefined, { style: "percent", minimumFractionDigits: digits || 2 }).format(value);
	};
	basil.sortByRank = function (data) {
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
	};

	Chart.plugins.unregister(ChartDataLabels);
};

$(function () { window.basil(window.basil); })
