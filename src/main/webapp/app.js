const workspaceSelect = document.getElementById("workspaceSelect");
const newWorkspaceBtn = document.getElementById("newWorkspaceBtn");
const saveWorkspaceBtn = document.getElementById("saveWorkspaceBtn");
const requestTabs = document.getElementById("requestTabs");
const addTabBtn = document.getElementById("addTabBtn");

const methodSelect = document.getElementById("methodSelect");
const urlInput = document.getElementById("urlInput");
const sendBtn = document.getElementById("sendBtn");
const importCurlBtn = document.getElementById("importCurlBtn");
const bodyInput = document.getElementById("bodyInput");
const bearerTokenInput = document.getElementById("bearerTokenInput");
const responseViewer = document.getElementById("responseViewer");
const statusBadge = document.getElementById("statusBadge");
const timeBadge = document.getElementById("timeBadge");
const loadingOverlay = document.getElementById("loadingOverlay");
const codeOutput = document.getElementById("codeOutput");

const toastContainer = document.getElementById("toastContainer");
const modal = document.getElementById("appModal");
const modalTitle = document.getElementById("modalTitle");
const modalMessage = document.getElementById("modalMessage");
const modalInput = document.getElementById("modalInput");
const modalOk = document.getElementById("modalOk");
const modalCancel = document.getElementById("modalCancel");

let showPretty = true;
let modalResolver = null;
let modalIsMultiline = false;
let idCounter = 1;

const state = {
    workspaces: [],
    activeWorkspaceId: null,
    activeTabId: null
};

function uid(prefix) {
    idCounter += 1;
    return `${prefix}-${Date.now()}-${idCounter}`;
}

function showToast(message, type = "success") {
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.textContent = message;
    toastContainer.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}

function closeModal(result) {
    if (modalResolver) {
        const resolver = modalResolver;
        modalResolver = null;
        modal.classList.add("hidden");
        resolver(result);
    }
}

function showModal(options) {
    const {
        title,
        message,
        defaultValue = "",
        placeholder = "",
        confirmText = "OK",
        cancelText = "Cancel",
        multiline = false
    } = options;

    modalTitle.textContent = title;
    modalMessage.textContent = message || "";
    modalInput.value = defaultValue;
    modalInput.placeholder = placeholder;
    modalInput.rows = multiline ? 7 : 1;
    modalOk.textContent = confirmText;
    modalCancel.textContent = cancelText;
    modal.classList.remove("hidden");
    modalIsMultiline = multiline;
    setTimeout(() => modalInput.focus(), 0);

    return new Promise((resolve) => {
        modalResolver = resolve;
    });
}

modalOk.addEventListener("click", () => closeModal(modalInput.value.trim()));
modalCancel.addEventListener("click", () => closeModal(null));
modal.addEventListener("click", (e) => {
    if (e.target === modal) {
        closeModal(null);
    }
});
modalInput.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
        closeModal(null);
    }
    if (e.key === "Enter" && !modalIsMultiline && !e.shiftKey) {
        e.preventDefault();
        closeModal(modalInput.value.trim());
    }
});

function defaultTab() {
    return {
        id: uid("tab"),
        title: "Untitled Tab",
        method: "GET",
        url: "",
        body: "",
        bearerToken: "",
        headers: [],
        params: [],
        _status: "-",
        _time: "- ms",
        _response: ""
    };
}

function normalizeTab(tab) {
    return {
        id: tab.id || uid("tab"),
        title: tab.title || "Untitled Tab",
        method: (tab.method || "GET").toUpperCase(),
        url: tab.url || "",
        body: tab.body || "",
        bearerToken: tab.bearerToken || "",
        headers: Array.isArray(tab.headers) ? tab.headers : [],
        params: Array.isArray(tab.params) ? tab.params : [],
        _status: tab._status || "-",
        _time: tab._time || "- ms",
        _response: tab._response || ""
    };
}

