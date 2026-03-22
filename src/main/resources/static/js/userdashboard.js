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
    panelSelect: document.getElementById('panelSelect')
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
function updateSummary() {
    const pmax = parseFloat(elements.pmax.value) || 0;
    const voc = parseFloat(elements.voc.value) || 19.83;
    const isc = parseFloat(elements.isc.value) || 10;
    const series = parseInt(elements.series.value) || 1;
    const parallel = parseInt(elements.parallel.value) || 1;
    const battV = document.querySelector('input[name="battV"]:checked').value;

    const totalW = pmax * series * parallel;
    const totalV = (voc * series * 1.2).toFixed(1);
    const totalI = isc * parallel;
    const chargeA = (totalW / (battV == 12 ? 14.7 : 29.4)).toFixed(1);

    elements.totalWatts.innerText = totalW + " W";
    elements.totalVoc.innerText = totalV + " V";
    elements.maxAmps.innerText = chargeA + " A";

    updateArrayImage();

}

//input listeners
document.querySelectorAll('input').forEach(input => {
    input.addEventListener('input', updateSummary);
});

//Dropdown auto-fill logic 
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