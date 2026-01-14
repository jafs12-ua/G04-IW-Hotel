// Actualizar precio cuando cambia el modelo de reserva
function actualizarPrecio(selectElement) {
    const tipoId = selectElement.id.replace('modelo-', '');
    const basePrice = parseFloat(selectElement.getAttribute('data-base-price'));
    const adicional = parseFloat(selectElement.value);
    const precioTotal = basePrice + adicional;

    document.getElementById('precio-' + tipoId).textContent = precioTotal + '€';

    // Mostrar descripción del modelo
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const descripcion = selectedOption.getAttribute('data-descripcion');
    document.getElementById('desc-' + tipoId).textContent = descripcion;
}

// Inicializar descripciones al cargar la página
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.modelo-select').forEach(select => {
        actualizarPrecio(select);
    });
});

// Función para reservar habitación con las fechas y cantidad de adultos pre-establecidas
function reservarHabitacionConFechas(button) {
    const tipoId = button.getAttribute('data-tipo-id');
    const tipoNombre = button.getAttribute('data-tipo-nombre');
    const fechaInicio = button.getAttribute('data-fecha-inicio');
    const fechaFin = button.getAttribute('data-fecha-fin');
    const cantidadAdultos = button.getAttribute('data-cantidad-adultos');
    const selectModelo = document.getElementById('modelo-' + tipoId);
    const selectedOption = selectModelo.options[selectModelo.selectedIndex];
    const modeloNombre = selectedOption.text.split(' (+')[0];

    // Redirigir a la página de nueva reserva con los parámetros
    window.location.href = `/cliente/nueva-reserva?tipoId=${tipoId}&fechaInicio=${fechaInicio}&fechaFin=${fechaFin}&cantidadAdultos=${cantidadAdultos}`;
}

// Función para reservar sin pre-establecer fechas (para la página de habitaciones normal)
function reservarHabitacion(button) {
    const tipoId = button.getAttribute('data-tipo-id');
    window.location.href = `/cliente/nueva-reserva?tipoId=${tipoId}`;
}