function getActiveWorkspace() {
    return state.workspaces.find((w) => w.id === state.activeWorkspaceId) || null;
}

function getActiveTab() {
    const workspace = getActiveWorkspace();
    if (!workspace) {
        return null;
    }
    return workspace.tabs.find((t) => t.id === state.activeTabId) || null;
}

function renderWorkspaceSelect() {
    workspaceSelect.innerHTML = "";
    state.workspaces.forEach((workspace) => {
        const option = document.createElement("option");
        option.value = workspace.id;
        option.textContent = workspace.name;
        workspaceSelect.appendChild(option);
    });
    if (state.activeWorkspaceId) {
        workspaceSelect.value = state.activeWorkspaceId;
    }
}

function renderTabStrip() {
    const workspace = getActiveWorkspace();
    requestTabs.innerHTML = "";

    if (!workspace) {
        return;
    }

    workspace.tabs.forEach((tab) => {
        const tabButton = document.createElement("button");
        tabButton.className = `req-tab ${tab.id === state.activeTabId ? "active" : ""}`;

        const label = document.createElement("span");
        label.textContent = tab.title;
        tabButton.appendChild(label);

        const close = document.createElement("span");
        close.className = "close";
        close.textContent = "x";
        close.addEventListener("click", (e) => {
            e.stopPropagation();
            closeTab(tab.id);
        });
        tabButton.appendChild(close);

        tabButton.addEventListener("click", () => {
            persistCurrentTabFromEditor();
            state.activeTabId = tab.id;
            renderTabStrip();
            hydrateEditorFromTab();
        });

        requestTabs.appendChild(tabButton);
    });
}

function createKVEditor(containerId, sourceArray, addLabel) {
    const root = document.getElementById(containerId);
    root.innerHTML = "";

    sourceArray.forEach((pair, index) => {
        const row = document.createElement("div");
        row.className = "kv-row";

        const keyInput = document.createElement("input");
        keyInput.placeholder = "Key";
        keyInput.value = pair.key || "";
        keyInput.addEventListener("input", (e) => {
            sourceArray[index].key = e.target.value;
        });

        const valueInput = document.createElement("input");
        valueInput.placeholder = "Value";
        valueInput.value = pair.value || "";
        valueInput.addEventListener("input", (e) => {
            sourceArray[index].value = e.target.value;
        });

        const removeBtn = document.createElement("button");
        removeBtn.className = "small-btn";
        removeBtn.textContent = "x";
        removeBtn.addEventListener("click", () => {
            sourceArray.splice(index, 1);
            createKVEditor(containerId, sourceArray, addLabel);
        });

        row.appendChild(keyInput);
        row.appendChild(valueInput);
        row.appendChild(removeBtn);
        root.appendChild(row);
    });

    const addBtn = document.createElement("button");
    addBtn.className = "ghost-btn";
    addBtn.textContent = addLabel;
    addBtn.addEventListener("click", () => {
        sourceArray.push({ key: "", value: "" });
        createKVEditor(containerId, sourceArray, addLabel);
    });
    root.appendChild(addBtn);
}

function hydrateEditorFromTab() {
    const tab = getActiveTab();
    if (!tab) {
        return;
    }

    methodSelect.value = tab.method || "GET";
    urlInput.value = tab.url || "";
    bodyInput.value = tab.body || "";
    bearerTokenInput.value = tab.bearerToken || "";

    createKVEditor("headers", tab.headers, "Add Header");
    createKVEditor("params", tab.params, "Add Param");

    statusBadge.textContent = `Status: ${tab._status || "-"}`;
    timeBadge.textContent = `Time: ${tab._time || "- ms"}`;
    renderResponse(tab._response || "");
}

