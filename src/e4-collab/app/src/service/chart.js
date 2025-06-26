
        function aggregateData(data, roundingIntervalSeconds = 10, attributeNameX = "x", attributeNameY = "y") {
            if (!data || data.length === 0) {
                return [];
            }

            if (data.length === 1) {
                return data;
            }

            const aggregatedData = Array.from(d3.rollup(
                data,
                v => ({
                    x: new Date(d3.mean(v, d => d[attributeNameX])),
                    y: d3.mean(v, d => d[attributeNameY])
                }),
                d => roundingIntervalSeconds ? roundToNSecondsUTC(d[attributeNameX], roundingIntervalSeconds) : d[attributeNameX]
            )).map(pair => pair[1]);

            return [data[0], ...aggregatedData, data[data.length - 1]];
        }

        function roundToNSecondsUTC(date, roundingIntervalSeconds) {
            if (roundingIntervalSeconds <= 0) {
                return date;
            }
            const totalMilliseconds = date.getTime();
            const roundedMilliseconds = Math.floor(totalMilliseconds / (roundingIntervalSeconds * 1000)) * (roundingIntervalSeconds * 1000);

            return new Date(roundedMilliseconds);
        };
        function getMousePosition(event, targetElement) {
            if (!event.offsetX || !event.offsetY) {
                return [0, 0];
            }

            const targetCTM = targetElement.getCTM();

            const invScaleX = 1 / targetCTM.a;
            const invScaleY = 1 / targetCTM.d;

            const mouseX = (event.offsetX - targetCTM.e) * invScaleX;
            const mouseY = (event.offsetY - targetCTM.f) * invScaleY;

            return [mouseX, mouseY];
        }
        function getElementViewBox(svgElement) {
            const svgElementNode = svgElement.node ? svgElement.node() : svgElement;
            if (!(svgElementNode instanceof SVGGraphicsElement)) {
                return { left: 0, top: 0, right: 0, bottom: 0, width: 0, height: 0 };
            }

            if (svgElementNode.style.display === 'none' || svgElementNode.style.visibility === 'hidden' || svgElementNode.style.opacity === '0') {
                return { left: 0, top: 0, right: 0, bottom: 0, width: 0, height: 0 };
            }

            const result = { left: Infinity, top: Infinity, right: -Infinity, bottom: -Infinity, width: 0, height: 0 };

            for (const child of svgElementNode.children) {
                const childBBox = getElementViewBox(child);
                if (childBBox) {
                    result.left = Math.min(result.left, childBBox.left);
                    result.top = Math.min(result.top, childBBox.top);
                    result.right = Math.max(result.right, childBBox.right);
                    result.bottom = Math.max(result.bottom, childBBox.bottom);
                }
            }

            try {
                const bbox = svgElementNode.getBBox();

                const localLeft = bbox.x;
                const localTop = bbox.y;
                const localRight = bbox.x + bbox.width;
                const localBottom = bbox.y + bbox.height;

                const ctm = svgElementNode.getCTM();

                if (ctm) {
                    const scaleX = ctm.a;
                    const scaleY = ctm.d;
                    const skewX = ctm.c;
                    const skewY = ctm.b;
                    const translateX = ctm.e;
                    const translateY = ctm.f;

                    const topLeft = {
                        x: scaleX * localLeft + skewX * localTop + translateX,
                        y: skewY * localLeft + scaleY * localTop + translateY
                    };
                    const topRight = {
                        x: scaleX * localRight + skewX * localTop + translateX,
                        y: skewY * localRight + scaleY * localTop + translateY
                    };
                    const bottomRight = {
                        x: scaleX * localRight + skewX * localBottom + translateX,
                        y: skewY * localRight + scaleY * localBottom + translateY
                    };
                    const bottomLeft = {
                        x: scaleX * localLeft + skewX * localBottom + translateX,
                        y: skewY * localLeft + scaleY * localBottom + translateY
                    };

                    const xs = [topLeft.x, topRight.x, bottomRight.x, bottomLeft.x];
                    const ys = [topLeft.y, topRight.y, bottomRight.y, bottomLeft.y];

                    const minX = Math.min(...xs);
                    const minY = Math.min(...ys);
                    const maxX = Math.max(...xs);
                    const maxY = Math.max(...ys);

                    result.left = Math.min(result.left, minX);
                    result.top = Math.min(result.top, minY);
                    result.right = Math.max(result.right, maxX);
                    result.bottom = Math.max(result.bottom, maxY);
                }
            } finally {
                if (result.left === Infinity) {
                    return { left: 0, top: 0, right: 0, bottom: 0, width: 0, height: 0 };
                }

                result.width = result.right - result.left;
                result.height = result.bottom - result.top;
                return result;
            }
        }
        function getElementsViewBox(svgElements) {
            const svgElementsNodes = svgElements.map(svgElement => svgElement.node ? svgElement.node() : svgElement);
            if (svgElementsNodes.length === 0) {
                return { left: 0, top: 0, right: 0, bottom: 0, width: 0, height: 0 };
            }

            const result = { left: Infinity, top: Infinity, right: -Infinity, bottom: -Infinity, width: 0, height: 0 };

            svgElementsNodes.forEach(svgElementNode => {
                const elemResult = getElementViewBox(svgElementNode);
                result.left = Math.min(result.left, elemResult.left);
                result.top = Math.min(result.top, elemResult.top);
                result.right = Math.max(result.right, elemResult.right);
                result.bottom = Math.max(result.bottom, elemResult.bottom);
            });

            if (result.left === Infinity) {
                return { left: 0, top: 0, right: 0, bottom: 0, width: 0, height: 0 };
            }

            result.width = result.right - result.left;
            result.height = result.bottom - result.top;
            return result;
        }
        function getScale(domain, data, attributeName, paddingPercentage) {
            let min, max;
            if (domain && domain.length === 2) {
                min = domain[0];
                max = domain[1];
            }
            else {
                const mean = d3.mean(data, d => d[attributeName]);
                min = d3.min(data, d => d[attributeName]);
                max = d3.max(data, d => d[attributeName]);
                const minDist = Math.abs(mean - min);
                const maxDist = Math.abs(mean - max);
                const dist = Math.max(minDist, maxDist) / 2;
                min = Math.min(min, mean - dist);
                max = Math.max(max, mean + dist);
            }

            let scaleFunction;
            if (min instanceof Date || max instanceof Date) {
                scaleFunction = d3.scaleTime;
            }
            else {
                scaleFunction = d3.scaleLinear;

                const span = Math.abs(max - min);
                const padding = span * paddingPercentage;

                min = min - padding;
                max = max + padding;

                if (min > max) {
                    [min, max] = [max, min];
                }
            }

            return scaleFunction().domain([min, max]);
        }
        function getClosestPathPoint(pathProperties, targetX, toleranceX = 0.5) {
            const length = pathProperties.getTotalLength();
            let left = 0, right = length;
            if (length === 0) {
                return null;
            }

            while (right - left > toleranceX) {
                const mid = (left + right) / 2;
                const point = pathProperties.getPointAtLength(mid);

                if (Math.abs(point.x - targetX) < toleranceX) {
                    return point;
                }

                if (point.x < targetX) {
                    left = mid;
                } else {
                    right = mid;
                }
            }

            return pathProperties.getPointAtLength((left + right) / 2);
        }
        function getClosestDataPoint(data, mouseX, toleranceX, scales) {
            const [xScale, yScale] = scales;
            const bisector = d3.bisector(d => d.x);
            const xValue = xScale.invert(mouseX);
            let index = bisector.right(data, xValue);
            index = Math.min(index, data.length - 1);

            let d0 = data[index];
            let x0 = xScale(d0.x);
            let dist0 = Math.abs(mouseX - x0);

            if (index > 0) {
                const d1 = data[index - 1];
                const x1 = xScale(d1.x);
                const dist1 = Math.abs(mouseX - x1);

                if (dist1 < dist0) {
                    d0 = d1;
                    x0 = x1;
                    dist0 = dist1;
                }
            }

            if (dist0 <= toleranceX) {
                return {
                    x: x0,
                    y: yScale(d0.y)
                }
            }
            return null;
        }
        function createChart(prepareSvgElements, onMousemove, onMouseout, onSizeChange) {
            const [eventsTarget, svg, ...otherElements] = prepareSvgElements();
            eventsTarget.node().clearEvents?.();

            const mousemoveCallback = (...eventArgs) => requestAnimationFrame(() => onMousemove(...eventArgs, ...otherElements));
            eventsTarget.on("mousemove", mousemoveCallback);

            const mouseoutCallback = () => onMouseout(...otherElements);
            eventsTarget.on("mouseout", () => onMouseout(...otherElements));

            const resizeObserverCallback = () => { onSizeChange(...otherElements) };
            const resizeObserver = new ResizeObserver(resizeObserverCallback)
            resizeObserver.observe(svg.node());
            window.addEventListener("resize", resizeObserverCallback);


            const mutationObserver = new MutationObserver((mutations) => {
                mutations.forEach(mutation => {
                    mutation.removedNodes.forEach(node => {
                        if (node === eventsTarget.node()) {
                            node.clearEvents();
                        }
                    });
                });
            });

            eventsTarget.node().clearEvents = () => {
                eventsTarget.node().removeEventListener("mousemove", mousemoveCallback);
                eventsTarget.node().removeEventListener("mouseout", mouseoutCallback);
                window.removeEventListener("resize", resizeObserverCallback);
                resizeObserver.disconnect();
                mutationObserver.disconnect();
            }

            mutationObserver.observe(eventsTarget.node(), { childList: true, subtree: true });

            requestAnimationFrame(() => resizeObserverCallback());
        }
        export const createAreaChart = function (data, svgElement, tooltipContainerElement = [], tooltipLabelElement = [], domainX = [], domainY = []) {
            const prepareSvgElements = () => {

                aggregatedData = aggregateData(data, 10, "x", "y");

                const svg = d3.select(svgElement);

                const xScale = getScale(domainX, aggregatedData, "x");
                const yScale = getScale(domainY, aggregatedData, "y", 0.1);

                const chartGroup = svg.append("g");

                const xAxis = chartGroup.append("g");
                const yAxis = chartGroup.append("g");

                const line = d3.line()
                    .x(d => xScale(d.x))
                    .y(d => yScale(d.y))
                    // .curve(d3.curveCatmullRom);
                    .curve(d3.curveCardinal.tension(0.4));

                const clipRect = svg.append("defs")
                    .append("clipPath")
                    .attr("id", "clip")
                    .append("rect");

                const path = chartGroup.append("path")
                    .attr("class", "line")
                    .attr("fill", "none")
                    .attr("stroke", "#143D59")
                    .attr("stroke-width", 2)
                    .attr("clip-path", "url(#clip)");


                const focus = svg.append("circle")
                    .attr("class", "focus-line")
                    .attr("r", 5)
                    .attr("fill", "#143D59")
                    .style("display", "none")
                    .style("pointer-events", "none");

                const eventsTarget = svg;
                const otherElements = [svg, [xScale, yScale], [xAxis, yAxis], path, aggregatedData, focus, chartGroup, line, clipRect];
                return [eventsTarget, svg, ...otherElements];
            };

            const onMousemove = (event, d, svg, scales, axes, path, aggregatedData, focus, ...otherElements) => {
                const [xScale, yScale] = scales;

                const viewBox = getElementsViewBox(axes);
                const width = viewBox.width;

                const minX = 0;
                const maxX = viewBox.right;

                const [mouseX] = getMousePosition(event, svg.node());
                const clampedMouseX = Math.min(Math.max(minX, mouseX), maxX);
                const toleranceX = width * 0.02;

                let closestPoint = null;

                if (minX - toleranceX < mouseX && mouseX < maxX + toleranceX) {
                    closestPoint = getClosestDataPoint(aggregatedData, clampedMouseX, toleranceX, scales);
                    if (!closestPoint || closestPoint.x < minX || maxX < closestPoint) {
                        if (Math.abs(mouseX - minX) < toleranceX) {
                            closestPoint = getClosestPathPoint(path.pathProperties, minX);
                        } else if (Math.abs(mouseX - maxX) < toleranceX) {
                            closestPoint = getClosestPathPoint(path.pathProperties, maxX);
                        }
                    }
                }

                if (!closestPoint) {
                    focus.style("display", "none");
                    tooltipContainerElement?.style?.setProperty("display", "none");
                    return;
                }

                focus.style("display", null);

                focus.attr("transform", `translate(${closestPoint.x},${closestPoint.y})`);

                if (tooltipContainerElement && tooltipLabelElement) {
                    const parent = svgElement.parentElement;
                    parent.appendChild(tooltipContainerElement);

                    const pathScreenCTM = path.node().getScreenCTM();
                    const centerX = (closestPoint.x * pathScreenCTM.a) + pathScreenCTM.e;
                    const centerY = (closestPoint.y * pathScreenCTM.d) + pathScreenCTM.f;

                    tooltipContainerElement.style.setProperty("left", centerX + "px");
                    tooltipContainerElement.style.setProperty("top", centerY + "px");
                    tooltipContainerElement.style.setProperty("position", "fixed");
                    tooltipContainerElement.style.setProperty("pointer-events", "none");
                    tooltipContainerElement.style.setProperty("display", "block");

                    const xValue = xScale.invert(closestPoint.x);
                    const yValue = yScale.invert(closestPoint.y);

                    tooltipLabelElement.textContent = `${yValue.toFixed(0)}`
                }
            };

            const onMouseout = (svg, scales, axes, path, aggregatedData, focus, ...otherElements) => {
                focus.style("display", "none");
                tooltipContainerElement?.style?.setProperty("display", "none");
            };

            const onSizeChange = (svg, scales, axes, path, aggregatedData, focus, chartGroup, line, clipRect) => {
                const [xScale, yScale] = scales;
                const [xAxis, yAxis] = axes;

                const width = svg.node().clientWidth;
                const height = svg.node().clientHeight;

                chartGroup.attr("width", width).attr("height", height);
                clipRect.attr("width", width).attr("height", height);
                path.datum(aggregatedData).attr("d", line);
                path.pathProperties = window.svgPathProperties.svgPathProperties(path.attr("d"));

                focus.attr("y1", 0).attr("y2", height);

                xAxis.call(d3.axisBottom(xScale.range([0, width])).tickFormat(d3.timeFormat("%B %Y")));
                yAxis.call(d3.axisLeft(yScale.range([height, 0])).tickSize(-width).tickFormat(d3.format(".0f")));
                xAxis.attr("transform", `translate(0,${height})`);
                xAxis.selectAll("g line").attr("color", `lightgray`);
                yAxis.selectAll("g line").attr("color", `lightgray`);

                svg.attr("viewBox", `0 0 ${width} ${height}`);
                const viewBox = getElementsViewBox(axes);
                svg.attr("viewBox", `${viewBox.left} ${viewBox.top} ${viewBox.right - viewBox.left} ${viewBox.bottom + viewBox.top}`);

                focus.style("display", "none");
                tooltipContainerElement?.style?.setProperty("display", "none");
            };

            createChart(prepareSvgElements, onMousemove, onMouseout, onSizeChange);
        }
        export const createPieChart = function (data, svgElement, tooltipContainerElement = null, tooltipLabelElement = null) {
            const prepareSvgElements = () => {
                const svg = d3.select(svgElement);
                const chartGroup = svg.append("g");

                const pie = d3.pie().sort(null).value(d => d.value);
                const arc = d3.arc();

                const customColors = ["#f3b933", "#ff7f0e", "#143D59"];
                const color = d3.scaleOrdinal(customColors);


                let paths;
                if (!data.some(d => d.value !== 0)) {
                    paths = chartGroup.selectAll("path")
                        .data(pie([{ label: "", value: 1 }]))
                        .enter().append("path")
                        .attr("fill", "#bfbfbf");
                }
                else {
                    paths = chartGroup.selectAll("path")
                        .data(pie(data))
                        .enter().append("path")
                        .attr("fill", (d, i) => color(d.data.label));
                }


                const legendGroup = svg.append("g");

                const legendItems = legendGroup.selectAll("g")
                    .data(data)
                    .enter().append("g");

                legendItems.append("circle")
                    .attr("r", 6)
                    .attr("fill", d => color(d.label));

                legendItems.append("text")
                    .attr("x", 18)
                    .text(d => d.label)
                    .style("font-size", "1rem")
                    .style("alignment-baseline", "central");

                const eventsTarget = paths;
                const otherElements = [svg, chartGroup, arc, paths, legendGroup, legendItems];
                return [eventsTarget, svg, ...otherElements];
            };

            const onMousemove = (event, d, svg, chartGroup, arc, ...otherElements) => {
                if (tooltipContainerElement && tooltipLabelElement) {

                    const parent = svgElement.parentElement;
                    parent.appendChild(tooltipContainerElement);

                    const boundingClientRect = chartGroup.node().getBoundingClientRect();

                    const width = boundingClientRect.width;
                    const height = boundingClientRect.height;
                    const radius = Math.min(width, height) / 2;

                    if (!data.some(d => d.value !== 0)) {
                        const centerX = boundingClientRect.x + boundingClientRect.width / 2;
                        const centerY = boundingClientRect.y + boundingClientRect.height / 2;

                        tooltipContainerElement.style.setProperty("left", centerX + "px");
                        tooltipContainerElement.style.setProperty("top", centerY + "px");
                        tooltipContainerElement.style.setProperty("position", "fixed");
                        tooltipContainerElement.style.setProperty("pointer-events", "none");
                        tooltipContainerElement.style.setProperty("display", "block");
                        tooltipContainerElement.style.setProperty("transform", "translate(-50%, -50%)");

                        tooltipLabelElement.textContent = "No data available"
                    }
                    else {
                        const midAngle = (d.startAngle + d.endAngle) / 2;
                        const centroidX = Math.sin(midAngle) * radius * 0.8;
                        const centroidY = -Math.cos(midAngle) * radius * 0.8;

                        const centerX = boundingClientRect.x + boundingClientRect.width / 2 + centroidX;
                        const centerY = boundingClientRect.y + boundingClientRect.height / 2 + centroidY;

                        tooltipContainerElement.style.setProperty("left", centerX + "px");
                        tooltipContainerElement.style.setProperty("top", centerY + "px");
                        tooltipContainerElement.style.setProperty("position", "fixed");
                        tooltipContainerElement.style.setProperty("pointer-events", "none");
                        tooltipContainerElement.style.setProperty("display", "block");

                        tooltipLabelElement.textContent = `${d.data.label}: ${d.value.toFixed(0)}`
                    }
                }
            };

            const onMouseout = (...otherElements) => {
                tooltipContainerElement?.style?.setProperty("display", "none");
            };

            const onSizeChange = (svg, chartGroup, arc, paths, legendGroup, legendItems) => {

                const boundingClientRect = svgElement.getBoundingClientRect();

                const width = boundingClientRect.width;
                const height = boundingClientRect.height;
                const radius = Math.min(width, height) / 2;

                arc.innerRadius(radius * 0.6).outerRadius(radius);
                paths.attr("d", arc);

                chartGroup.attr("width", width).attr("height", height);
                chartGroup.attr("transform", `translate(${radius}, ${radius})`);

                let cumulativeLegendItemsWidth = 0;
                legendItems.each(function (d, i) {
                    const legendItem = d3.select(this);
                    legendItem.attr("transform", `translate(${cumulativeLegendItemsWidth}, 0)`);
                    cumulativeLegendItemsWidth += this.getBBox().width + 20;
                })

                const gap = 40;

                const legendWidth = legendGroup.node().getBBox().width;
                const legendHeight = legendGroup.node().getBBox().height;

                legendGroup.attr("transform", `translate(${radius - legendWidth / 2}, ${radius * 2 + gap})`);

                svg.attr("viewBox", `0 0 ${radius * 2} ${radius * 2 + gap + legendHeight}`);
            };

            createChart(prepareSvgElements, onMousemove, onMouseout, onSizeChange);
        }
        export const createSessionChart = function (sensorsData, tagsTimestamps, svgElement, tooltipsContainersElements = null, tooltipsLabelsElements = null, domainX = [], domainsY = null, roundIntervalsInSeconds = null, curveFunctions = null, pathsTitles = null, pathsColors = null, ticksColors = null, tagsColor = null) {
            const prepareSvgElements = () => {
                roundIntervalsInSeconds = roundIntervalsInSeconds ?? [];
                if (roundIntervalsInSeconds.length < sensorsData.length) {
                    roundIntervalsInSeconds = [...roundIntervalsInSeconds, ...Array(sensorsData.length - roundIntervalsInSeconds.length).fill(null)];
                }

                pathsColors = pathsColors ?? [];
                if (pathsColors.length < sensorsData.length) {
                    pathsColors = [...pathsColors, ...Array(sensorsData.length - pathsColors.length).fill("#143D59")];
                }

                ticksColors = ticksColors ?? [];
                if (ticksColors.length < sensorsData.length) {
                    ticksColors = [...ticksColors, ...Array(sensorsData.length - ticksColors.length).fill("lightgray")];
                }

                tagsColor = tagsColor ?? "#143D59";


                if (!domainX || domainX.length < 2) {
                    domainX = [d3.min(sensorsData, sensorData => d3.min(sensorData, d => d.x)), d3.max(sensorsData, sensorData => d3.max(sensorData, d => d.x))];
                }

                aggregatedSensorsData = sensorsData.map((sensorData, i) => aggregateData(sensorData, roundIntervalsInSeconds[i], "x", "y"));

                domainsY = domainsY ?? [];
                if (domainsY.length < sensorsData.length) {
                    domainsY = [...domainsY, ...Array(aggregatedSensorsData.length - domainsY.length).fill(null)];
                }

                const svg = d3.select(svgElement);

                const xScale = getScale(domainX, aggregatedSensorsData[0], "x");
                const yScales = domainsY.map((domainY, i) => getScale(domainY, aggregatedSensorsData[i], "y", 0.1));

                const chartGroup = svg.append("g");
                const yAxesBackgrounds = aggregatedSensorsData.map((sensorData, i) => chartGroup.append("g"));
                

                curveFunctions = curveFunctions ?? [];
                if (curveFunctions && curveFunctions.length < sensorsData.length) {
                    curveFunctions = [...curveFunctions, ...Array(sensorsData.length - curveFunctions.length).fill(null)];
                }

                const lines = aggregatedSensorsData.map((sensorData, i) => {
                    return d3.line()
                        .x(d => xScale(d.x))
                        .y(d => yScales[i](d.y))
                        .curve(curveFunctions[i] ?? d3.curveCardinal.tension(0.4));
                });

                const clipRect = svg.append("defs")
                    .append("clipPath")
                    .attr("id", "clip")
                    .append("rect");

                const paths = sensorsData.map((sensorData, i) => {
                    return chartGroup.append("path")
                        .attr("class", "line")
                        .attr("fill", "none")
                        .attr("stroke", "#143D59")
                        .attr("stroke-width", 2)
                        .attr("clip-path", "url(#clip)");
                });

                const xAxis = chartGroup.append("g");
                const yAxes = aggregatedSensorsData.map((sensorData, i) => chartGroup.append("g"));

                pathsTitles = pathsTitles ?? [];
                if (pathsTitles.length < sensorsData.length) {
                    pathsTitles = [...pathsTitles, ...Array(sensorsData.length - pathsTitles.length).fill("")];
                }
                const titlesTexts = pathsTitles.map((title, i) => {
                    return chartGroup.append("text")
                        .attr("class", "title")
                        .attr("x", 0)
                        .attr("y", 0)
                        .attr("text-anchor", "middle")
                        .attr("vertical-align", "middle")
                        .text(title)
                });

                tagsTimestamps = tagsTimestamps ?? [];
                const tagsFocus = tagsTimestamps.map((tag, i) => {
                    return svg.append("line")
                        .attr("class", "focus-line")
                        .attr("stroke", tagsColor)
                        .style("pointer-events", "none");
                });

                const focus = svg.append("line")
                    .attr("class", "focus-line")
                    // .attr("r", 5)
                    .attr("fill", "#143D59")
                    .attr("stroke", "#143D59")
                    .style("display", "none")
                    .style("pointer-events", "none");

                const eventsTarget = svg;
                const otherElements = [svg, [xScale, ...yScales], [xAxis, ...yAxes], yAxesBackgrounds, paths, titlesTexts, aggregatedSensorsData, tagsTimestamps, tagsFocus, focus, chartGroup, lines, clipRect, pathsColors, ticksColors];
                return [eventsTarget, svg, ...otherElements];
            };

            const onMousemove = (event, d, svg, scales, axes, yAxesBackgrounds, paths, titlesTexts, aggregatedSensorsData, tagsTimestamps, tagsFocus, focus, ...otherElements) => {
                const [xScale, ...yScales] = scales;

                const viewBox = getElementsViewBox(axes);
                const width = viewBox.width;

                const minX = 0;
                const maxX = viewBox.right;

                const [mouseX] = getMousePosition(event, svg.node());
                const clampedMouseX = Math.min(Math.max(minX, mouseX), maxX);
                const toleranceX = width * 0.02;

                const closestPoints = paths.map((path, i) => {
                    let closestPoint = null;

                    if (minX - toleranceX < mouseX && mouseX < maxX + toleranceX) {
                        closestPoint = getClosestPathPoint(path.pathProperties, mouseX);
                        // closestPoint = getClosestDataPoint(aggregatedSensorsData[i], clampedMouseX, toleranceX, [xScale, yScales[i]]);
                        if (!closestPoint || closestPoint.x < minX || maxX < closestPoint) {
                            if (Math.abs(mouseX - minX) < toleranceX) {
                                closestPoint = getClosestPathPoint(path.pathProperties, minX);
                            } else if (Math.abs(mouseX - maxX) < toleranceX) {
                                closestPoint = getClosestPathPoint(path.pathProperties, maxX);
                            }
                        }
                    }

                    return closestPoint;
                });

                let closestPoint = null
                let closestDistance = Infinity;
                for (let i = 0; i < closestPoints.length; i++) {
                    if (closestPoints[i]) {
                        const distance = Math.abs(closestPoints[i].x - mouseX);
                        if (distance < closestDistance) {
                            closestPoint = closestPoints[i];
                            closestDistance = distance;
                        }
                    }
                }

                if (!closestPoint) {
                    focus.style("display", "none");
                    tooltipsContainersElements?.forEach(tooltipContainerElement => tooltipContainerElement?.style?.setProperty("display", "none"));
                    return;
                }

                focus.style("display", null);

                focus.attr("x1", mouseX).attr("x2", mouseX);
                focus.attr("x1", closestPoint.x).attr("x2", closestPoint.x);

                if (tooltipsContainersElements && tooltipsLabelsElements && tooltipsContainersElements.length === tooltipsLabelsElements.length) {
                    tooltipsContainersElements.forEach((tooltipContainerElement, i) => {
                        const tooltipLabelElement = tooltipsLabelsElements[i];
                        const closestPoint = closestPoints[i];
                        const path = paths[i];
                        if (tooltipContainerElement && tooltipLabelElement && closestPoint) {
                            const parent = svgElement.parentElement;
                            parent.appendChild(tooltipContainerElement);

                            const pathScreenCTM = path.node().getScreenCTM();

                            const centerX = (closestPoint.x * pathScreenCTM.a) + pathScreenCTM.e;
                            const centerY = (closestPoint.y * pathScreenCTM.d) + pathScreenCTM.f;

                            tooltipContainerElement.style.setProperty("left", centerX + "px");
                            tooltipContainerElement.style.setProperty("top", centerY + "px");
                            tooltipContainerElement.style.setProperty("position", "fixed");
                            tooltipContainerElement.style.setProperty("pointer-events", "none");
                            tooltipContainerElement.style.setProperty("display", "block");

                            const xValue = xScale.invert(closestPoint.x);
                            const yValue = yScales[i].invert(closestPoint.y);

                            const domain = yScales[i].domain();
                            const domainSpan = Math.abs(domain[1] - domain[0]);
                            const decimalPlaces = domainSpan < 15 ? 2 : 0;
                            tooltipLabelElement.textContent = `${yValue.toFixed(decimalPlaces)}`
                        }
                    });
                }
            };

            const onMouseout = (svg, scales, axes, yAxesBackgrounds, paths, titlesTexts, aggregatedSensorsData, tagsTimestamps, tagsFocus, focus, ...otherElements) => {
                focus.style("display", "none");
                if (tooltipsContainersElements && tooltipsLabelsElements && tooltipsContainersElements.length === tooltipsLabelsElements.length) {
                    tooltipsContainersElements.forEach((tooltipContainerElement, i) => {
                        const tooltipLabelElement = tooltipsLabelsElements[i];
                        if (tooltipContainerElement && tooltipLabelElement) {
                            tooltipContainerElement.style.setProperty("display", "none");
                        }
                    });
                }
            };

            const onSizeChange = (svg, scales, axes, yAxesBackgrounds, paths, titlesTexts, aggregatedSensorsData, tagsTimestamps, tagsFocus, focus, chartGroup, lines, clipRect, pathsColors, ticksColors) => {
                const [xScale, ...yScales] = scales;
                const [xAxis, ...yAxes] = axes;

                const width = svg.node().clientWidth;
                const height = svg.node().clientHeight;
                const pathHeight = height / aggregatedSensorsData.length;

                chartGroup.attr("width", width).attr("height", height);
                clipRect.attr("width", width).attr("height", height);
                paths.forEach((path, i) => path.datum(aggregatedSensorsData[i]).attr("d", lines[i]).attr("stroke", pathsColors[i]));
                paths.forEach((path, i) => path.pathProperties = window.svgPathProperties.svgPathProperties(path.attr("d")));
                paths.forEach((path, i) => path.attr("transform", `translate(0,${pathHeight * i})`));

                // text-anchor: middle;
                // transform: translate(-21px, 86px) rotate(270deg);

                focus.attr("y1", 0).attr("y2", height);

                tagsTimestamps.forEach((tagTimestamp, i) => {
                    const date = new Date(tagTimestamp);
                    const tagX = xScale(date);
                    const tagFocus = tagsFocus[i];
                    tagFocus.attr("y1", 0).attr("y2", height);
                    tagFocus.attr("x1", tagX).attr("x2", tagX);
                });

                let ticksBackgroudFlag = false;

                xAxis.call(d3.axisBottom(xScale.range([0, width])).tickFormat(d3.timeFormat("%H:%M:%S")));
                yAxes.forEach((yAxis, i) => {
                    const yScale = yScales[i].range([pathHeight, 0]);
                    const domain = yScale.domain();
                    const span = Math.abs(domain[1] - domain[0]);

                    const ticksCount = 6;

                    const axisLeft = d3.axisLeft(yScale).ticks(ticksCount).tickSize(-width).tickFormat(d3.format(`${span < 15 ? ".2f" : ".0f"}`));

                    const tickValues = yScale.ticks(ticksCount);
                    yAxesBackgrounds[i].selectAll("rect.background")
                        .data(tickValues.slice(0, -1))
                        .join("rect")
                        .attr("class", "background")
                        .attr("x", 0)
                        .attr("y", d => yScale(d))
                        .attr("width", width)
                        .attr("height", (d, j) => yScale(tickValues[j]) - yScale(tickValues[j + 1]))
                        .attr("fill", (d, j) => { ticksBackgroudFlag = !ticksBackgroudFlag; return ticksBackgroudFlag ? ticksColors[i] : "#ffffff" })
                        .attr("opacity", 0.5);

                    yAxis.call(axisLeft);

                    yAxis.selectAll("g text")
                        .attr("x", 5)
                        // .attr("dy", 0)
                        // .style("alignment-baseline", "hanging")
                        .style("text-anchor", "start");
                });

                xAxis.attr("transform", `translate(0,${height})`);
                yAxes.forEach((yAxis, i) => yAxis.attr("transform", `translate(0,${height / aggregatedSensorsData.length * i})`));
                yAxesBackgrounds.forEach((yAxis, i) => yAxis.attr("transform", `translate(0,${height / aggregatedSensorsData.length * i})`));
                xAxis.selectAll("g line").attr("color", `lightgray`);
                yAxes.forEach(yAxis => yAxis.selectAll("g line").attr("color", `#d3d3d387`));

                svg.attr("viewBox", `0 0 ${width} ${height}`);
                const yAxesViewBox = getElementsViewBox(yAxes);

                titlesTexts.forEach((title, i) => {
                    title.attr("x", yAxesViewBox.left)
                        .attr("y", yAxesViewBox.top + (height / aggregatedSensorsData.length * i) + (height / aggregatedSensorsData.length) / 2)
                        .attr("dy", "-0.5em")
                        .attr("transform", `rotate(270 ${yAxesViewBox.left} ${yAxesViewBox.top + (height / aggregatedSensorsData.length * i) + (height / aggregatedSensorsData.length) / 2})`)
                        .attr("fill", pathsColors[i])
                        .style("font-size", "1rem")
                        .style("alignment-baseline", "baseline")
                        .style("text-anchor", "middle");
                });

                const viewBox = getElementViewBox(svg);
                viewBox.top = 0;
                svg.attr("viewBox", `${viewBox.left} ${viewBox.top} ${viewBox.right - viewBox.left} ${viewBox.bottom + viewBox.top}`);

                focus.style("display", "none");
                if (tooltipsContainersElements && tooltipsLabelsElements && tooltipsContainersElements.length === tooltipsLabelsElements.length) {
                    tooltipsContainersElements.forEach((tooltipContainerElement, i) => {
                        const tooltipLabelElement = tooltipsLabelsElements[i];
                        if (tooltipContainerElement && tooltipLabelElement) {
                            tooltipContainerElement.style.setProperty("display", "none");
                        }
                    });
                }
            };

            createChart(prepareSvgElements, onMousemove, onMouseout, onSizeChange);
        }