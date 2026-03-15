package org.justme.justPlugin.managers;

/**
 * Contains the full HTML/CSS/JS for the self-hosted config web editor.
 * Served as a single-page application by WebEditorManager.
 */
public final class WebEditorPage {

    private WebEditorPage() {}

    public static String getHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>JustPlugin - Config Editor</title>
<style>
  :root {
    --bg-primary: #0d1117;
    --bg-secondary: #161b22;
    --bg-tertiary: #1c2333;
    --bg-card: #1a2233;
    --bg-input: #0d1117;
    --border: #30363d;
    --border-focus: #00aaff;
    --text-primary: #e6edf3;
    --text-secondary: #8b949e;
    --text-muted: #484f58;
    --accent-blue: #00aaff;
    --accent-green: #00ffaa;
    --accent-red: #ff6b6b;
    --accent-yellow: #ffc107;
    --accent-purple: #a855f7;
    --gradient-start: #00aaff;
    --gradient-end: #00ffaa;
    --toggle-on: #00ffaa;
    --toggle-off: #30363d;
    --shadow: 0 4px 24px rgba(0,0,0,0.4);
    --radius: 8px;
    --radius-lg: 12px;
    --font: 'Segoe UI', -apple-system, BlinkMacSystemFont, 'Helvetica Neue', sans-serif;
    --mono: 'JetBrains Mono', 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  html { scroll-behavior: smooth; }
  body {
    font-family: var(--font);
    background: var(--bg-primary);
    color: var(--text-primary);
    min-height: 100vh;
    line-height: 1.6;
  }

  /* ===== Scrollbar ===== */
  ::-webkit-scrollbar { width: 8px; }
  ::-webkit-scrollbar-track { background: var(--bg-primary); }
  ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 4px; }
  ::-webkit-scrollbar-thumb:hover { background: var(--text-muted); }