function persistCurrentTabFromEditor() {
    const tab = getActiveTab();
    if (!tab) {
        return;
    }
    tab.method = methodSelect.value;
    tab.url = urlInput.value;
    tab.body = bodyInput.value;
    tab.bearerToken = bearerTokenInput.value;

    tab.title = tab.url && tab.url.trim() ? tab.url.trim().slice(0, 28) : tab.title || "Untitled Tab";
    renderTabStrip();
}

function renderResponse(raw) {
    const text = raw || "";
    if (!showPretty) {
        responseViewer.textContent = text;
        return;
    }
    try {
        responseViewer.textContent = JSON.stringify(JSON.parse(text), null, 2);
    } catch {
        responseViewer.textContent = text;
    }
}

function buildHeadersObject(tab) {
    const headers = {};
    (tab.headers || []).forEach((h) => {
        if (h.key) {
            headers[h.key] = h.value || "";
        }
    });
    if (tab.bearerToken && tab.bearerToken.trim()) {
        headers.Authorization = `Bearer ${tab.bearerToken.trim()}`;
    }
    return headers;
}

function withQueryParams(baseUrl, tab) {
    const active = (tab.params || []).filter((p) => p.key);
    if (!active.length) {
        return baseUrl;
    }
    const target = new URL(baseUrl);
    active.forEach((p) => target.searchParams.set(p.key, p.value || ""));
    return target.toString();
}

async function parseApiResponse(res) {
    const contentType = res.headers.get("content-type") || "";
    const text = await res.text();
    if (contentType.includes("application/json")) {
        try {
            return JSON.parse(text || "{}");
        } catch {
            return { status: res.status, responseTime: 0, body: text, error: "Invalid JSON response" };
        }
    }
    return { status: res.status, responseTime: 0, body: text, error: res.ok ? "" : "Non-JSON response" };
}

