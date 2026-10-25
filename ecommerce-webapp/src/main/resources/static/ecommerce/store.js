/**
 * MultiStore - store.js
 * Funcionalidades interactivas para el E-Commerce:
 * 1. Buscador reactivo en Header (productos, categorías y marcas con dropdown)
 * 2. Buscador reactivo en Catálogo (/productos - filtrado en vivo de tarjetas)
 * 3. Filtros Select2 en Catálogo (/productos)
 * 4. Navegación responsive del menú de categorías (flechas, fade y drawer)
 */
document.addEventListener('DOMContentLoaded', function () {
    initReactiveHeaderSearch();
    initLiveCatalogGridFilter();
    initCatalogFilters();
    initCategoryScrollBar();
});

/* ==========================================================================
   1. Buscador Reactivo en Header (Categorías, Marcas y Productos con Dropdown)
   ========================================================================== */
function initReactiveHeaderSearch() {
    const input = document.getElementById('storeSearchInput');
    const dropdown = document.getElementById('searchSuggestionsBox');
    const spinner = document.getElementById('searchSpinner');
    const clearBtn = document.getElementById('btnClearSearch');

    if (!input || !dropdown) return;

    let debounceTimer = null;
    let abortController = null;
    let currentSelectedIndex = -1;

    input.addEventListener('input', function () {
        const query = input.value.trim();

        if (clearBtn) {
            if (input.value.length > 0) {
                clearBtn.classList.remove('d-none');
            } else {
                clearBtn.classList.add('d-none');
            }
        }

        if (query.length === 0) {
            dropdown.classList.add('d-none');
            if (abortController) abortController.abort();
            setLoading(false);
            return;
        }

        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            ejecutarBusqueda(query);
        }, 180);
    });

    if (clearBtn) {
        clearBtn.addEventListener('click', function () {
            input.value = '';
            clearBtn.classList.add('d-none');
            dropdown.classList.add('d-none');
            input.focus();
        });
    }

    input.addEventListener('focus', function () {
        const query = input.value.trim();
        if (query.length > 0 && dropdown.innerHTML.trim() !== '') {
            dropdown.classList.remove('d-none');
        }
    });

    document.addEventListener('click', function (e) {
        if (!input.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.add('d-none');
        }
    });

    input.addEventListener('keydown', function (e) {
        const items = dropdown.querySelectorAll('.suggestion-navigable-item');
        if (items.length === 0) return;

        if (e.key === 'ArrowDown') {
            e.preventDefault();
            currentSelectedIndex = (currentSelectedIndex + 1) % items.length;
            actualizarSeleccionTeclado(items);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            currentSelectedIndex = (currentSelectedIndex - 1 + items.length) % items.length;
            actualizarSeleccionTeclado(items);
        } else if (e.key === 'Enter') {
            if (currentSelectedIndex >= 0 && currentSelectedIndex < items.length) {
                e.preventDefault();
                items[currentSelectedIndex].click();
            }
        } else if (e.key === 'Escape') {
            dropdown.classList.add('d-none');
        }
    });

    function actualizarSeleccionTeclado(items) {
        items.forEach((item, index) => {
            if (index === currentSelectedIndex) {
                item.classList.add('selected-by-keyboard');
                item.scrollIntoView({ block: 'nearest' });
            } else {
                item.classList.remove('selected-by-keyboard');
            }
        });
    }

    function setLoading(isLoading) {
        if (!spinner) return;
        if (isLoading) {
            spinner.classList.remove('d-none');
            if (clearBtn) clearBtn.classList.add('d-none');
        } else {
            spinner.classList.add('d-none');
            if (clearBtn && input.value.length > 0) clearBtn.classList.remove('d-none');
        }
    }

    function ejecutarBusqueda(query) {
        if (abortController) abortController.abort();
        abortController = new AbortController();

        setLoading(true);

        const url = `/api/busqueda/sugerencias?q=${encodeURIComponent(query)}`;

        fetch(url, { signal: abortController.signal })
            .then(response => {
                if (!response.ok) throw new Error('Error en sugerencias');
                return response.json();
            })
            .then(data => {
                setLoading(false);
                renderizarSugerenciasHeader(data, query);
            })
            .catch(err => {
                if (err.name !== 'AbortError') {
                    setLoading(false);
                }
            });
    }

    function renderizarSugerenciasHeader(data, query) {
        currentSelectedIndex = -1;
        const total = (data.productos?.length || 0) + (data.categorias?.length || 0) + (data.marcas?.length || 0);

        if (total === 0) {
            dropdown.innerHTML = `
                <div class="p-4 text-center text-muted">
                    <i class="bi bi-search fs-3 text-secondary d-block mb-2"></i>
                    <p class="mb-1 fw-semibold text-dark">No se encontraron sugerencias para "${escapeHtml(query)}"</p>
                    <small>Presiona <kbd class="bg-light text-dark border">Enter</kbd> para buscar en todo el catálogo.</small>
                </div>
            `;
            dropdown.classList.remove('d-none');
            return;
        }

        let html = '<div class="suggestions-inner-wrap">';

        // 1. Categorías sugeridas
        if (data.categorias && data.categorias.length > 0) {
            html += `
                <div class="suggestion-section">
                    <div class="suggestion-section-title">
                        <i class="bi bi-grid text-warning me-1"></i> Categorías
                    </div>
                    <div class="suggestion-chips-grid">
            `;
            data.categorias.forEach(cat => {
                html += `
                    <a href="${cat.url}" class="suggestion-chip suggestion-navigable-item" title="Ver categoría ${escapeHtml(cat.nombreCategoria)}">
                        <i class="bi bi-tag-fill me-1 small opacity-75"></i>
                        <span>${resaltarCoincidencia(cat.nombreCategoria, query)}</span>
                    </a>
                `;
            });
            html += `
                    </div>
                </div>
            `;
        }

        // 2. Marcas sugeridas
        if (data.marcas && data.marcas.length > 0) {
            html += `
                <div class="suggestion-section">
                    <div class="suggestion-section-title">
                        <i class="bi bi-award text-warning me-1"></i> Marcas
                    </div>
                    <div class="suggestion-chips-grid">
            `;
            data.marcas.forEach(marca => {
                html += `
                    <a href="${marca.url}" class="suggestion-chip suggestion-navigable-item" title="Ver marca ${escapeHtml(marca.nombreMarca)}">
                        <i class="bi bi-patch-check-fill me-1 small opacity-75"></i>
                        <span>${resaltarCoincidencia(marca.nombreMarca, query)}</span>
                    </a>
                `;
            });
            html += `
                    </div>
                </div>
            `;
        }

        // 3. Productos sugeridos
        if (data.productos && data.productos.length > 0) {
            html += `
                <div class="suggestion-section">
                    <div class="suggestion-section-title">
                        <i class="bi bi-box-seam text-warning me-1"></i> Productos recomendados
                    </div>
                    <div class="suggestion-products-list">
            `;
            data.productos.forEach(prod => {
                const precio = prod.precioVenta ? `$${parseFloat(prod.precioVenta).toFixed(2)}` : '$0.00';
                const img = prod.imagenUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=120&q=80';

                html += `
                    <a href="${prod.url}" class="suggestion-product-row suggestion-navigable-item">
                        <div class="suggestion-product-thumb">
                            <img src="${img}" alt="${escapeHtml(prod.nombreProducto)}" loading="lazy">
                        </div>
                        <div class="suggestion-product-info">
                            <span class="suggestion-product-name">${resaltarCoincidencia(prod.nombreProducto, query)}</span>
                            <div class="suggestion-product-meta">
                                ${prod.nombreMarca ? `<span class="badge-meta brand">${escapeHtml(prod.nombreMarca)}</span>` : ''}
                                ${prod.nombreCategoria ? `<span class="badge-meta cat">${escapeHtml(prod.nombreCategoria)}</span>` : ''}
                                <span class="sku-code text-muted">${escapeHtml(prod.codigoSku || '')}</span>
                            </div>
                        </div>
                        <div class="suggestion-product-price">
                            ${precio}
                        </div>
                        <i class="bi bi-chevron-right text-muted small ms-2"></i>
                    </a>
                `;
            });
            html += `
                    </div>
                </div>
            `;
        }

        // Pie de resultados
        html += `
            <div class="suggestion-footer">
                <a href="/productos?busqueda=${encodeURIComponent(query)}" class="suggestion-footer-link suggestion-navigable-item">
                    <span>Ver todos los resultados para <strong>"${escapeHtml(query)}"</strong></span>
                    <i class="bi bi-arrow-right ms-2"></i>
                </a>
            </div>
        `;

        html += '</div>';
        dropdown.innerHTML = html;
        dropdown.classList.remove('d-none');
    }
}

