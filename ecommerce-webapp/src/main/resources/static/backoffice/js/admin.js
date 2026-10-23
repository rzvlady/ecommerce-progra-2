/**
 * Configuración global para el panel administrativo (Backoffice)
 * Inicializa Select2 en todos los elementos con clase .select2-global
 */
document.addEventListener('DOMContentLoaded', function () {
    initGlobalSelect2();

    // Re-inicializar Select2 cuando se abre cualquier modal de Bootstrap para asegurar ancho correcto y foco
    if (window.jQuery) {
        jQuery(document).on('shown.bs.modal', '.modal', function () {
            initGlobalSelect2(this);
        });
    }
});

function initGlobalSelect2(context) {
    if (typeof window.jQuery === 'undefined' || typeof jQuery.fn.select2 === 'undefined') {
        return;
    }

    const $scope = context ? jQuery(context) : jQuery(document);

    $scope.find('.select2-global').each(function () {
        const $el = jQuery(this);
        const $modalParent = $el.closest('.modal');
        const placeholder = $el.attr('data-placeholder') || 'Seleccione una opción...';
        const allowClear = $el.attr('data-allow-clear') === 'true';

        // Si ya tenía select2 inicializado, reinicializar limpiamente
        if ($el.hasClass('select2-hidden-accessible')) {
            $el.select2('destroy');
        }

        $el.select2({
            theme: 'bootstrap-5',
            width: '100%',
            language: 'es',
            placeholder: placeholder,
            allowClear: allowClear,
            dropdownParent: $modalParent.length ? $modalParent : jQuery(document.body),
            templateResult: formatSelect2Option,
            templateSelection: formatSelect2Selection
        });
    });
}

function formatSelect2Option(state) {
    if (!state.id || !state.element) {
        return state.text;
    }
    const opt = state.element;
    const nivel = opt.getAttribute('data-nivel');
    if (!nivel) {
        return state.text;
    }
    const rutaPadres = opt.getAttribute('data-ruta-padres') || '';
    const nombre = opt.getAttribute('data-nombre') || state.text;
    const indentPx = (Math.max(1, parseInt(nivel, 10)) - 1) * 12;

    const wrapper = document.createElement('div');
    wrapper.className = 'd-flex align-items-center justify-content-between gap-2';
    wrapper.style.paddingLeft = indentPx + 'px';

    const left = document.createElement('div');
    left.className = 'text-truncate';
    if (rutaPadres) {
        const spanPadres = document.createElement('span');
        spanPadres.className = 'text-secondary small';
        spanPadres.textContent = rutaPadres + ' › ';
        left.appendChild(spanPadres);
    }
    const spanNombre = document.createElement('span');
    spanNombre.className = 'fw-semibold text-dark';
    spanNombre.textContent = nombre;
    left.appendChild(spanNombre);

    const badge = document.createElement('span');
    badge.className = 'taxonomy-level-pill nivel-' + nivel;
    badge.textContent = 'N' + nivel;

    wrapper.appendChild(left);
    wrapper.appendChild(badge);
    return wrapper;
}

function formatSelect2Selection(state) {
    if (!state.id || !state.element) {
        return state.text;
    }
    const opt = state.element;
    const nivel = opt.getAttribute('data-nivel');
    if (!nivel) {
        return state.text;
    }
    const rutaPadres = opt.getAttribute('data-ruta-padres') || '';
    const nombre = opt.getAttribute('data-nombre') || state.text;

    const wrapper = document.createElement('span');
    if (rutaPadres) {
        const spanPadres = document.createElement('span');
        spanPadres.className = 'text-secondary';
        spanPadres.textContent = rutaPadres + ' › ';
        wrapper.appendChild(spanPadres);
    }
    const spanNombre = document.createElement('span');
    spanNombre.className = 'fw-semibold text-dark';
    spanNombre.textContent = nombre;
    wrapper.appendChild(spanNombre);
    return wrapper;
}
