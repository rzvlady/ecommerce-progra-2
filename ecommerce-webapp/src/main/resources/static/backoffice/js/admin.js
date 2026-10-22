/**
 * Configuración global para el panel administrativo (Backoffice)
 * Inicializa Select2 en todos los elementos con clase .select2-global
 */
document.addEventListener('DOMContentLoaded', function () {
    initGlobalSelect2();
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

        $el.select2({
            theme: 'bootstrap-5',
            width: '100%',
            language: 'es',
            placeholder: placeholder,
            allowClear: allowClear,
            dropdownParent: $modalParent.length ? $modalParent : jQuery(document.body)
        });
    });
}