/* ==========================================================================
   2. Buscador Reactivo en Catálogo (/productos - FILTRADO EN VIVO DE GRILLA)
   ========================================================================== */
function initLiveCatalogGridFilter() {
    const input = document.getElementById('catalogSearchInput');
    const clearBtn = document.getElementById('btnCatalogClearSearch');
    const grid = document.getElementById('catalogProductsGrid');
    const countElem = document.getElementById('catalogProductsCount');
    const labelElem = document.getElementById('catalogProductsLabel');
    const liveEmptyState = document.getElementById('catalogLiveEmptyState');
    const queryTermSpan = document.getElementById('liveSearchQueryTerm');
    const resetBtn = document.getElementById('btnResetLiveSearch');

    if (!input || !grid) return;

    const cards = Array.from(grid.querySelectorAll('.product-item-col'));

    // Normalizar texto para ignorar acentos, mayúsculas y espacios extras
    function normalizar(texto) {
        if (!texto) return '';
        return texto
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase()
            .trim();
    }

    function filtrarGrilla() {
        const query = normalizar(input.value);

        // Mostrar / ocultar botón de limpieza
        if (clearBtn) {
            if (input.value.length > 0) {
                clearBtn.classList.remove('d-none');
            } else {
                clearBtn.classList.add('d-none');
            }
        }

        let visibles = 0;

        cards.forEach(card => {
            if (!query) {
                card.classList.remove('d-none');
                visibles++;
                return;
            }

            const nombre = normalizar(card.getAttribute('data-nombre') || '');
            const sku = normalizar(card.getAttribute('data-sku') || '');
            const marca = normalizar(card.getAttribute('data-marca') || '');
            const categoria = normalizar(card.getAttribute('data-categoria') || '');

            const coincide = nombre.includes(query)
                || sku.includes(query)
                || marca.includes(query)
                || categoria.includes(query);

            if (coincide) {
                card.classList.remove('d-none');
                visibles++;
            } else {
                card.classList.add('d-none');
            }
        });

        // Actualizar contador numérico y texto
        if (countElem) countElem.textContent = visibles;
        if (labelElem) {
            labelElem.textContent = visibles === 1 ? 'producto encontrado' : 'productos encontrados';
        }

        // Mostrar u ocultar estado vacío dinámico
        if (liveEmptyState) {
            if (visibles === 0 && query.length > 0) {
                liveEmptyState.classList.remove('d-none');
                grid.classList.add('d-none');
                if (queryTermSpan) queryTermSpan.textContent = input.value.trim();
            } else {
                liveEmptyState.classList.add('d-none');
                grid.classList.remove('d-none');
            }
        }
    }

    // Filtrar de manera reactiva instantánea al escribir cada letra
    input.addEventListener('input', filtrarGrilla);

    // Evitar recargar la página innecesariamente al presionar Enter
    input.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
        }
    });

    // Función para reiniciar búsqueda
    function resetear() {
        input.value = '';
        if (clearBtn) clearBtn.classList.add('d-none');
        filtrarGrilla();
        input.focus();
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', resetear);
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', resetear);
    }

    // Si la página se carga con un valor inicial de búsqueda
    if (input.value.trim().length > 0) {
        filtrarGrilla();
    }
}

