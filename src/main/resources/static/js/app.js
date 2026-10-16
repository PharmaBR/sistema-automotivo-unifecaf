/*
 * Front-end simples (JavaScript puro, sem framework) que consome a API REST.
 * Toda regra de negócio está no back-end; aqui só exibimos dados e mostramos erros.
 */
const API = '/api';
const state = { page: 0, size: 10, brands: [], models: [] };

const $ = (sel) => document.querySelector(sel);
const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const km = new Intl.NumberFormat('pt-BR');
const CATEGORY = { HATCH: 'Hatch', SEDAN: 'Sedã', SUV: 'SUV', PICKUP: 'Picape', MINIVAN: 'Minivan', COUPE: 'Cupê' };
const STATUS = { AVAILABLE: 'Disponível', RESERVED: 'Reservado', SOLD: 'Vendido', DISCONTINUED: 'Descontinuado' };

// ------------------------------------------------------------------ HTTP

async function api(path, options = {}) {
    const res = await fetch(API + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
    });
    if (res.status === 204) return null;
    const body = await res.json().catch(() => null);
    if (!res.ok) {
        // A API responde erros no formato Problem Details: { title, detail, errors }
        let msg = body?.detail || `Erro ${res.status}`;
        if (body?.errors) {
            msg += ': ' + Object.entries(body.errors).map(([f, m]) => `${f} ${m}`).join('; ');
        }
        throw new Error(msg);
    }
    return body;
}

function toast(message, kind = 'success') {
    const el = $('#toast');
    el.textContent = message;
    el.className = `show ${kind}`;
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => (el.className = ''), 3500);
}

const escapeHtml = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
}[c]));

// ------------------------------------------------------------------ Catálogo

async function loadCatalog() {
    [state.brands, state.models] = await Promise.all([api('/brands'), api('/models')]);
    const brandOptions = state.brands.map((b) => `<option value="${b.id}">${escapeHtml(b.name)}</option>`).join('');
    $('#f-brand').innerHTML = '<option value="">Todas</option>' + brandOptions;
    $('#c-brand').innerHTML = brandOptions;
    $('#model-brand').innerHTML = brandOptions;
    fillModelSelect($('#f-model'), '', true);
    fillModelSelect($('#c-model'), $('#c-brand').value, false);
    renderCatalogLists();
}

function fillModelSelect(select, brandId, withAll) {
    const items = state.models.filter((m) => !brandId || String(m.brandId) === String(brandId));
    select.innerHTML = (withAll ? '<option value="">Todos</option>' : '') +
        items.map((m) => `<option value="${m.id}">${escapeHtml(m.name)}</option>`).join('');
}

function renderCatalogLists() {
    $('#brand-list').innerHTML = state.brands.map((b) => `
        <li><span><b>${escapeHtml(b.name)}</b> <span class="muted">${escapeHtml(b.country || '')}</span></span>
            <button class="btn ghost icon" data-del-brand="${b.id}" title="Remover">✕</button></li>`).join('');
    $('#model-list').innerHTML = state.models.map((m) => `
        <li><span><b>${escapeHtml(m.name)}</b> <span class="muted">${escapeHtml(m.brandName)} · ${CATEGORY[m.category]}</span></span>
            <button class="btn ghost icon" data-del-model="${m.id}" title="Remover">✕</button></li>`).join('');
}

// ------------------------------------------------------------------ Veículos

function currentQuery() {
    const form = new FormData($('#filters'));
    const params = new URLSearchParams();
    for (const [key, value] of form.entries()) {
        if (value === '' || key === 'sort') continue;
        params.set(key, value);
    }
    const [sortBy, direction] = (form.get('sort') || 'id:asc').split(':');
    params.set('sortBy', sortBy);
    params.set('direction', direction);
    params.set('page', state.page);
    params.set('size', state.size);
    return params.toString();
}

async function loadVehicles() {
    try {
        const data = await api('/vehicles?' + currentQuery());
        renderVehicles(data);
    } catch (e) {
        toast(e.message, 'error');
    }
}

function renderVehicles(data) {
    $('#total').textContent = `(${data.totalElements} encontrados)`;
    $('#page-info').textContent = `Página ${data.totalPages ? data.page + 1 : 0} de ${data.totalPages}`;
    $('#prev').disabled = data.page <= 0;
    $('#next').disabled = data.page + 1 >= data.totalPages;

    if (!data.content.length) {
        $('#vehicle-rows').innerHTML = '<tr><td colspan="9" class="empty">Nenhum veículo encontrado com esses filtros.</td></tr>';
        return;
    }
    $('#vehicle-rows').innerHTML = data.content.map((v) => `
        <tr>
            <td class="muted">${v.id}</td>
            <td><b>${escapeHtml(v.brand)} ${escapeHtml(v.model)}</b></td>
            <td>${CATEGORY[v.category] || ''}</td>
            <td>${v.manufactureYear}</td>
            <td>${escapeHtml(v.color)}</td>
            <td class="num">${brl.format(v.price)}</td>
            <td class="num">${km.format(v.mileage)}</td>
            <td><span class="badge s-${v.status}">${v.statusLabel}</span></td>
            <td><div class="row-actions">
                <button class="btn ghost icon" data-edit='${escapeHtml(JSON.stringify(v))}'>Editar</button>
                <button class="btn ghost icon" data-delete='${escapeHtml(JSON.stringify(v))}'>Remover</button>
            </div></td>
        </tr>`).join('');
}

