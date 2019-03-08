let basil = function(basil) {
	Handlebars.registerHelper("inc", function(v) { return parseInt(v) + 1; });
	basil.formatDateTime = function(epochSecond) {
		return moment.unix(epochSecond).format("YYYY.MM.DD hh:mm:ss a");
	};
	basil.formatDate = function(epochSecond) {
		return moment.unix(epochSecond).format("YYYY.MM.DD");
	};
	basil.racecol = function(race) {
		switch (race) {
			case "PROTOSS":
				return "race_protoss";
			case "ZERG":
				return "race_zerg";
			case "TERRAN":
				return "race_terran";
			case "RANDOM":
				return "race_random";
			default:
				return "race_unknown";
		}
	};
	Handlebars.registerHelper("racecol", basil.racecol);
	Handlebars.registerHelper("date", basil.formatDate);
};

$(function() {basil(basil);})