async function sendRequest() {
    persistCurrentTabFromEditor();
    const tab = getActiveTab();
    if (!tab) {
        return;
    }

    if (!tab.url || !tab.url.trim()) {
        showToast("URL is required", "error");
        return;
    }

    let finalUrl;
    try {
        finalUrl = withQueryParams(tab.url.trim(), tab);
    } catch {
        showToast("Invalid URL", "error");
        return;
    }

    const payload = {
        method: tab.method,
        url: finalUrl,
        headers: buildHeadersObject(tab),
        body: tab.body
    };

    loadingOverlay.classList.remove("hidden");
    try {
        const res = await fetch("/api/execute", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await parseApiResponse(res);

        tab._status = data.status;
        tab._time = `${data.responseTime || 0} ms`;
        tab._response = data.body || data.error || "";

        statusBadge.textContent = `Status: ${tab._status}`;
        statusBadge.className = data.status >= 200 && data.status < 400 ? "status-pill good" : "status-pill bad";
        timeBadge.textContent = `Time: ${tab._time}`;
        renderResponse(tab._response);

        if (!res.ok) {
            showToast(`Request failed (${data.status})`, "error");
        }
    } catch {
        tab._status = 0;
        tab._time = "0 ms";
        tab._response = "Network error while sending request.";
        statusBadge.textContent = "Status: 0";
        timeBadge.textContent = "Time: 0 ms";
        renderResponse(tab._response);
        showToast("Request failed", "error");
    } finally {
        loadingOverlay.classList.add("hidden");
    }
}

async function importCurl() {
    const curl = await showModal({
        title: "Import cURL",
        message: "Paste your cURL command",
        placeholder: "curl -X GET https://api.example.com",
        confirmText: "Import",
        multiline: true
    });
    if (!curl) {
        return;
    }

    const res = await fetch("/api/curl/parse", {
        method: "POST",
        headers: { "Content-Type": "text/plain" },
        body: curl
    });
    const parsed = await parseApiResponse(res);
    if (parsed.error) {
        showToast(parsed.error, "error");
        return;
    }

    const tab = getActiveTab();
    if (!tab) {
        return;
    }

    tab.method = parsed.method || "GET";
    tab.url = parsed.url || "";
    tab.body = parsed.body || "";
    tab.headers = Object.entries(parsed.headers || {}).map(([key, value]) => ({ key, value }));
    hydrateEditorFromTab();
    renderTabStrip();
    showToast("cURL imported", "success");
}

function addTab() {
    const workspace = getActiveWorkspace();
    if (!workspace) {
        return;
    }
    persistCurrentTabFromEditor();
    const tab = defaultTab();
    tab.title = `Untitled Tab ${workspace.tabs.length + 1}`;
    workspace.tabs.push(tab);
    state.activeTabId = tab.id;
    renderTabStrip();
    hydrateEditorFromTab();
}

function closeTab(tabId) {
    const workspace = getActiveWorkspace();
    if (!workspace) {
        return;
    }

    if (workspace.tabs.length === 1) {
        showToast("At least one tab is required", "error");
        return;
    }

    const idx = workspace.tabs.findIndex((t) => t.id === tabId);
    if (idx < 0) {
        return;
    }

    workspace.tabs.splice(idx, 1);

    if (state.activeTabId === tabId) {
        const next = workspace.tabs[Math.max(0, idx - 1)] || workspace.tabs[0];
        state.activeTabId = next.id;
    }

    renderTabStrip();
    hydrateEditorFromTab();
}

function workspaceToPayload(workspace) {
    return {
        name: workspace.name,
        tabs: workspace.tabs.map((tab) => ({
            id: tab.id,
            title: tab.title,
            method: tab.method,
            url: tab.url,
            body: tab.body,
            bearerToken: tab.bearerToken,
            headers: tab.headers,
            params: tab.params
        }))
    };
}

function tabToPayload(tab) {
    return {
        id: tab.id,
        title: tab.title,
        method: tab.method,
        url: tab.url,
        body: tab.body,
        bearerToken: tab.bearerToken,
        headers: tab.headers,
        params: tab.params
    };
}

async function saveWorkspace() {
    persistCurrentTabFromEditor();
    const workspace = getActiveWorkspace();
    if (!workspace) {
        return;
    }

    const res = await fetch(`/api/workspaces/${workspace.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(workspaceToPayload(workspace))
    });

    if (!res.ok) {
        showToast("Failed to save workspace", "error");
        return;
    }

    showToast("Workspace saved", "success");
}

async function createWorkspace() {
    const name = await showModal({
        title: "New Workspace",
        message: "Enter workspace name (optional)",
        placeholder: "Untitled",
        confirmText: "Create"
    });

    const initialTab = tabToPayload(defaultTab());
    const res = await fetch("/api/workspaces", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: name || "", tabs: [initialTab] })
    });

    if (!res.ok) {
        const err = await parseApiResponse(res);
        showToast(`Failed to create workspace (${res.status}) ${err.error || ""}`.trim(), "error");
        return;
    }

    const created = await parseApiResponse(res);
    const normalized = {
        ...created,
        tabs: (created.tabs || []).map(normalizeTab)
    };

    state.workspaces.unshift(normalized);
    state.activeWorkspaceId = normalized.id;
    state.activeTabId = normalized.tabs[0].id;
    renderWorkspaceSelect();
    renderTabStrip();
    hydrateEditorFromTab();
    showToast("Workspace created", "success");
}

async function loadWorkspaces() {
    const res = await fetch("/api/workspaces");
    const data = await parseApiResponse(res);
    const list = Array.isArray(data) ? data : [];

    state.workspaces = list.map((w) => ({
        ...w,
        tabs: (w.tabs || []).map(normalizeTab)
    }));

    if (state.workspaces.length === 0) {
        const initialTab = tabToPayload(defaultTab());
        const createRes = await fetch("/api/workspaces", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: "", tabs: [initialTab] })
        });
        if (!createRes.ok) {
            const err = await parseApiResponse(createRes);
            showToast(`Workspace bootstrap failed (${createRes.status}) ${err.error || ""}`.trim(), "error");
            return;
        }
        const created = await parseApiResponse(createRes);
        state.workspaces = [{ ...created, tabs: (created.tabs || []).map(normalizeTab) }];
    }

    state.activeWorkspaceId = state.workspaces[0].id;
    state.activeTabId = state.workspaces[0].tabs[0].id;

    renderWorkspaceSelect();
    renderTabStrip();
    hydrateEditorFromTab();
}

function activateTabButtons() {
    document.querySelectorAll(".tab-actions .tab").forEach((tab) => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".tab-actions .tab").forEach((t) => t.classList.remove("active"));
            document.querySelectorAll(".tab-panel").forEach((panel) => panel.classList.remove("active"));
            tab.classList.add("active");
            document.getElementById(tab.dataset.tab).classList.add("active");
        });
    });
}

function generateCurl() {
    persistCurrentTabFromEditor();
    const tab = getActiveTab();
    if (!tab) {
        return;
    }

    let line = `curl -X ${tab.method} "${tab.url}"`;
    Object.entries(buildHeadersObject(tab)).forEach(([k, v]) => {
        line += ` \\\n  -H "${k}: ${v}"`;
    });
    if (tab.body) {
        line += ` \\\n  -d '${tab.body.replaceAll("'", "\\'")}'`;
    }
    codeOutput.textContent = line;
}

function generateFetch() {
    persistCurrentTabFromEditor();
    const tab = getActiveTab();
    if (!tab) {
        return;
    }

    codeOutput.textContent = `fetch("${tab.url}", {\n  method: "${tab.method}",\n  headers: ${JSON.stringify(buildHeadersObject(tab), null, 2)},\n  body: ${tab.body ? JSON.stringify(tab.body) : "undefined"}\n}).then(r => r.text()).then(console.log);`;
}

workspaceSelect.addEventListener("change", () => {
    persistCurrentTabFromEditor();
    state.activeWorkspaceId = workspaceSelect.value;
    const workspace = getActiveWorkspace();
    state.activeTabId = workspace && workspace.tabs[0] ? workspace.tabs[0].id : null;
    renderTabStrip();
    hydrateEditorFromTab();
});

newWorkspaceBtn.addEventListener("click", createWorkspace);
saveWorkspaceBtn.addEventListener("click", saveWorkspace);
addTabBtn.addEventListener("click", addTab);
sendBtn.addEventListener("click", sendRequest);
importCurlBtn.addEventListener("click", importCurl);

methodSelect.addEventListener("change", persistCurrentTabFromEditor);
urlInput.addEventListener("input", persistCurrentTabFromEditor);
bodyInput.addEventListener("input", persistCurrentTabFromEditor);
bearerTokenInput.addEventListener("input", persistCurrentTabFromEditor);

document.getElementById("prettyBtn").addEventListener("click", () => {
    showPretty = true;
    document.getElementById("prettyBtn").classList.add("active");
    document.getElementById("rawBtn").classList.remove("active");
    renderResponse(getActiveTab() ? getActiveTab()._response : "");
});

document.getElementById("rawBtn").addEventListener("click", () => {
    showPretty = false;
    document.getElementById("rawBtn").classList.add("active");
    document.getElementById("prettyBtn").classList.remove("active");
    renderResponse(getActiveTab() ? getActiveTab()._response : "");
});

document.getElementById("copyResponseBtn").addEventListener("click", async () => {
    await navigator.clipboard.writeText(responseViewer.textContent || "");
    showToast("Response copied", "success");
});

document.getElementById("genCurlBtn").addEventListener("click", generateCurl);
document.getElementById("genFetchBtn").addEventListener("click", generateFetch);

document.addEventListener("keydown", (e) => {
    if (e.ctrlKey && e.key === "Enter") {
        sendRequest();
    }
});

activateTabButtons();
loadWorkspaces();
