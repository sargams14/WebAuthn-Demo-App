(function () {
    const logElement = document.getElementById("log");

    function log(message, type) {
        const prefix = type === "error" ? "[error]" : "[info]";
        const text = `${prefix} ${message}`;
        if (logElement.textContent === "Ready.") {
            logElement.textContent = text;
        } else {
            logElement.textContent += `\n${text}`;
        }
    }

    function bufferToBase64Url(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = "";
        bytes.forEach((b) => (binary += String.fromCharCode(b)));
        return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
    }

    function base64UrlToBuffer(base64Url) {
        const padding = "=".repeat((4 - (base64Url.length % 4)) % 4);
        const base64 = (base64Url + padding).replace(/-/g, "+").replace(/_/g, "/");
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i += 1) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }

    async function postJson(url, payload) {
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || `Request failed with ${response.status}`);
        }

        return response.json();
    }

    window.webauthnUtils = {
        log,
        bufferToBase64Url,
        base64UrlToBuffer,
        postJson,
    };

    if (!window.isSecureContext) {
        log("WebAuthn requires a secure context (https or localhost).", "error");
    }
})();
