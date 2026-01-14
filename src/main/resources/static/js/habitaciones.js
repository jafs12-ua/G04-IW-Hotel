// Función para actualizar el precio total con animaciones coordinadas
function actualizarPrecio(selectElement) {
    const tipoId = selectElement.id.replace('modelo-', '');
    const precioBase = parseFloat(selectElement.dataset.basePrice);
    const precioModelo = parseFloat(selectElement.value);
    const precioTotal = precioBase + precioModelo;

    // Obtener elementos
    const card = selectElement.closest('.room-detail-card');
    const precioElement = document.getElementById('precio-' + tipoId);
    const priceBox = card.querySelector('.room-price');
    const btn = card.querySelector('.btn-primary');

    // Actualizar el precio mostrado
    precioElement.textContent = precioTotal.toFixed(2) + '€';

    // Animación coordinada - todos empiezan y terminan juntos
    const duration = 500; // 500ms de duración

    // Activar todas las animaciones simultáneamente
    if (priceBox) {
        priceBox.classList.add('highlight');
    }
    precioElement.classList.add('highlight');
    if (btn) {
        btn.classList.add('highlight');
    }

    // Desactivar todas las animaciones simultáneamente después de la duración
    setTimeout(() => {
        if (priceBox) priceBox.classList.remove('highlight');
        precioElement.classList.remove('highlight');
        if (btn) btn.classList.remove('highlight');
    }, duration);

    // Actualizar descripción del modelo
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const descripcion = selectedOption.dataset.descripcion || '';
    document.getElementById('desc-' + tipoId).textContent = descripcion;
}

// Inicializar precios al cargar la página
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.modelo-select').forEach(function (select) {
        // Solo actualizar descripción sin animación al cargar
        const tipoId = select.id.replace('modelo-', '');
        const precioBase = parseFloat(select.dataset.basePrice);
        const precioModelo = parseFloat(select.value);
        const precioTotal = precioBase + precioModelo;

        const precioElement = document.getElementById('precio-' + tipoId);
        precioElement.textContent = precioTotal.toFixed(2) + '€';

        const selectedOption = select.options[select.selectedIndex];
        const descripcion = selectedOption.dataset.descripcion || '';
        document.getElementById('desc-' + tipoId).textContent = descripcion;
    });
});

// Función para reservar habitación (solo para usuarios autenticados)
function reservarHabitacion(btnElement) {
    const tipoId = btnElement.dataset.tipoId;
    const tipoNombre = btnElement.dataset.tipoNombre;

    // Obtener el modelo seleccionado
    const selectModelo = document.getElementById('modelo-' + tipoId);
    const modeloIndex = selectModelo.selectedIndex;

    // Construir URL con parámetros
    const params = new URLSearchParams({
        tipoId: tipoId,
        tipoNombre: tipoNombre,
        modeloIndex: modeloIndex
    });

    // Redirigir a nueva reserva con parámetros
    window.location.href = '/cliente/nueva-reserva?' + params.toString();
}
