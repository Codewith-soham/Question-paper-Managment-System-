// Minimal front-end logic for add/search/view-all with localStorage fallback
(function() {
    const STORAGE_KEY = 'qpms_items_v1';
    const PDF_REGEX = /\.[Pp][Dd][Ff]$/;

    function readStore() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            return raw ? JSON.parse(raw) : [];
        } catch (e) { return []; }
    }

    function writeStore(items) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    }

    // Export/Import removed per request

    function toast(message) {
        const el = document.getElementById('toast');
        if (!el) return;
        el.textContent = message;
        el.classList.add('show');
        setTimeout(() => el.classList.remove('show'), 1800);
    }

    function setYear() {
        const y = document.getElementById('yearNow');
        if (y) y.textContent = new Date().getFullYear();
    }

    // Theme toggle
    function initTheme() {
        const themeToggle = document.getElementById('themeToggle');
        const saved = localStorage.getItem('qpms_theme');
        if (saved) document.documentElement.setAttribute('data-theme', saved);
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                const current = document.documentElement.getAttribute('data-theme');
                const next = current === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-theme', next);
                localStorage.setItem('qpms_theme', next);
            });
        }
    }

    // Back to top
    function initBackToTop() {
        const btn = document.getElementById('backToTop');
        if (!btn) return;
        btn.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    }

    // Populate all papers on dashboard
    function renderAllPapers() {
        const table = document.getElementById('allPapersTable');
        if (!table) return;
        const tbody = table.querySelector('tbody');
        const info = document.getElementById('allPapersInfo');
        tbody.innerHTML = '';
        const items = readStore();
        if (info) info.textContent = items.length ? '' : 'No papers yet. Add one to get started.';
        items.forEach(item => {
            const tr = document.createElement('tr');
            const fileLink = `../PDF/${encodeURIComponent(item.fileName)}`;
            tr.innerHTML = `
                <td>${item.id}</td>
                <td>${item.subject}</td>
                <td>${item.year}</td>
                <td>${item.semester}</td>
                <td>${item.status}</td>
                <td><a href="${fileLink}" target="_blank" rel="noopener">${item.fileName}</a></td>
                <td><button class="send-email-btn" data-filename="${item.fileName}">Send to Email</button></td>
            `;
            tbody.appendChild(tr);
        });
        // Add listeners
        Array.from(document.querySelectorAll('.send-email-btn')).forEach(btn => {
            btn.addEventListener('click', function() {
                showEmailModal(this.getAttribute('data-filename'));
            });
        });
    }

    function showEmailModal(fileName) {
        const modal = document.getElementById('emailModal');
        const emailInput = document.getElementById('recipientEmail');
        const fileInput = document.getElementById('sendFileName');
        const info = document.getElementById('emailModalInfo');
        if (!modal || !emailInput || !fileInput) return;
        modal.style.display = 'block';
        emailInput.value = '';
        fileInput.value = fileName;
        info.textContent = '';
        emailInput.focus();
    }
    document.getElementById('closeModal').onclick = function() {
        document.getElementById('emailModal').style.display = 'none';
    };
    window.onclick = function(event) {
        const modal = document.getElementById('emailModal');
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    };
    document.getElementById('emailSendForm').onsubmit = async function (e) {
        e.preventDefault();
        const recipient = document.getElementById('recipientEmail').value.trim();
        const fileName = document.getElementById('sendFileName').value;
        const info = document.getElementById('emailModalInfo');
        if (!recipient || !fileName) return;
        info.textContent = 'Sending...';
        try {
            const response = await fetch('http://localhost:8000/send-email', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ recipient, filename: fileName })
            });
            const text = await response.text();
            info.textContent = text;
            toast(text);
        } catch (err) {
            info.textContent = 'Failed to send email.';
            toast('Failed to send email.');
        }
        setTimeout(() => {
            document.getElementById('emailModal').style.display = 'none';
        }, 1600);
    };

    function initRefreshAll() {
        const btn = document.getElementById('refreshAllBtn');
        if (btn) btn.addEventListener('click', renderAllPapers);
    }

    // Add form
    function initAddForm() {
        const form = document.getElementById('addPaperForm');
        if (!form) return;
        const info = document.getElementById('addInfo');
        const fileInput = form.querySelector('input[name="fileName"]');

        function isPdf(name) { return PDF_REGEX.test((name || '').trim()); }
        if (fileInput) {
            fileInput.addEventListener('input', () => {
                if (isPdf(fileInput.value)) {
                    fileInput.setCustomValidity('');
                } else {
                    fileInput.setCustomValidity('Enter a file name ending with .pdf');
                }
            });
        }
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const data = new FormData(form);
            const subject = (data.get('subject') || '').toString().trim();
            const year = Number(data.get('year'));
            const semester = Number(data.get('semester'));
            const fileName = (data.get('fileName') || '').toString().trim();
            const status = (data.get('status') || '').toString();
            if (!subject || !year || !semester || !fileName || !status) return;
            if (!isPdf(fileName)) {
                if (fileInput) {
                    fileInput.setCustomValidity('Enter a file name ending with .pdf');
                    fileInput.reportValidity();
                }
                return;
            }
            const items = readStore();
            const id = items.length ? Math.max(...items.map(i => i.id)) + 1 : 1;
            const item = { id, subject, year, semester, fileName, status };
            items.push(item);
            writeStore(items);
            if (info) info.textContent = `Added: ${subject} ${year} Sem ${semester}`;
            toast('Paper added');
            form.reset();
        });
    }

    // Search form
    function initSearchForm() {
        const form = document.getElementById('searchPaperForm');
        const table = document.getElementById('resultTable');
        if (!form || !table) return;
        const info = document.getElementById('searchInfo');
        const tbody = table.querySelector('tbody');
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const data = new FormData(form);
            const subject = (data.get('subject') || '').toString().trim().toLowerCase();
            const year = Number(data.get('year'));
            const semester = Number(data.get('semester'));
            const items = readStore();
            const results = items.filter(i =>
                i.subject.toLowerCase() === subject && i.year === year && i.semester === semester
            );
            tbody.innerHTML = '';
            if (info) info.textContent = results.length ? '' : 'No results found.';
            results.forEach(item => {
                const tr = document.createElement('tr');
                const fileLink = `../PDF/${encodeURIComponent(item.fileName)}`;
                tr.innerHTML = `
                    <td>${item.id}</td>
                    <td>${item.subject}</td>
                    <td>${item.year}</td>
                    <td>${item.semester}</td>
                    <td>${item.status}</td>
                    <td><a href="${fileLink}" target="_blank" rel="noopener">${item.fileName}</a></td>
                `;
                tbody.appendChild(tr);
            });
        });

        const exportBtn = document.getElementById('exportCsvBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => {
                const rows = [['ID','Subject','Year','Semester','Status','File Name']];
                table.querySelectorAll('tbody tr').forEach(tr => {
                    const cols = Array.from(tr.children).map(td => td.textContent || '');
                    rows.push(cols);
                });
                const csv = rows.map(r => r.map(v => '"' + v.replaceAll('"', '""') + '"').join(',')).join('\n');
                const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'search_results.csv';
                a.click();
                URL.revokeObjectURL(url);
                toast('Exported CSV');
            });
        }
    }

    // Boot
    document.addEventListener('DOMContentLoaded', () => {
        setYear();
        initTheme();
        initBackToTop();
        initRefreshAll();
        renderAllPapers();
        initAddForm();
        initSearchForm();
    });
})();


