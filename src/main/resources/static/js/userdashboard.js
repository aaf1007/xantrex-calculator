//DOM elements
const elements = {
    pmax: document.getElementById('pmax'),
    voc: document.getElementById('voc'),
    isc: document.getElementById('isc'),
    series: document.getElementById('series'),
    parallel: document.getElementById('parallel'),
    totalWatts: document.getElementById('totalWatts'),
    totalVoc: document.getElementById('totalVoc'),
    maxAmps: document.getElementById('maxAmps'),
    panelSelect: document.getElementById('panelSelect'),
    city: document.getElementById('city'),
    country: document.getElementById('country')
};

// let tempFactor = 1.2; // default temp factor for corrected Voc

// Calculation function
function updateSummary() {
    const pmax = parseFloat(elements.pmax.value) || 0;
    const voc = parseFloat(elements.voc.value) || 19.83;
    const isc = parseFloat(elements.isc.value) || 10;
    const series = parseInt(elements.series.value) || 1;
    const parallel = parseInt(elements.parallel.value) || 1;
    const battV = document.querySelector('input[name="battV"]:checked').value;

    const totalW = pmax * series * parallel;
    const totalV = (voc * series * tempFactor).toFixed(1);
    const chargeA = (totalW / (battV == 12 ? 14.7 : 29.4)).toFixed(1);

    elements.totalWatts.innerText = totalW + " W";
    elements.totalVoc.innerText = totalV + " V";
    elements.maxAmps.innerText = chargeA + " A";
}

// Input listeners
document.querySelectorAll('input').forEach(input => {
    input.addEventListener('input', updateSummary);
});

// Dropdown auto-fill logic
elements.panelSelect.addEventListener('change', function () {
    const selectedOption = this.options[this.selectedIndex];
    const pmax = selectedOption.getAttribute('data-pmax');
    const voc = selectedOption.getAttribute('data-voc');
    const isc = selectedOption.getAttribute('data-isc');

    if (pmax && voc && isc) {
        elements.pmax.value = pmax;
        elements.voc.value = voc;
        elements.isc.value = isc;
        updateSummary();
    }
});

// Fetch minimum temperature from Open-Meteo via our backend.
// Open-Meteo only needs city name — country is passed through for the form
// but the backend geocoding step resolves lat/lon from the city alone.
async function fetchTemperature() {
    const city = elements.city.value.trim();
    const country = elements.country.value.trim();

    if (!city || !country) return;

    try {
        const response = await fetch(
            `/api/weather/min-temp?city=${encodeURIComponent(city)}&country=${encodeURIComponent(country)}`
        );

        if (!response.ok) {
            console.error("Weather API error:", response.status, await response.text());
            return;
        }

        const rawText = await response.text();
        console.log("Min Temp raw:", rawText);
        const minTemp = parseFloat(rawText);
 
        if (isNaN(minTemp)) {
            console.error("Could not parse temperature:", rawText);
            return;
        }

        // add the actuall values from the chart 
        if (minTemp < -10) tempFactor = 1.3;
        else if (minTemp < 0) tempFactor = 1.25;
        else tempFactor = 1.2;

        updateSummary();
    } catch (error) {
        console.error("Temperature fetch error:", error);
    }
}

elements.city.addEventListener('change', fetchTemperature);
elements.country.addEventListener('change', fetchTemperature);


fetchTemperature();
updateSummary();