/* ==========================================================================
   3. Filtros Select2 en Catálogo (/productos)
   ========================================================================== */
function initCatalogFilters() {
    function setupSelect2() {
        if (typeof $ !== 'undefined' && $.fn && $.fn.select2) {
            $('.select2-catalogo').each(function () {
                const $select = $(this);
                const placeholder = $select.data('placeholder') || 'Seleccionar...';

                $select.select2({
                    theme: 'bootstrap-5',
                    language: 'es',
                    width: '100%',
                    allowClear: true,
                    placeholder: placeholder
                }).on('change', function () {
                    // Auto-filtrado reactivo al cambiar la selección
                    $(this).closest('form').submit();
                });
            });
        }
    }

    if (typeof $ !== 'undefined' && $.fn && $.fn.select2) {
        setupSelect2();
    } else {
        window.addEventListener('load', setupSelect2);
    }
}

/* ==========================================================================
   4. Menú de Categorías Responsive (Scroll + Indicadores)
   ========================================================================== */
function initCategoryScrollBar() {
    const container = document.getElementById('categoryScrollContainer');
    const btnLeft = document.getElementById('btnCatScrollLeft');
    const btnRight = document.getElementById('btnCatScrollRight');
    const fadeLeft = document.getElementById('catFadeLeft');
    const fadeRight = document.getElementById('catFadeRight');

    if (!container) return;

    function actualizarControlesScroll() {
        const scrollLeft = Math.round(container.scrollLeft);
        const maxScroll = Math.round(container.scrollWidth - container.clientWidth);
        const hayOverflow = maxScroll > 4;

        if (!hayOverflow) {
            if (btnLeft) btnLeft.classList.add('d-none');
            if (btnRight) btnRight.classList.add('d-none');
            if (fadeLeft) fadeLeft.classList.add('d-none');
            if (fadeRight) fadeRight.classList.add('d-none');
            return;
        }

        if (scrollLeft > 6) {
            if (btnLeft) btnLeft.classList.remove('d-none');
            if (fadeLeft) fadeLeft.classList.remove('d-none');
        } else {
            if (btnLeft) btnLeft.classList.add('d-none');
            if (fadeLeft) fadeLeft.classList.add('d-none');
        }

        if (scrollLeft < maxScroll - 6) {
            if (btnRight) btnRight.classList.remove('d-none');
            if (fadeRight) fadeRight.classList.remove('d-none');
        } else {
            if (btnRight) btnRight.classList.add('d-none');
            if (fadeRight) fadeRight.classList.add('d-none');
        }
    }

    if (btnLeft) {
        btnLeft.addEventListener('click', function () {
            container.scrollBy({ left: -240, behavior: 'smooth' });
        });
    }

    if (btnRight) {
        btnRight.addEventListener('click', function () {
            container.scrollBy({ left: 240, behavior: 'smooth' });
        });
    }

    container.addEventListener('scroll', actualizarControlesScroll, { passive: true });
    window.addEventListener('resize', actualizarControlesScroll);

    const activeLink = container.querySelector('.category-nav-link.active');
    if (activeLink) {
        setTimeout(() => {
            activeLink.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
            actualizarControlesScroll();
        }, 150);
    } else {
        actualizarControlesScroll();
    }
}

