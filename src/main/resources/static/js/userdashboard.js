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
});