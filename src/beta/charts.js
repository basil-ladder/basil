import Chart from 'chart.js'
import ChartDataLabels from 'chartjs-plugin-datalabels'

// Charts opt in to in-bar labels themselves.
Chart.plugins.unregister(ChartDataLabels);

export function chartThemeColors() {
    var isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    return {
        isDark: isDark,
        gridColor: isDark ? 'rgba(255,255,255,0.15)' : 'rgba(0,0,0,0.1)',
        tickColor: isDark ? '#aaaaaa' : '#666666',
        legendColor: isDark ? '#e0e0e0' : '#333333',
        tooltipBg: isDark ? 'rgba(30,30,50,0.95)' : 'rgba(0,0,0,0.8)',
        tooltipText: isDark ? '#e0e0e0' : '#ffffff',
        datalabelColor: isDark ? '#cccccc' : '#444444',
    };
}

export function applyThemeToChart(chart) {
    var t = chartThemeColors();
    if (chart.options.scales) {
        (chart.options.scales.xAxes || []).forEach(function(axis) {
            axis.gridLines = axis.gridLines || {};
            axis.gridLines.color = t.gridColor;
            axis.gridLines.zeroLineColor = t.gridColor;
            axis.ticks = axis.ticks || {};
            axis.ticks.fontColor = t.tickColor;
        });
        (chart.options.scales.yAxes || []).forEach(function(axis) {
            axis.gridLines = axis.gridLines || {};
            axis.gridLines.color = t.gridColor;
            axis.gridLines.zeroLineColor = t.gridColor;
            axis.ticks = axis.ticks || {};
            axis.ticks.fontColor = t.tickColor;
        });
    }
    chart.options.legend = chart.options.legend || {};
    chart.options.legend.labels = chart.options.legend.labels || {};
    chart.options.legend.labels.fontColor = t.legendColor;
    chart.options.tooltips = chart.options.tooltips || {};
    chart.options.tooltips.backgroundColor = t.tooltipBg;
    chart.options.tooltips.bodyFontColor = t.tooltipText;
    chart.options.tooltips.titleFontColor = t.tooltipText;
    chart.options.tooltips.footerFontColor = t.tooltipText;
}

// The theme button in the header calls this to recolour every chart.
const chartUpdaters = [];
window.basilUpdateChartsTheme = function () {
    chartUpdaters.forEach(fn => fn());
};

export function registerChartUpdater(fn) {
    chartUpdaters.push(fn);
}
