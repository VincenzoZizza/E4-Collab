
export function getDateRange(timestamp, mode, prevNext) {
            const range = {}
            const date = new Date(timestamp);

            const dateFormatOptions = {
                year: 'numeric',
                month: 'numeric',
                day: 'numeric'
            };

            if (mode === 'year') {
                range.from = new Date(prevNext ? date.getFullYear() - 1 : date.getFullYear(), 0, 1);
                range.to = new Date(prevNext ? date.getFullYear() + 1 : date.getFullYear(), 11, 31);
                const fromText = range.from.toLocaleDateString('en-GB', dateFormatOptions);
                const toText = range.to.toLocaleDateString('en-GB', dateFormatOptions);
                range.label = `${fromText} - ${toText}`;
            } else if (mode === 'month') {
                range.from = new Date(date.getFullYear(), prevNext ? date.getMonth() - 1 : date.getMonth(), 1);
                range.to = new Date(date.getFullYear(), prevNext ? date.getMonth() + 2 : date.getMonth() + 1, 0);
                const fromText = range.from.toLocaleDateString('en-GB', dateFormatOptions);
                const toText = range.to.toLocaleDateString('en-GB', dateFormatOptions);
                range.label = `${fromText} - ${toText}`;
            } else if (mode === 'day') {
                range.from = new Date(date.getFullYear(), date.getMonth(), prevNext ? date.getDate() - 1 : date.getDate(), 0, 0, 0, 0);
                range.to = new Date(date.getFullYear(), date.getMonth(), prevNext ? date.getDate() + 1 : date.getDate(), 23, 59, 59, 999);
                range.label = range.from.toLocaleDateString('en-GB', dateFormatOptions);
            }

            return range;
        }


export function compareDates(timestampA, timestampB, mode) {
            const a = new Date(timestampA);
            const b = new Date(timestampB);

            let result = a.getFullYear() === b.getFullYear();
            if (mode === 'year') {
                return result;
            }

            result &= a.getMonth() === b.getMonth();
            if (mode === 'month') {
                return result;
            }

            return result && a.getDate() === b.getDate();
        }
export function formatDateUTC(date, timeOnly) {
            const timeOptions = { hour: '2-digit', minute: '2-digit', hour12: false, timeZoneName: 'longOffset' };
            const formattedTime = date.toLocaleTimeString('en-GB', timeOptions);

            if (timeOnly) {
                return formattedTime;
            }

            const dateOptions = { day: '2-digit', month: 'short', year: 'numeric' };
            const formattedDate = date.toLocaleDateString('en-GB', dateOptions);

            return `${formattedDate} - ${formattedTime}`;
        }
export function formatDuration(duration, decimalPlaces = 0) {
            const totalSeconds = duration / 1000;
            const hours = Math.floor(totalSeconds / 3600).toString().padStart(2, '0');
            const minutes = Math.floor((totalSeconds % 3600) / 60).toString().padStart(2, '0');
            const seconds = (totalSeconds % 60).toFixed(decimalPlaces).padStart(2, '0');
        
            return `${hours}h ${minutes}m ${seconds}s`;
        };
        
export function getLast12MonthsRange() {
            const today = new Date();
            const fromMonth = new Date(today.getFullYear() - 1, today.getMonth() + 1, 1);
            const toMonth = new Date(today.getFullYear(), today.getMonth(), 1);
            return [fromMonth, toMonth];
        }
export function createActionButton(buttonClasses, iconClasses, textContent, onClick, tooltip) {
            const actionButton = document.createElement('button');
            actionButton.onclick = onClick;
            actionButton.classList.add(...buttonClasses);
            const buttonIcon = document.createElement('i');
            buttonIcon.classList.add(...iconClasses);
            actionButton.appendChild(buttonIcon);
            if(textContent)
            {
                const buttonText = document.createElement('span');
                // buttonText.classList.add('d-lg-inline', 'd-sm-none');
                buttonText.innerText = textContent;
                actionButton.appendChild(buttonText);
            }
            if (tooltip) {
                actionButton.setAttribute('data-bs-toggle', 'tooltip');
                actionButton.setAttribute('data-bs-placement', 'top');
                actionButton.setAttribute('title', tooltip);
                new bootstrap.Tooltip(actionButton);
            }
            return actionButton;
        }
export function isHTMLElementVisible(htmlElement) {
            if (!htmlElement) {
                return false;
            }

            const style = getComputedStyle(htmlElement);
            return (style.display !== "none" && style.visibility !== "hidden" && style.opacity !== "0");
        }
export function getClosestNonVisibleParent(htmlElement) {
            if (!htmlElement) {
                return null;
            }

            while (htmlElement) {
                if (!utils.isHTMLElementVisible(htmlElement)) {
                    return htmlElement;
                }
                htmlElement = htmlElement.parentElement;
            }
            return null;
        }
export function ensureHTMLElementVisible(htmlElement) {
            return new Promise((resolve, reject) => {
                if (!htmlElement) {
                    reject();
                }

                let nonVisibleParent = utils.getClosestNonVisibleParent(htmlElement);

                if (nonVisibleParent) {
                    const observer = new MutationObserver((mutations) => {
                        setTimeout(async () => {
                            if (utils.isHTMLElementVisible(nonVisibleParent)) {
                                observer.disconnect();
                                await utils.ensureHTMLElementVisible(htmlElement);
                                resolve();
                            }
                        }, 100);
                    })
                    observer.observe(nonVisibleParent, {
                        childList: false,
                        subtree: false,
                        attributes: true,
                        attributeOldValue: false,
                        attributeFilter: ['style', 'class'],
                        characterData: false,
                        characterDataOldValue: false
                    });
                }
                else {
                    resolve();
                }
            })
        }