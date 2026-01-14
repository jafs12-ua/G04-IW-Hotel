
// Funciones y lógica para nueva reserva

function initReservaCliente() {
    // Establecer fecha mínima como hoy
    const fechaInicioInput = document.getElementById('fechaInicio');
    const fechaFinInput = document.getElementById('fechaFin');

    if (!fechaInicioInput || !fechaFinInput) return;

    const hoy = new Date().toISOString().split('T')[0];
    fechaInicioInput.setAttribute('min', hoy);
    fechaFinInput.setAttribute('min', hoy);

    // Actualizar fecha mínima de salida cuando cambia la entrada
    fechaInicioInput.addEventListener('change', function () {
        const fechaInicio = this.value;
        if (fechaInicio) {
            const fechaMinSalida = new Date(fechaInicio);
            fechaMinSalida.setDate(fechaMinSalida.getDate() + 1);
            fechaFinInput.setAttribute('min', fechaMinSalida.toISOString().split('T')[0]);
            // Also update value if current is less
            const currentFin = fechaFinInput.value;
            if (currentFin && new Date(currentFin) <= new Date(fechaInicio)) {
                fechaFinInput.value = fechaMinSalida.toISOString().split('T')[0];
            }
        }
        calculateTotal();
    });

    fechaFinInput.addEventListener('change', calculateTotal);

    const idTipoHab = document.getElementById('idTipoHabitacion');
    if (idTipoHab) idTipoHab.addEventListener('change', calculateTotal);

    const idModelo = document.getElementById('idModeloReserva');
    if (idModelo) idModelo.addEventListener('change', calculateTotal);

    const idSala = document.getElementById('idSala');
    if (idSala) idSala.addEventListener('change', calculateTotal);

    // Validar que fecha fin sea posterior a fecha inicio
    const form = document.getElementById('reservaForm'); // Added ID in HTML earlier? No, I need to check.
    // In the HTML: <form ... id="reservaForm">. Yes, typically. Let's check existing HTML.
    // Line 593: <form th:action="@{/cliente/nueva-reserva}" method="POST" th:object="${reservaDTO}" id="reservaForm">
    // Yes it has id reservaForm.

    if (form) {
        form.addEventListener('submit', function (e) {
            const fechaInicio = new Date(fechaInicioInput.value);
            const fechaFin = new Date(fechaFinInput.value);
            const tipoReserva = document.getElementById('tipoReservaInput').value;

            if (fechaFin <= fechaInicio) {
                e.preventDefault();
                alert('La fecha de salida debe ser posterior a la fecha de entrada.');
                return;
            }

            // Validar campos requeridos según tipo
            if (tipoReserva === 'HABITACION') {
                const tipoHab = document.getElementById('idTipoHabitacion').value;
                if (!tipoHab) {
                    e.preventDefault();
                    alert('Por favor, selecciona un tipo de habitación.');
                    return;
                }
            } else if (tipoReserva === 'SALA') {
                const sala = document.getElementById('idSala').value;
                if (!sala) {
                    e.preventDefault();
                    alert('Por favor, selecciona una sala.');
                    return;
                }
            }
        });
    }

    calculateTotal();
}

// Función para cambiar tipo de reserva
function selectType(tipo) {
    const habitacionFields = document.getElementById('habitacionFields');
    const salaFields = document.getElementById('salaFields');
    const typeHabitacion = document.getElementById('type-habitacion');
    const typeSala = document.getElementById('type-sala');
    const tipoInput = document.getElementById('tipoReservaInput');
    const formHeader = document.getElementById('formHeaderTitle');
    const summaryTipo = document.getElementById('summaryTipoReserva');
    const summaryInfoHab = document.getElementById('summaryInfoHabitacion');
    const summaryInfoSala = document.getElementById('summaryInfoSala');
    const labelFechaInicio = document.getElementById('labelFechaInicio');
    const labelFechaFin = document.getElementById('labelFechaFin');
    const helpNumPersonas = document.getElementById('helpNumPersonas');
    const numPersonasInput = document.getElementById('numPersonas');

    if (tipo === 'HABITACION') {
        habitacionFields.classList.add('active');
        salaFields.classList.remove('active');
        typeHabitacion.classList.add('active');
        typeSala.classList.remove('active');
        tipoInput.value = 'HABITACION';
        formHeader.textContent = 'Detalles de la Reserva de Habitación';
        summaryTipo.textContent = 'Habitación';
        summaryInfoHab.style.display = 'block';
        summaryInfoSala.style.display = 'none';
        labelFechaInicio.textContent = 'Fecha de Entrada *';
        labelFechaFin.textContent = 'Fecha de Salida *';
        helpNumPersonas.textContent = 'Verifica que la habitación tenga capacidad suficiente';
        if (numPersonasInput) numPersonasInput.max = 10;
    } else {
        habitacionFields.classList.remove('active');
        salaFields.classList.add('active');
        typeHabitacion.classList.remove('active');
        typeSala.classList.add('active');
        tipoInput.value = 'SALA';
        formHeader.textContent = 'Detalles de la Reserva de Sala';
        summaryTipo.textContent = 'Sala';
        summaryInfoHab.style.display = 'none';
        summaryInfoSala.style.display = 'block';
        labelFechaInicio.textContent = 'Fecha de Inicio *';
        labelFechaFin.textContent = 'Fecha de Fin *';
        helpNumPersonas.textContent = 'Verifica que la sala tenga aforo suficiente';
        if (numPersonasInput) numPersonasInput.max = 200;
    }
    calculateTotal();
}

function calculateTotal() {
    const fechaInicioElement = document.getElementById('fechaInicio');
    const fechaFinElement = document.getElementById('fechaFin');

    if (!fechaInicioElement || !fechaFinElement) return;

    const fechaInicio = new Date(fechaInicioElement.value);
    const fechaFin = new Date(fechaFinElement.value);
    const tipoReserva = document.getElementById('tipoReservaInput').value;
    const summaryTotal = document.getElementById('summaryTotal');

    // Check valid dates
    if (!fechaInicioElement.value || !fechaFinElement.value || isNaN(fechaInicio.getTime()) || isNaN(fechaFin.getTime()) || fechaFin <= fechaInicio) {
        if (summaryTotal) summaryTotal.textContent = '0.00€';
        return;
    }

    const diffTime = Math.abs(fechaFin - fechaInicio);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    let total = 0;

    if (tipoReserva === 'HABITACION') {
        const tipoSelect = document.getElementById('idTipoHabitacion');
        const modeloSelect = document.getElementById('idModeloReserva');

        const precioBase = parseFloat(tipoSelect?.options[tipoSelect.selectedIndex]?.dataset.precio || 0);
        const precioModelo = parseFloat(modeloSelect?.options[modeloSelect.selectedIndex]?.dataset.precio || 0);

        total = (precioBase + precioModelo) * diffDays;

    } else { // SALA
        const salaSelect = document.getElementById('idSala');
        const salaSelectIndex = salaSelect?.selectedIndex;
        const precioSala = parseFloat(salaSelect?.options[salaSelectIndex]?.dataset.precio || 0);

        total = precioSala * diffDays;
    }

    if (summaryTotal) summaryTotal.textContent = total.toFixed(2) + '€';
}

document.addEventListener('DOMContentLoaded', initReservaCliente);
