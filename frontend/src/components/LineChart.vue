<script setup>
    import { ref, onMounted } from "vue";
    import { VueDataUi , VueUiQuickChart} from "vue-data-ui";
    import "vue-data-ui/style.css";

    import { useUserStore } from '@/stores/user'
    import { formatDuration } from '@/service/utils.js'
    import * as chart from '@/service/chart.js'
    import { getUserSummary } from '@/service/api.js'

    const userStore = useUserStore()
    const x = ref([]);
    const y = ref([]);
    
    const dataset = ref([

    {
        name: "Dataset",
        series: [],
        type: "line",
        color: "#ff7f0e",
        useArea: false,
        dataLabels: true,
        smooth: true,
        shape: "circle",
        scaleSteps: 5,
        showSerieName: 'end'
    }]
    );

        const config = ref({
        responsive: false,
        backgroundColor: '#ffffff',
        barAnimated: true,
        barGap: 12,
        barStrokeWidth: 1,
        blurOnHover: true,
        chartIsBarUnderDatasetLength: 6,
        color: '#000000',
        dataLabelFontSize: 14,
        dataLabelRoundingPercentage: 1,
        dataLabelRoundingValue: 1,
        donutHideLabelUnderPercentage: 3,
        donutLabelMarkerStrokeWidth: 1,
        donutRadiusRatio: 0.4,
        donutShowTotal: true,
        donutStrokeWidth: 2,
        donutThicknessRatio: 0.18,
        donutTotalLabelFontSize: 24,
        donutTotalLabelOffsetY: 0,
        donutTotalLabelText: 'Total',
        donutUseShadow: false,
        donutShadowColor: '#1A1A1A',
        fontFamily: 'inherit',
        height: 338,
        legendFontSize: 12,
        legendIcon: 'circleFill',
        legendIconSize: 12,
        lineAnimated: true,
        lineSmooth: true,
        lineStrokeWidth: 2,
        paletteStartIndex: 0,
        showDataLabels: true,
        showLegend: true,
        showTooltip: true,
        showUserOptions: true,
        userOptionsButtons: {
            tooltip: true,
            pdf: true,
            img: true,
            fullscreen: true,
            annotator: true
        },
        userOptionsButtonTitles: {
            open: 'Open options',
            close: 'Close options',
            tooltip: 'Toggle tooltip',
            pdf: 'Download PDF',
            img: 'Download PNG',
            fullscreen: 'Toggle fullscreen',
            annotator: 'Toggle annotator'
        },
        userOptionsPrint: {
            scale: 2
        },
        tooltipCustomFormat: null,
        tooltipBorderRadius: 4,
        tooltipBorderColor: '#3A3A3A',
        tooltipBorderWidth: 1,
        tooltipFontSize: 14,
        tooltipBackgroundOpacity: 30,
        tooltipPosition: 'center',
        tooltipOffsetY: 24,
        useCustomLegend: false,
        valuePrefix: '',
        valueSuffix: '',
        width: 512,
        xyAxisStroke: '#4A4A4A',
        xyAxisStrokeWidth: 1,
        xyGridStroke: '#5A5A5A',
        xyGridStrokeWidth: 0.5,
        xyHighlighterColor: '#FFFFFF',
        xyHighlighterOpacity: 0.05,
        xyLabelsXFontSize: 8,
        xyLabelsYFontSize: 12,
        xyPaddingBottom: 48,
        xyPaddingLeft: 48,
        xyPaddingRight: 12,
        xyPaddingTop: 24,
        xyPeriods: [],
        xyPeriodLabelsRotation: 0,
        xyPeriodsShowOnlyAtModulo: true,
        xyPeriodsModulo: 12,
        xyScaleSegments: 10,
        xyShowAxis: true,
        xyShowGrid: true,
        xyShowScale: true,
        yAxisLabel: 'Quantity',
        xAxisLabel: 'Time',
        axisLabelsFontSize: 12,
        zoomXy: true,
        zoomColor: '#CCCCCC',
        zoomHighlightColor: '#4A4A4A',
        zoomFontSize: 14,
        zoomUseResetSlot: false,
        userOptionsPosition: 'right',
        zoomMinimap: {
            show: true,
            smooth: true,
            selectedColor: '#8A8A8A',
            selectedColorOpacity: 0.2,
            lineColor: '#1f77b4',
            selectionRadius: 2,
            indicatorColor: '#CCCCCC',
            verticalHandles: false
        },
        zoomStartIndex: null,
        zoomEndIndex: null,
        showUserOptionsOnChartHover: false,
        keepUserOptionsStateOnChartLeave: true,
        zoomEnableRangeHandles: true,
        zoomEnableSelectionDrag: true
    });

    /*const config = ref(
        {
            responsive: false,
            xyPeriods: [],
            xyTitle: {
                x: 'Months',
                y: 'Values'
            },
            xyLegend: {
                position: 'top',
                align: 'center'
            },
            xyGrid: {
                showXGrid: true,
                showYGrid: true,
                xGridColor: '#e0e0e0',
                yGridColor: '#e0e0e0'
            },
            xyTooltip: {
                enabled: true,
                format: '{name}: {value}'
            },
            xyAxis: {
                xAxisType: 'category',
                yAxisType: 'linear'
            }
        }
    );*/

const userSummary = ref({
  sessionsCount: 0,
  minSessionDuration: 0,
  maxSessionDuration: 0,
  averageSessionDuration: 0,
  monthlySessionsCounts: {},
  hourlySessionsCounts: {}
})

onMounted(async () => {
  const response = await getUserSummary(userStore.username).then(data => {
    userSummary.value = data.data

    const monthlyData = Object.entries(userSummary.value.monthlySessionsCounts)
      .map(([key, val]) => ({ x: new Date(key), y: val }))
      .sort((a, b) => a.x - b.x)

    console.log('Monthly data:', monthlyData);

    for(data of monthlyData)
    {
      x.value.push(data.x.toLocaleDateString());
      y.value.push(data.y);
    }

    dataset.value[0].series = y.value
    config.value.xyPeriods = x.value
    })
})
</script>

<template>
    <VueUiQuickChart 
        :dataset="dataset"
        :config="config"
    />
</template>