  /* ===== Header ===== */
  .header {
    background: var(--bg-secondary);
    border-bottom: 1px solid var(--border);
    padding: 16px 0;
    position: sticky;
    top: 0;
    z-index: 100;
    backdrop-filter: blur(12px);
  }
  .header-inner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .logo-icon {
    width: 36px; height: 36px;
    background: linear-gradient(135deg, var(--gradient-start), var(--gradient-end));
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 900;
    font-size: 18px;
    color: var(--bg-primary);
  }
  .logo-text {
    font-size: 20px;
    font-weight: 700;
  }
  .logo-text span {
    background: linear-gradient(135deg, var(--gradient-start), var(--gradient-end));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  .logo-badge {
    background: var(--bg-tertiary);
    color: var(--text-secondary);
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 12px;
    border: 1px solid var(--border);
    font-family: var(--mono);
  }
  .header-actions { display: flex; gap: 10px; align-items: center; }

  /* ===== Buttons ===== */
  .btn {
    padding: 8px 18px;
    border-radius: var(--radius);
    border: 1px solid var(--border);
    cursor: pointer;
    font-size: 13px;
    font-weight: 600;
    font-family: var(--font);
    transition: all 0.2s ease;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
  .btn-primary {
    background: linear-gradient(135deg, var(--gradient-start), var(--gradient-end));
    color: var(--bg-primary);
    border-color: transparent;
  }
  .btn-primary:hover { opacity: 0.9; transform: translateY(-1px); box-shadow: 0 4px 16px rgba(0,170,255,0.3); }
  .btn-secondary { background: var(--bg-tertiary); color: var(--text-primary); }
  .btn-secondary:hover { background: var(--border); }
  .btn-danger { background: transparent; color: var(--accent-red); border-color: var(--accent-red); }
  .btn-danger:hover { background: rgba(255,107,107,0.1); }

  /* ===== Layout ===== */
  .container { max-width: 1200px; margin: 0 auto; padding: 24px; }

  /* ===== Stats bar ===== */
  .stats {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
    flex-wrap: wrap;
  }
  .stat {
    background: var(--bg-secondary);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    padding: 12px 20px;
    flex: 1;
    min-width: 150px;
  }
  .stat-label { font-size: 11px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; }
  .stat-value { font-size: 22px; font-weight: 700; margin-top: 2px; }
  .stat-value.blue { color: var(--accent-blue); }
  .stat-value.green { color: var(--accent-green); }
  .stat-value.yellow { color: var(--accent-yellow); }
  .stat-value.purple { color: var(--accent-purple); }

  /* ===== Search ===== */
  .search-bar {
    position: relative;
    margin-bottom: 20px;
  }
  .search-bar input {
    width: 100%;
    padding: 12px 16px 12px 42px;
    background: var(--bg-secondary);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    color: var(--text-primary);
    font-size: 14px;
    font-family: var(--font);
    outline: none;
    transition: border-color 0.2s;
  }
  .search-bar input:focus { border-color: var(--accent-blue); }
  .search-bar input::placeholder { color: var(--text-muted); }
  .search-icon {
    position: absolute;
    left: 14px; top: 50%;
    transform: translateY(-50%);
    color: var(--text-muted);
    font-size: 16px;
  }

  /* ===== Sections ===== */
  .section {
    background: var(--bg-secondary);
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    margin-bottom: 16px;
    overflow: hidden;
    transition: box-shadow 0.2s;
  }
  .section:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.2); }
  .section-header {
    padding: 14px 20px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
    user-select: none;
    transition: background 0.15s;
  }
  .section-header:hover { background: var(--bg-tertiary); }
  .section-title {
    font-size: 14px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .section-title .icon { font-size: 16px; }
  .section-count {
    background: var(--bg-primary);
    color: var(--text-secondary);
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 10px;
    font-family: var(--mono);
  }
  .section-arrow {
    color: var(--text-muted);
    transition: transform 0.25s ease;
    font-size: 12px;
  }
  .section.open .section-arrow { transform: rotate(90deg); }
  .section-body {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.35s ease;
  }
  .section.open .section-body { max-height: 5000px; }
  .section-content { padding: 4px 20px 16px; }

  /* ===== Config entries ===== */
  .entry {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 0;
    border-bottom: 1px solid rgba(48,54,61,0.5);
    gap: 16px;
  }
  .entry:last-child { border-bottom: none; }
  .entry-info { flex: 1; min-width: 0; }
  .entry-key {
    font-family: var(--mono);
    font-size: 13px;
    color: var(--accent-blue);
    word-break: break-all;
  }
  .entry-comment {
    font-size: 11px;
    color: var(--text-muted);
    margin-top: 2px;
    line-height: 1.4;
  }
  .entry-placeholder {
    font-size: 10px;
    color: var(--accent-purple);
    margin-top: 2px;
    font-family: var(--mono);
  }
  .entry-control { flex-shrink: 0; }

  /* ===== Toggle Switch ===== */
  .toggle {
    position: relative;
    width: 44px; height: 24px;
    cursor: pointer;
  }
  .toggle input { display: none; }
  .toggle-slider {
    position: absolute;
    inset: 0;
    background: var(--toggle-off);
    border-radius: 12px;
    transition: background 0.25s;
  }
  .toggle-slider::before {
    content: '';
    position: absolute;
    width: 18px; height: 18px;
    left: 3px; top: 3px;
    background: white;
    border-radius: 50%;
    transition: transform 0.25s;
  }
  .toggle input:checked + .toggle-slider { background: var(--toggle-on); }
  .toggle input:checked + .toggle-slider::before { transform: translateX(20px); }

  /* ===== Text & Number inputs ===== */
  .input-field {
    padding: 6px 12px;
    background: var(--bg-input);
    border: 1px solid var(--border);
    border-radius: 6px;
    color: var(--text-primary);
    font-size: 13px;
    font-family: var(--mono);
    outline: none;
    min-width: 180px;
    max-width: 340px;
    transition: border-color 0.2s;
  }
  .input-field:focus { border-color: var(--accent-blue); }
  .input-field.number { width: 100px; min-width: 80px; text-align: right; }
  .input-field.wide { width: 100%; max-width: 500px; }

  /* ===== Modified indicator ===== */
  .entry.modified .entry-key::after {
    content: '●';
    color: var(--accent-yellow);
    margin-left: 6px;
    font-size: 10px;
  }

  /* ===== Modal ===== */
  .modal-overlay {
    display: none;
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.7);
    z-index: 1000;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(4px);
  }
  .modal-overlay.active { display: flex; }
  .modal {
    background: var(--bg-secondary);
    border: 1px solid var(--border);
    border-radius: var(--radius-lg);
    padding: 32px;
    max-width: 480px;
    width: 90%;
    text-align: center;
    box-shadow: var(--shadow);
    animation: modalIn 0.25s ease;
  }
  @keyframes modalIn {
    from { opacity: 0; transform: scale(0.95) translateY(10px); }
    to { opacity: 1; transform: scale(1) translateY(0); }
  }
  .modal h2 {
    font-size: 20px;
    margin-bottom: 8px;
    background: linear-gradient(135deg, var(--gradient-start), var(--gradient-end));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  .modal p { color: var(--text-secondary); font-size: 14px; margin-bottom: 20px; }
  .session-code {
    background: var(--bg-primary);
    border: 2px dashed var(--accent-green);
    border-radius: var(--radius);
    padding: 16px;
    font-family: var(--mono);
    font-size: 28px;
    font-weight: 700;
    color: var(--accent-green);
    letter-spacing: 4px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: background 0.2s;
    user-select: all;
  }
  .session-code:hover { background: var(--bg-tertiary); }
  .modal .hint {
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 20px;
  }
  .modal .command-preview {
    background: var(--bg-primary);
    border: 1px solid var(--border);
    border-radius: 6px;
    padding: 10px 16px;
    font-family: var(--mono);
    font-size: 13px;
    color: var(--accent-yellow);
    margin-bottom: 20px;
    user-select: all;
  }

  /* ===== Toast ===== */
  .toast {
    position: fixed;
    bottom: 24px;
    right: 24px;
    background: var(--bg-secondary);
    border: 1px solid var(--accent-green);
    color: var(--accent-green);
    padding: 12px 20px;
    border-radius: var(--radius);
    font-size: 13px;
    font-weight: 600;
    z-index: 2000;
    animation: toastIn 0.3s ease, toastOut 0.3s ease 2.7s;
    box-shadow: var(--shadow);
  }
  @keyframes toastIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
  @keyframes toastOut { to { opacity: 0; transform: translateY(20px); } }

  /* ===== Loading ===== */
  .loading {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 80px;
    flex-direction: column;
    gap: 16px;
  }
  .spinner {
    width: 36px; height: 36px;
    border: 3px solid var(--border);
    border-top-color: var(--accent-blue);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  @keyframes spin { to { transform: rotate(360deg); } }

  /* ===== Footer ===== */
  .footer {
    text-align: center;
    padding: 32px;
    color: var(--text-muted);
    font-size: 12px;
    border-top: 1px solid var(--border);
    margin-top: 40px;
  }

  /* ===== Responsive ===== */
  @media (max-width: 768px) {
    .stats { flex-direction: column; }
    .entry { flex-direction: column; align-items: flex-start; gap: 8px; }
    .input-field { min-width: 100%; }
    .header-inner { flex-direction: column; gap: 12px; }
  }
</style>
</head>
<body>

<div class="header">
  <div class="header-inner">
    <div class="logo">
      <div class="logo-icon">JP</div>
      <div class="logo-text"><span>JustPlugin</span> Config Editor</div>
      <div class="logo-badge" id="version"></div>
    </div>
    <div class="header-actions">
      <button class="btn btn-secondary" onclick="resetAll()">↺ Reset Changes</button>
      <button class="btn btn-primary" onclick="saveChanges()" id="saveBtn" disabled>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
        Save &amp; Generate Code
      </button>
    </div>
  </div>
</div>

<div class="container">
  <div class="stats">
    <div class="stat">
      <div class="stat-label">Total Settings</div>
      <div class="stat-value blue" id="statTotal">-</div>
    </div>
    <div class="stat">
      <div class="stat-label">Modified</div>
      <div class="stat-value yellow" id="statModified">0</div>
    </div>
    <div class="stat">
      <div class="stat-label">Sections</div>
      <div class="stat-value green" id="statSections">-</div>
    </div>
    <div class="stat">
      <div class="stat-label">Session Expires</div>
      <div class="stat-value purple" id="statExpiry">10 min</div>
    </div>
  </div>

  <div class="search-bar">
    <span class="search-icon">⌕</span>
    <input type="text" id="search" placeholder="Search settings... (e.g. teleport, economy, enabled)" oninput="filterEntries()">
  </div>

  <div id="configRoot">
    <div class="loading">
      <div class="spinner"></div>
      <span style="color:var(--text-secondary)">Loading configuration...</span>
    </div>
  </div>

  <div class="footer">
    JustPlugin Config Editor &mdash; Changes require <code>/applyedits &lt;code&gt;</code> in-game to take effect.
  </div>
</div>

<!-- Session Code Modal -->
<div class="modal-overlay" id="modal">
  <div class="modal">
    <h2>✓ Edits Saved</h2>
    <p>Your changes have been staged. Run the following command in-game to apply them:</p>
    <div class="session-code" id="sessionCode" onclick="copyCode()">--------</div>
    <div class="hint">Click the code to copy &bull; Expires in 10 minutes</div>
    <div class="command-preview">/applyedits <span id="cmdCode">--------</span></div>
    <div style="display:flex;gap:10px;justify-content:center">
      <button class="btn btn-secondary" onclick="closeModal()">Close</button>
      <button class="btn btn-primary" onclick="copyCode()">📋 Copy Code</button>
    </div>
  </div>
</div>

<script>
const SECTION_ICONS = {
  'general': '⚙️', 'economy': '💰', 'teleport': '🌀', 'clickable-commands': '🔗',
  'trade': '🤝', 'homes': '🏠', 'warns': '⚠️', 'default-reasons': '📝',
  'discord-webhook': '🔔', 'punishment-announcements': '📢', 'entity-clear': '🧹',
  'clear-chat': '💬', 'friendly-fire': '⚔️', 'command-settings': '📋',
  'web-editor': '🌐'
};

const PLACEHOLDER_HINTS = {
  'motd': ['{player} - Player name'],
  'entity-clear.warning-message': ['%seconds% - Seconds until clear'],
  'entity-clear.clear-message': ['%items% - Items cleared', '%mobs% - Mobs cleared', '%total% - Total cleared'],
  'clear-chat.message': ['MiniMessage format supported'],
  'discord-link': ['Discord invite URL'],
  'warns.appeal-source': ['Defaults to discord-link if empty'],
  'discord-webhook.url': ['Discord webhook URL (use /setlogswebhook in-game)']
};

let originalConfig = {};
let currentConfig = {};
let modifiedKeys = new Set();

async function loadConfig() {
  try {
    const res = await fetch('/api/config');
    if (!res.ok) throw new Error('Failed to load config');
    const data = await res.json();
    originalConfig = JSON.parse(JSON.stringify(data.config));
    currentConfig = JSON.parse(JSON.stringify(data.config));
    document.getElementById('version').textContent = data.version || 'unknown';
    renderConfig();
    updateStats();
  } catch (e) {
    document.getElementById('configRoot').innerHTML =
      '<div class="loading" style="color:var(--accent-red)">❌ Failed to load configuration.<br><span style="font-size:13px;color:var(--text-muted)">Make sure the server is running and the web editor is enabled.</span></div>';
  }
}

function renderConfig() {
  const root = document.getElementById('configRoot');
  root.innerHTML = '';
  const sections = groupIntoSections(currentConfig);
  let sectionCount = 0;

  // Top-level simple keys
  const topLevel = {};
  for (const [k, v] of Object.entries(currentConfig)) {
    if (typeof v !== 'object' || v === null) topLevel[k] = v;
  }
  if (Object.keys(topLevel).length > 0) {
    root.appendChild(createSection('General', 'general', topLevel, ''));
    sectionCount++;
  }

  // Nested sections
  for (const [k, v] of Object.entries(currentConfig)) {
    if (typeof v === 'object' && v !== null) {
      root.appendChild(createSection(formatTitle(k), k, v, k));
      sectionCount++;
    }
  }

  document.getElementById('statSections').textContent = sectionCount;
}

function groupIntoSections(config) {
  const sections = {};
  for (const [key, val] of Object.entries(config)) {
    if (typeof val === 'object' && val !== null) {
      sections[key] = val;
    }
  }
  return sections;
}

function createSection(title, sectionKey, data, pathPrefix) {
  const section = document.createElement('div');
  section.className = 'section';
  section.setAttribute('data-section', sectionKey);

  const entries = flattenForDisplay(data, pathPrefix);
  const icon = SECTION_ICONS[sectionKey] || '📄';

  section.innerHTML = `
    <div class="section-header" onclick="this.parentElement.classList.toggle('open')">
      <div class="section-title">
        <span class="icon">${icon}</span>
        ${title}
        <span class="section-count">${entries.length} settings</span>
      </div>
      <span class="section-arrow">▶</span>
    </div>
    <div class="section-body">
      <div class="section-content" id="sec-${sectionKey}"></div>
    </div>
  `;

  const content = section.querySelector('.section-content');
  for (const entry of entries) {
    content.appendChild(createEntry(entry));
  }
  return section;
}

function flattenForDisplay(obj, prefix) {
  const entries = [];
  for (const [key, val] of Object.entries(obj)) {
    const fullKey = prefix ? prefix + '.' + key : key;
    if (typeof val === 'object' && val !== null) {
      entries.push(...flattenForDisplay(val, fullKey));
    } else {
      entries.push({ key: fullKey, value: val, type: typeof val });
    }
  }
  return entries;
}

function createEntry(entry) {
  const div = document.createElement('div');
  div.className = 'entry';
  div.setAttribute('data-key', entry.key);
  if (modifiedKeys.has(entry.key)) div.classList.add('modified');

  const shortKey = entry.key.includes('.') ? entry.key.split('.').pop() : entry.key;
  const hints = PLACEHOLDER_HINTS[entry.key] || [];
  const placeholderHtml = hints.length > 0
    ? '<div class="entry-placeholder">' + hints.join(' &bull; ') + '</div>'
    : '';

  // Detect placeholders in value
  let detectedPlaceholders = '';
  if (typeof entry.value === 'string') {
    const found = [];
    const matches = entry.value.matchAll(/\\{(\\w+)\\}|%(\\w+)%/g);
    for (const m of matches) found.push(m[0]);
    if (found.length > 0 && hints.length === 0) {
      detectedPlaceholders = '<div class="entry-placeholder">Variables: ' + found.join(', ') + '</div>';
    }
  }

  let controlHtml;
  if (typeof entry.value === 'boolean') {
    controlHtml = `<label class="toggle"><input type="checkbox" ${entry.value ? 'checked' : ''} onchange="updateValue('${entry.key}', this.checked)"><span class="toggle-slider"></span></label>`;
  } else if (typeof entry.value === 'number') {
    controlHtml = `<input type="number" class="input-field number" value="${entry.value}" onchange="updateValue('${entry.key}', parseFloat(this.value) || 0)" step="any">`;
  } else {
    const escaped = String(entry.value).replace(/"/g, '&quot;').replace(/</g, '&lt;');
    const isLong = String(entry.value).length > 60;
    controlHtml = `<input type="text" class="input-field ${isLong ? 'wide' : ''}" value="${escaped}" onchange="updateValue('${entry.key}', this.value)">`;
  }

  div.innerHTML = `
    <div class="entry-info">
      <div class="entry-key">${entry.key}</div>
      ${placeholderHtml}${detectedPlaceholders}
    </div>
    <div class="entry-control">${controlHtml}</div>
  `;
  return div;
}

function updateValue(key, value) {
  setNestedValue(currentConfig, key, value);
  const orig = getNestedValue(originalConfig, key);
  if (orig === value || (String(orig) === String(value))) {
    modifiedKeys.delete(key);
  } else {
    modifiedKeys.add(key);
  }
  // Update entry visual
  const entry = document.querySelector(`.entry[data-key="${key}"]`);
  if (entry) {
    entry.classList.toggle('modified', modifiedKeys.has(key));
  }
  updateStats();
}

function setNestedValue(obj, path, value) {
  const keys = path.split('.');
  let cur = obj;
  for (let i = 0; i < keys.length - 1; i++) {
    if (!(keys[i] in cur)) cur[keys[i]] = {};
    cur = cur[keys[i]];
  }
  cur[keys[keys.length - 1]] = value;
}

function getNestedValue(obj, path) {
  const keys = path.split('.');
  let cur = obj;
  for (const k of keys) {
    if (cur === undefined || cur === null) return undefined;
    cur = cur[k];
  }
  return cur;
}

function updateStats() {
  const total = document.querySelectorAll('.entry').length;
  document.getElementById('statTotal').textContent = total;
  document.getElementById('statModified').textContent = modifiedKeys.size;
  document.getElementById('saveBtn').disabled = modifiedKeys.size === 0;
}

function filterEntries() {
  const query = document.getElementById('search').value.toLowerCase().trim();
  document.querySelectorAll('.section').forEach(sec => {
    let anyVisible = false;
    sec.querySelectorAll('.entry').forEach(entry => {
      const key = entry.getAttribute('data-key').toLowerCase();
      const matches = !query || key.includes(query);
      entry.style.display = matches ? '' : 'none';
      if (matches) anyVisible = true;
    });
    sec.style.display = anyVisible ? '' : 'none';
    if (query && anyVisible) sec.classList.add('open');
  });
}

function resetAll() {
  currentConfig = JSON.parse(JSON.stringify(originalConfig));
  modifiedKeys.clear();
  renderConfig();
  updateStats();
  showToast('All changes reset');
}

async function saveChanges() {
  if (modifiedKeys.size === 0) return;
  const diff = {};
  for (const key of modifiedKeys) {
    diff[key] = getNestedValue(currentConfig, key);
  }
  try {
    const res = await fetch('/api/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ changes: diff })
    });
    if (!res.ok) throw new Error('Save failed');
    const data = await res.json();
    document.getElementById('sessionCode').textContent = data.code;
    document.getElementById('cmdCode').textContent = data.code;
    document.getElementById('modal').classList.add('active');
  } catch (e) {
    showToast('Failed to save changes!');
  }
}

function closeModal() {
  document.getElementById('modal').classList.remove('active');
}

function copyCode() {
  const code = document.getElementById('sessionCode').textContent;
  navigator.clipboard.writeText(code).then(() => showToast('Code copied to clipboard!'));
}

function formatTitle(key) {
  return key.replace(/-/g, ' ').replace(/\\b\\w/g, c => c.toUpperCase());
}

function showToast(msg) {
  const t = document.createElement('div');
  t.className = 'toast';
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 3000);
}

// Close modal on escape
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });
// Close modal on backdrop click
document.getElementById('modal').addEventListener('click', e => { if (e.target === e.currentTarget) closeModal(); });

// Load on start
loadConfig();
</script>
</body>
</html>
""";
    }
}

