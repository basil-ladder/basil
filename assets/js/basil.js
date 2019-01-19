let basil = function(basil) {
	Handlebars.registerHelper("racecol", function(v) { 
		switch (v) {
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
	});
	Handlebars.registerHelper("inc", function(v) { return parseInt(v) + 1; });
	basil.formatDate = function(epochSecond) {
		return moment.unix(epochSecond).format("YYYY.MM.DD hh:mm:ss a");
	};
	Handlebars.registerHelper("date", function(d) { return basil.formatDate(d); });
};

$(function() {basil(basil);})
