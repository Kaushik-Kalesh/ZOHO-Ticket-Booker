async function loadNavbar() {
    const res = await fetch("navbar.html");
    const html = await res.text();

    document.getElementById("navbar-container").innerHTML = html;

    // Attach logout handler AFTER injection
    document.getElementById("logout-link").addEventListener("click", e => {
        e.preventDefault();
        window.location.href = "/TicketBooker-0.1";
    });
}

document.addEventListener("DOMContentLoaded", loadNavbar);
