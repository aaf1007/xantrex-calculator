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

function updateArrayImage() {
    const series = parseInt(elements.series.value) || 1;
    const parallel = parseInt(elements.parallel.value) || 1;
    const arrayImage = document.getElementById('arrayImage');

    if (series >= 1 && series <= 6 && parallel >= 1 && parallel <= 4) {
        arrayImage.src = `/images/panels/${series}S-${parallel}P-Solar-Panel-Array-Series-Parallel.webp`;
        arrayImage.style.display = 'block';
    } else {
        arrayImage.style.display = 'none';
    }
}

//calculation function
let tempFactor = 1.2; // default temp factor for corrected Voc

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
    const chargeA = (totalW / (battV == 12 ? 14.7 : battV == 24 ? 29.4 : battV == 36 ? 44.1 : 58.8)).toFixed(1);

    elements.totalWatts.innerText = totalW + " W";
    elements.totalVoc.innerText = totalV + " V";
    elements.maxAmps.innerText = chargeA + " A";

    updateArrayImage();

}

// Input listeners
document.querySelectorAll('input').forEach(input => {
    input.addEventListener('input', updateSummary);
});

// Function to lock/unlock fields based on panel selection
function lockUnlockFields(isLocked) {
    elements.pmax.readOnly = isLocked;
    elements.voc.readOnly = isLocked;
    elements.isc.readOnly = isLocked;
    
    // Add visual feedback
    if (isLocked) {
        elements.pmax.classList.add('bg-light');
        elements.voc.classList.add('bg-light');
        elements.isc.classList.add('bg-light');
    } else {
        elements.pmax.classList.remove('bg-light');
        elements.voc.classList.remove('bg-light');
        elements.isc.classList.remove('bg-light');
    }
}

// Dropdown auto-fill logic
elements.panelSelect.addEventListener('change', function () {
    const selectedOption = this.options[this.selectedIndex];
    const selectedValue = this.value;
    const pmax = selectedOption.getAttribute('data-pmax');
    const voc = selectedOption.getAttribute('data-voc');
    const isc = selectedOption.getAttribute('data-isc');

    if (pmax && voc && isc) {
        elements.pmax.value = pmax;
        elements.voc.value = voc;
        elements.isc.value = isc;
        
        if (pmax == 0 && voc == 0 && isc == 0) {
            lockUnlockFields(false); // Unlock fields for custom
        } else {
            lockUnlockFields(true); // Lock fields for Xantrex panels
        }
        updateSummary();
    } else if (selectedValue === '') {
        lockUnlockFields(false);  // Unlock fields for empty selection
    }
    
    const image = selectedOption.getAttribute('data-image');
    const panelImage = document.getElementById('panelImage');
    if (image) {
        panelImage.src = image;
        panelImage.style.display = 'block';
    } else {
        panelImage.style.display = 'none';
    }
    updateArrayImage()
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
 
        if (minTemp < -25) tempFactor = 1.2;
        else if (minTemp < -10) tempFactor = 1.16;
        else if (minTemp < 0) tempFactor = 1.12;
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

// Error handling for location 
const cityInput    = document.getElementById('city');
    const countryInput = document.getElementById('country');
    const errorBox     = document.getElementById('locationError');
    const form         = document.querySelector('form[action*="calculator"]');
 
    function showLocationError(message) {
        errorBox.textContent = message;
        errorBox.classList.remove('d-none');
        cityInput.classList.add('is-invalid');
        countryInput.classList.add('is-invalid');
    }
 
    function clearLocationError() {
        errorBox.classList.add('d-none');
        errorBox.textContent = '';
        cityInput.classList.remove('is-invalid');
        countryInput.classList.remove('is-invalid');
    }

    async function validateLocation() {
        const city    = cityInput.value.trim();
        const country = countryInput.value.trim();
        if (!city || !country) return; 
 
        try {
            const res = await fetch(`/api/weather/min-temp?city=${encodeURIComponent(city)}&country=${encodeURIComponent(country)}`);
            if (res.ok) {
                clearLocationError();
            } else {
                const msg = await res.text();
                showLocationError(msg);
            }
        } catch (err) {
            clearLocationError();
        }
    }
 
    cityInput.addEventListener('blur', validateLocation);
    countryInput.addEventListener('blur', validateLocation);
 
    // Also block form submission if location is currently invalid
    form.addEventListener('submit', async function (e) {
        const city    = cityInput.value.trim();
        const country = countryInput.value.trim();
        if (!city || !country) return; 
 
        e.preventDefault(); 
 
        try {
            const res = await fetch(`/api/weather/min-temp?city=${encodeURIComponent(city)}&country=${encodeURIComponent(country)}`);
            if (res.ok) {
                clearLocationError();
                form.submit(); 
            } else {
                const msg = await res.text();
                showLocationError(msg);
                
                errorBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        } catch (err) {
            form.submit(); 
        }
    });