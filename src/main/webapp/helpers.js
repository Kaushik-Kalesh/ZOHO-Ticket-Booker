document.addEventListener("click", (e) => {
    const openId = e.target.dataset.open;

    if (openId) {
        document.getElementById(openId)?.classList.add("open");
    }

    if (e.target.classList.contains("modal")) {
        e.target.classList.remove("open");
    }
});

Handlebars.registerHelper("timeDisplay", iso => {
    if (!iso) return "";
    const d = new Date(iso);
    const hh = String(d.getHours()).padStart(2, "0");
    const min = String(d.getMinutes()).padStart(2, "0");
    return `${hh}:${min}`;
});

Handlebars.registerHelper("formatDate", dateStr => {
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-IN", {
        weekday: "long",
        year: "numeric",
        month: "long",
        day: "numeric"
    });
});