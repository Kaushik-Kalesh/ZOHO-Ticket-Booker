document.addEventListener("click", (e) => {
    const openId = e.target.dataset.open;

    if (openId) {
        document.getElementById(openId)?.classList.add("open");
    }

    if (e.target.classList.contains("modal")) {
        e.target.classList.remove("open");
    }
});

Handlebars.registerHelper("timeDisplay", (iso) => {
    if (!iso) return "";
    const d = new Date(iso);
    const dd = String(d.getDate()).padStart(2, "0");
    const mm = String(d.getMonth() +1).padStart(2, "0");
    const yyyy = d.getFullYear();
    const hh = String(d.getHours()).padStart(2, "0");
    const min = String(d.getMinutes()).padStart(2, "0");
    return `${dd}-${mm}-${yyyy} ${hh}:${min}`;
});