/* ==========================================================================
   Utilidades Globales
   ========================================================================== */
function resaltarCoincidencia(texto, termino) {
    if (!texto || !termino) return escapeHtml(texto || '');
    const regex = new RegExp(`(${termino.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    const partes = texto.split(regex);
    return partes.map(parte => {
        if (parte.toLowerCase() === termino.toLowerCase()) {
            return `<strong class="text-mustard">${escapeHtml(parte)}</strong>`;
        }
        return escapeHtml(parte);
    }).join('');
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

/* ==========================================================================
   5. Modal de Autenticación Moderno (Login / Registro / Password)
   ========================================================================== */
window.abrirModalAuth = function (tab = 'login', callback = null) {
    const modalEl = document.getElementById('authModal');
    if (!modalEl) {
        window.location.href = tab === 'register' ? '/registro' : '/inicio-sesion';
        return;
    }

    if (callback) {
        window.__authSuccessCallback = callback;
    }

    // Cerrar cualquier dropdown de perfil abierto
    const profileDropdown = document.getElementById('perfilDropdown');
    if (profileDropdown && window.bootstrap) {
        const bsDropdown = bootstrap.Dropdown.getInstance(profileDropdown);
        if (bsDropdown) bsDropdown.hide();
    }

    switchAuthTab(tab);

    const bsModal = bootstrap.Modal.getOrCreateInstance(modalEl);
    bsModal.show();
};

window.switchAuthTab = function (tab) {
    const paneLogin = document.getElementById('authPaneLogin');
    const paneRegister = document.getElementById('authPaneRegister');
    const paneForgot = document.getElementById('authPaneForgot');
    const btnTabLogin = document.getElementById('authBtnTabLogin');
    const btnTabRegister = document.getElementById('authBtnTabRegister');

    if (!paneLogin || !paneRegister) return;

    // Resetear alertas
    limpiarAlertasAuth();

    if (tab === 'register') {
        paneLogin.classList.add('d-none');
        paneRegister.classList.remove('d-none');
        if (paneForgot) paneForgot.classList.add('d-none');

        if (btnTabRegister) btnTabRegister.classList.add('active');
        if (btnTabLogin) btnTabLogin.classList.remove('active');

        const input = document.getElementById('authRegEmail');
        if (input) setTimeout(() => input.focus(), 150);
    } else if (tab === 'forgot') {
        paneLogin.classList.add('d-none');
        paneRegister.classList.add('d-none');
        if (paneForgot) paneForgot.classList.remove('d-none');

        if (btnTabRegister) btnTabRegister.classList.remove('active');
        if (btnTabLogin) btnTabLogin.classList.remove('active');

        const input = document.getElementById('authForgotEmail');
        if (input) setTimeout(() => input.focus(), 150);
    } else {
        // Por defecto 'login'
        paneLogin.classList.remove('d-none');
        paneRegister.classList.add('d-none');
        if (paneForgot) paneForgot.classList.add('d-none');

        if (btnTabLogin) btnTabLogin.classList.add('active');
        if (btnTabRegister) btnTabRegister.classList.remove('active');

        const input = document.getElementById('authLoginEmail');
        if (input) setTimeout(() => input.focus(), 150);
    }
};

window.togglePasswordVisibility = function (inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';

    const icon = btn.querySelector('i');
    if (icon) {
        if (isPassword) {
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
    }
};

window.handleAuthLogin = async function (e) {
    e.preventDefault();
    const alertBox = document.getElementById('authLoginAlert');
    const btn = document.getElementById('btnSubmitLogin');
    const email = document.getElementById('authLoginEmail')?.value.trim();
    const password = document.getElementById('authLoginPassword')?.value;
    const remember = document.getElementById('authLoginRemember')?.checked;

    if (!email || !password) {
        mostrarAlertaAuth(alertBox, 'Por favor, ingresa tu correo y contraseña.', 'danger');
        return;
    }

    btn.disabled = true;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Iniciando sesión...';
    limpiarAlertasAuth();

    try {
        const bodyParams = new URLSearchParams();
        bodyParams.append('username', email);
        bodyParams.append('password', password);
        if (remember) bodyParams.append('remember-me', 'true');

        const response = await fetch('/inicio-sesion', {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            body: bodyParams
        });

        const data = await response.json().catch(() => ({}));

        if (response.ok && data.ok) {
            mostrarAlertaAuth(alertBox, '¡Inicio de sesión exitoso! Redirigiendo...', 'success');
            setTimeout(() => {
                if (window.__authSuccessCallback && typeof window.__authSuccessCallback === 'function') {
                    const cb = window.__authSuccessCallback;
                    window.__authSuccessCallback = null;
                    const modalEl = document.getElementById('authModal');
                    if (modalEl && window.bootstrap) {
                        bootstrap.Modal.getInstance(modalEl)?.hide();
                    }
                    cb();
                } else if (data.redirect && data.redirect.includes('/backoffice')) {
                    window.location.href = data.redirect;
                } else {
                    window.location.reload();
                }
            }, 600);
        } else {
            const errorMsg = data.mensaje || 'Correo o contraseña incorrectos. Verifica tus credenciales.';
            mostrarAlertaAuth(alertBox, errorMsg, 'danger');
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    } catch (err) {
        mostrarAlertaAuth(alertBox, 'Error de conexión al iniciar sesión. Inténtalo nuevamente.', 'danger');
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
};

window.handleAuthRegister = async function (e) {
    e.preventDefault();
    const alertBox = document.getElementById('authRegisterAlert');
    const btn = document.getElementById('btnSubmitRegister');
    const email = document.getElementById('authRegEmail')?.value.trim();
    const password = document.getElementById('authRegPassword')?.value;
    const confirmPassword = document.getElementById('authRegPasswordConfirm')?.value;
    const terms = document.getElementById('authRegTerms')?.checked;

    if (!terms) {
        mostrarAlertaAuth(alertBox, 'Debes aceptar los términos y condiciones para continuar.', 'warning');
        return;
    }

    if (!email || !password || !confirmPassword) {
        mostrarAlertaAuth(alertBox, 'Todos los campos son obligatorios.', 'danger');
        return;
    }

    if (password.length < 6) {
        mostrarAlertaAuth(alertBox, 'La contraseña debe tener al menos 6 caracteres.', 'danger');
        return;
    }

    if (password !== confirmPassword) {
        mostrarAlertaAuth(alertBox, 'Las contraseñas no coinciden. Por favor, verifica.', 'danger');
        return;
    }

    btn.disabled = true;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Creando cuenta...';
    limpiarAlertasAuth();

    try {
        const response = await fetch('/api/auth/registro', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                correoInicioSesion: email,
                contrasenia: password,
                confirmarContrasenia: confirmPassword
            })
        });

        const data = await response.json().catch(() => ({}));

        if (response.ok && data.ok) {
            mostrarAlertaAuth(alertBox, '¡Cuenta creada con éxito! Iniciando sesión...', 'success');
            // Iniciar sesión automáticamente
            setTimeout(async () => {
                try {
                    const loginParams = new URLSearchParams();
                    loginParams.append('username', email);
                    loginParams.append('password', password);
                    const loginRes = await fetch('/inicio-sesion', {
                        method: 'POST',
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest',
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'Accept': 'application/json'
                        },
                        body: loginParams
                    });
                    if (loginRes.ok) {
                        window.location.reload();
                    } else {
                        switchAuthTab('login');
                        const loginEmail = document.getElementById('authLoginEmail');
                        if (loginEmail) loginEmail.value = email;
                        const loginAlert = document.getElementById('authLoginAlert');
                        mostrarAlertaAuth(loginAlert, 'Cuenta creada con éxito. Ya puedes iniciar sesión con tu contraseña.', 'success');
                    }
                } catch (e) {
                    switchAuthTab('login');
                    const loginEmail = document.getElementById('authLoginEmail');
                    if (loginEmail) loginEmail.value = email;
                }
            }, 800);
        } else {
            const errorMsg = data.mensaje || 'No se pudo crear la cuenta. Inténtalo con otro correo.';
            mostrarAlertaAuth(alertBox, errorMsg, 'danger');
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    } catch (err) {
        mostrarAlertaAuth(alertBox, 'Error de conexión con el servidor al registrar cuenta.', 'danger');
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
};

window.handleAuthForgot = async function (e) {
    e.preventDefault();
    const alertBox = document.getElementById('authForgotAlert');
    const btn = document.getElementById('btnSubmitForgot');
    const email = document.getElementById('authForgotEmail')?.value.trim();

    if (!email) {
        mostrarAlertaAuth(alertBox, 'Por favor, ingresa tu correo electrónico.', 'danger');
        return;
    }

    btn.disabled = true;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Enviando...';

    try {
        const response = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ correo: email })
        });

        if (response.ok) {
            mostrarAlertaAuth(alertBox, 'Si la cuenta existe, se ha enviado un enlace de recuperación.', 'success');
        } else {
            mostrarAlertaAuth(alertBox, 'No se encontró ninguna cuenta asociada a este correo.', 'danger');
        }
    } catch (err) {
        mostrarAlertaAuth(alertBox, 'Error de conexión. Inténtalo de nuevo.', 'danger');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
};

function mostrarAlertaAuth(alertEl, mensaje, tipo = 'danger') {
    if (!alertEl) return;
    alertEl.className = `alert alert-${tipo} py-2 px-3 small rounded-3 mb-3 d-flex align-items-center gap-2`;
    const icon = tipo === 'success' ? 'bi-check-circle-fill' : (tipo === 'warning' ? 'bi-exclamation-triangle-fill' : 'bi-x-circle-fill');
    alertEl.innerHTML = `<i class="bi ${icon} fs-6 flex-shrink-0"></i> <div>${escapeHtml(mensaje)}</div>`;
    alertEl.classList.remove('d-none');
}

function limpiarAlertasAuth() {
    ['authLoginAlert', 'authRegisterAlert', 'authForgotAlert'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.classList.add('d-none');
            el.innerHTML = '';
        }
    });
}

/* ==========================================================================
   6. Carrito Reactivo AJAX (/carrito) - Cero Delay & Optimistic UI
   ========================================================================== */
let cartSyncTimer = null;
const cartPendingUpdates = new Map();

function formatearMoneda(valor) {
    const num = Number(valor) || 0;
    return '$' + num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function recalcularTotalesCartOptimista() {
    const rows = document.querySelectorAll('.cart-item-row');
    let subtotalTotal = 0;
    let unidadesTotal = 0;

    rows.forEach(row => {
        const id = row.getAttribute('data-id');
        const precio = parseFloat(row.getAttribute('data-precio')) || 0;
        const input = document.getElementById('item-cant-' + id);
        const cant = input ? (parseInt(input.value) || 0) : 0;
        const itemSubtotal = precio * cant;

        subtotalTotal += itemSubtotal;
        unidadesTotal += cant;

        // Actualizar subtotal de este renglón (desktop y mobile)
        const elSubtotal = document.getElementById('item-subtotal-' + id);
        if (elSubtotal) elSubtotal.textContent = formatearMoneda(itemSubtotal);
        const elSubtotalMobile = document.getElementById('item-subtotal-mobile-' + id);
        if (elSubtotalMobile) elSubtotalMobile.textContent = formatearMoneda(itemSubtotal);
    });

    // Envío gratis permanente
    const totalPagar = subtotalTotal;

    // Actualizar elementos en DOM
    const elCartSubtotal = document.getElementById('cart-summary-subtotal');
    if (elCartSubtotal) elCartSubtotal.textContent = formatearMoneda(subtotalTotal);

    const elCartTotal = document.getElementById('cart-summary-total');
    if (elCartTotal) elCartTotal.textContent = formatearMoneda(totalPagar);

    const elCartShipping = document.getElementById('cart-summary-shipping');
    if (elCartShipping) {
        elCartShipping.className = 'fw-bold text-success';
        elCartShipping.innerHTML = '<i class="bi bi-check-lg"></i> Gratis';
    }

    const elSummaryCount = document.getElementById('cart-summary-count');
    if (elSummaryCount) elSummaryCount.textContent = unidadesTotal;

    const elHeaderBadgeCount = document.getElementById('cart-header-count');
    if (elHeaderBadgeCount) elHeaderBadgeCount.textContent = unidadesTotal;

    const elHeaderUnit = document.getElementById('cart-header-unit');
    if (elHeaderUnit) elHeaderUnit.textContent = unidadesTotal === 1 ? 'artículo' : 'artículos';

    // Badge en Navbar global
    const navbarBadge = document.getElementById('headerCartBadge');
    if (navbarBadge) {
        navbarBadge.textContent = unidadesTotal;
        navbarBadge.classList.toggle('d-none', unidadesTotal === 0);
    }
}

window.actualizarCantidadAjax = function (idProducto, delta) {
    const input = document.getElementById('item-cant-' + idProducto) || document.getElementById('item-cant-mobile-' + idProducto);
    if (!input) return;

    const actual = parseInt(input.value) || 1;
    const min = parseInt(input.min) || 1;
    const max = parseInt(input.max) || 9999;
    const nueva = actual + delta;

    if (nueva < min || nueva > max) return;

    const inpDesktop = document.getElementById('item-cant-' + idProducto);
    const inpMobile = document.getElementById('item-cant-mobile-' + idProducto);
    if (inpDesktop) inpDesktop.value = nueva;
    if (inpMobile) inpMobile.value = nueva;

    // Actualizar estado de botones +/-
    const btnDec = document.getElementById('btn-decrement-' + idProducto);
    const btnInc = document.getElementById('btn-increment-' + idProducto);
    if (btnDec) btnDec.disabled = (nueva <= min);
    if (btnInc) btnInc.disabled = (nueva >= max);

    // Optimistic UI: actualización instantánea (0ms)
    recalcularTotalesCartOptimista();

    // Sincronizar con el servidor en segundo plano
    syncCantidadConServidor(idProducto, nueva, actual);
};

// Alias de retrocompatibilidad
window.cambiarCantidadItem = function (idProducto, delta) {
    window.actualizarCantidadAjax(idProducto, delta);
};

window.actualizarCantidadInputAjax = function (idProducto, val) {
    const input = document.getElementById('item-cant-' + idProducto) || document.getElementById('item-cant-mobile-' + idProducto);
    if (!input) return;

    let nueva = parseInt(val) || 1;
    const min = parseInt(input.min) || 1;
    const max = parseInt(input.max) || 9999;

    if (nueva < min) nueva = min;
    if (nueva > max) nueva = max;

    const inpDesktop = document.getElementById('item-cant-' + idProducto);
    const inpMobile = document.getElementById('item-cant-mobile-' + idProducto);
    if (inpDesktop) inpDesktop.value = nueva;
    if (inpMobile) inpMobile.value = nueva;

    const btnDec = document.getElementById('btn-decrement-' + idProducto);
    const btnInc = document.getElementById('btn-increment-' + idProducto);
    if (btnDec) btnDec.disabled = (nueva <= min);
    if (btnInc) btnInc.disabled = (nueva >= max);

    recalcularTotalesCartOptimista();
    syncCantidadConServidor(idProducto, nueva, min);
};

function syncCantidadConServidor(idProducto, cantidad, valorPrevio) {
    cartPendingUpdates.set(idProducto, cantidad);

    clearTimeout(cartSyncTimer);
    cartSyncTimer = setTimeout(() => {
        cartPendingUpdates.forEach((cant, id) => {
            const formData = new URLSearchParams();
            formData.append('idProducto', id);
            formData.append('cantidad', cant);

            fetch('/api/carrito/actualizar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            })
            .then(res => res.json())
            .then(data => {
                if (data.ok) {
                    // Verificación de valores exactos con backend
                    if (data.subtotal !== undefined) {
                        const elCartSubtotal = document.getElementById('cart-summary-subtotal');
                        if (elCartSubtotal) elCartSubtotal.textContent = formatearMoneda(data.subtotal);
                    }
                    if (data.total !== undefined) {
                        const elCartTotal = document.getElementById('cart-summary-total');
                        if (elCartTotal) elCartTotal.textContent = formatearMoneda(data.total);
                    }
                } else {
                    mostrarToastEcommerce(data.mensaje || 'Error actualizando cantidad.', 'danger');
                    const inp = document.getElementById('item-cant-' + id);
                    const inpMob = document.getElementById('item-cant-mobile-' + id);
                    if (inp) inp.value = valorPrevio;
                    if (inpMob) inpMob.value = valorPrevio;
                    recalcularTotalesCartOptimista();
                }
            })
            .catch(err => {
                console.error('Error sincronizando carrito:', err);
            });
        });
        cartPendingUpdates.clear();
    }, 250);
}

window.eliminarItemAjax = function (idProducto) {
    const row = document.getElementById('cart-row-' + idProducto);
    if (!row) return;

    // Transición suave de salida
    row.style.transition = 'opacity 0.25s ease, transform 0.25s ease';
    row.style.opacity = '0';
    row.style.transform = 'scale(0.96)';

    setTimeout(() => {
        row.remove();
        recalcularTotalesCartOptimista();

        const remainingRows = document.querySelectorAll('.cart-item-row');
        if (remainingRows.length === 0) {
            const emptyView = document.getElementById('cart-empty-view');
            const filledView = document.getElementById('cart-filled-view');
            const headerBadge = document.getElementById('cart-header-badge');
            const headerActions = document.getElementById('cart-header-actions');
            if (emptyView) emptyView.classList.remove('d-none');
            if (filledView) filledView.classList.add('d-none');
            if (headerBadge) headerBadge.classList.add('d-none');
            if (headerActions) headerActions.classList.add('d-none');
        }

        // Llamar al endpoint
        const formData = new URLSearchParams();
        formData.append('idProducto', idProducto);

        fetch('/api/carrito/eliminar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        })
        .then(res => res.json())
        .then(data => {
            if (data.ok) {
                mostrarToastEcommerce('Artículo eliminado del carrito.', 'info');
                if (data.carritoCantidad !== undefined) {
                    const navbarBadge = document.getElementById('headerCartBadge');
                    if (navbarBadge) {
                        navbarBadge.textContent = data.carritoCantidad;
                        navbarBadge.classList.toggle('d-none', data.carritoCantidad === 0);
                    }
                }
            } else {
                mostrarToastEcommerce(data.mensaje || 'No se pudo eliminar el artículo.', 'danger');
            }
        })
        .catch(err => {
            console.error('Error al eliminar artículo:', err);
        });
    }, 250);
};

function mostrarToastEcommerce(mensaje, tipo = 'info') {
    let container = document.querySelector('.store-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'store-toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `store-toast store-toast-${tipo}`;

    let icono = 'bi-info-circle-fill text-primary';
    if (tipo === 'success') icono = 'bi-check-circle-fill text-success';
    if (tipo === 'danger') icono = 'bi-exclamation-triangle-fill text-danger';

    toast.innerHTML = `<i class="bi ${icono} fs-5"></i><span>${escapeHtml(mensaje)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px) scale(0.95)';
        setTimeout(() => toast.remove(), 250);
    }, 3200);
}
