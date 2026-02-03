(function () {
    const { log, bufferToBase64Url, base64UrlToBuffer, postJson } = window.webauthnUtils;
    const registerButton = document.getElementById("register-button");
    const usernameInput = document.getElementById("register-username");

    async function registerPasskey() {
        if (!window.PublicKeyCredential) {
            log("WebAuthn is not supported in this browser.", "error");
            return;
        }

        const username = usernameInput.value.trim();
        if (!username) {
            log("Please enter a username for registration.", "error");
            return;
        }

        registerButton.disabled = true;
        try {
            log(`Requesting registration options for ${username}...`);
            const options = await postJson("/webauthn/register/options", { username });

            options.challenge = base64UrlToBuffer(options.challenge);
            options.user.id = base64UrlToBuffer(options.user.id);

            if (options.excludeCredentials) {
                options.excludeCredentials = options.excludeCredentials.map((cred) => ({
                    ...cred,
                    id: base64UrlToBuffer(cred.id),
                }));
            }

            const credential = await navigator.credentials.create({ publicKey: options });
            if (!credential) {
                throw new Error("Credential creation was cancelled.");
            }

            const payload = {
                username,
                id: credential.id,
                rawId: bufferToBase64Url(credential.rawId),
                type: credential.type,
                response: {
                    attestationObject: bufferToBase64Url(credential.response.attestationObject),
                    clientDataJSON: bufferToBase64Url(credential.response.clientDataJSON),
                },
            };

            await postJson("/webauthn/register/verify", payload);
            log("Registration completed successfully.");
        } catch (error) {
            log(error.message, "error");
        } finally {
            registerButton.disabled = false;
        }
    }

    registerButton.addEventListener("click", registerPasskey);
})();
