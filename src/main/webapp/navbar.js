async function loadNavbar() {
    const res = await fetch("navbar.html");
    const html = await res.text();

    document.getElementById("navbar-container").innerHTML = html;

    const usernameEl = document.getElementById("username");
    const storedUsername = localStorage.getItem("username");

    if (storedUsername) {
        usernameEl.innerText = storedUsername;
    }

    document.getElementById("logout-link")
        .addEventListener("click", function (e) {
            e.preventDefault();
            localStorage.clear();
            window.location.href = "/TicketBooker-0.1";
        });
}

document.addEventListener("DOMContentLoaded", loadNavbar);