async function loadSummary() {
    const s = await api('/vehicles/summary');
    const colors = { AVAILABLE: 'var(--ok)', RESERVED: 'var(--warn)', SOLD: 'var(--sold)', DISCONTINUED: 'var(--off)' };
    $('#summary').innerHTML = Object.keys(STATUS).map((k) => `
        <div class="stat" style="--c:${colors[k]}"><b>${s[k] ?? 0}</b><span>${STATUS[k]}</span></div>`).join('');
}

async function refreshAll() {
    await Promise.all([loadVehicles(), loadSummary()]);
}

// ------------------------------------------------------------------ Eventos

function formToJson(form, numericFields = []) {
    const out = {};
    for (const [k, v] of new FormData(form).entries()) {
        if (v === '' || k === 'vehicleId') continue;
        out[k] = numericFields.includes(k) ? Number(v) : v;
    }
    return out;
}

function bindEvents() {
    document.querySelectorAll('.tab[data-tab]').forEach((tab) => tab.addEventListener('click', () => {
        document.querySelectorAll('.tab[data-tab]').forEach((t) => t.classList.toggle('active', t === tab));
        $('#tab-vehicles').hidden = tab.dataset.tab !== 'vehicles';
        $('#tab-catalog').hidden = tab.dataset.tab !== 'catalog';
    }));

    $('#filters').addEventListener('submit', (e) => { e.preventDefault(); state.page = 0; loadVehicles(); });
    $('#filters').addEventListener('reset', () => setTimeout(() => {
        fillModelSelect($('#f-model'), '', true); state.page = 0; loadVehicles();
    }));
    $('#f-brand').addEventListener('change', (e) => fillModelSelect($('#f-model'), e.target.value, true));
    $('#prev').addEventListener('click', () => { state.page--; loadVehicles(); });
    $('#next').addEventListener('click', () => { state.page++; loadVehicles(); });

    document.querySelectorAll('[data-close]').forEach((b) => b.addEventListener('click', () => b.closest('dialog').close()));

    // Cadastro
    $('#btn-new-vehicle').addEventListener('click', () => {
        $('#create-form').reset();
        fillModelSelect($('#c-model'), $('#c-brand').value, false);
        $('#dlg-create').showModal();
    });
    $('#c-brand').addEventListener('change', (e) => fillModelSelect($('#c-model'), e.target.value, false));
    $('#create-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const body = formToJson(e.target, ['modelId', 'manufactureYear', 'price', 'mileage']);
            const v = await api('/vehicles', { method: 'POST', body: JSON.stringify(body) });
            $('#dlg-create').close();
            toast(`${v.brand} ${v.model} cadastrado (#${v.id})`);
            refreshAll();
        } catch (err) { toast(err.message, 'error'); }
    });

    // Edição e remoção (delegação de eventos na tabela)
    $('#vehicle-rows').addEventListener('click', (e) => {
        const editBtn = e.target.closest('[data-edit]');
        const delBtn = e.target.closest('[data-delete]');
        if (editBtn) {
            const v = JSON.parse(editBtn.dataset.edit);
            const f = $('#edit-form');
            f.elements.vehicleId.value = v.id;
            f.elements.price.value = v.price;
            f.elements.mileage.value = v.mileage;
            f.elements.color.value = v.color;
            f.elements.status.value = v.status;
            $('#edit-title').textContent = `#${v.id} ${v.brand} ${v.model}`;
            $('#dlg-edit').showModal();
        }
        if (delBtn) {
            const v = JSON.parse(delBtn.dataset.delete);
            $('#delete-form').elements.vehicleId.value = v.id;
            $('#delete-text').textContent = `#${v.id} ${v.brand} ${v.model} ${v.manufactureYear} — status: ${v.statusLabel}. `
                + 'Só é possível remover veículos vendidos ou descontinuados.';
            $('#dlg-delete').showModal();
        }
    });
    $('#edit-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = e.target.elements.vehicleId.value;
        try {
            const body = formToJson(e.target, ['price', 'mileage']);
            await api(`/vehicles/${id}`, { method: 'PATCH', body: JSON.stringify(body) });
            $('#dlg-edit').close();
            toast(`Veículo #${id} atualizado`);
            refreshAll();
        } catch (err) { toast(err.message, 'error'); }
    });
    $('#delete-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = e.target.elements.vehicleId.value;
        try {
            await api(`/vehicles/${id}`, { method: 'DELETE' });
            toast(`Veículo #${id} removido do estoque`);
            refreshAll();
        } catch (err) { toast(err.message, 'error'); }
        $('#dlg-delete').close();
    });

    // Marcas e modelos
    $('#brand-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/brands', { method: 'POST', body: JSON.stringify(formToJson(e.target)) });
            e.target.reset(); toast('Marca cadastrada'); await loadCatalog();
        } catch (err) { toast(err.message, 'error'); }
    });
    $('#model-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            await api('/models', { method: 'POST', body: JSON.stringify(formToJson(e.target, ['brandId'])) });
            e.target.elements.name.value = ''; toast('Modelo cadastrado'); await loadCatalog();
        } catch (err) { toast(err.message, 'error'); }
    });
    $('#brand-list').addEventListener('click', async (e) => {
        const id = e.target.dataset.delBrand;
        if (!id) return;
        try { await api(`/brands/${id}`, { method: 'DELETE' }); toast('Marca removida'); await loadCatalog(); }
        catch (err) { toast(err.message, 'error'); }
    });
    $('#model-list').addEventListener('click', async (e) => {
        const id = e.target.dataset.delModel;
        if (!id) return;
        try { await api(`/models/${id}`, { method: 'DELETE' }); toast('Modelo removido'); await loadCatalog(); }
        catch (err) { toast(err.message, 'error'); }
    });
}

// ------------------------------------------------------------------ Início

bindEvents();
loadCatalog().then(refreshAll).catch((e) => toast(e.message, 'error'));
