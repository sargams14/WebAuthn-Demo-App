(function () {
    const { log, bufferToBase64Url, base64UrlToBuffer, postJson } = window.webauthnUtils;
    const authButton = document.getElementById("auth-button");
    const usernameInput = document.getElementById("auth-username");

    async function authenticate() {
        if (!window.PublicKeyCredential) {
            log("WebAuthn is not supported in this browser.", "error");
            return;
        }

        const username = usernameInput.value.trim();
        if (!username) {
            log("Please enter a username to authenticate.", "error");
            return;
        }

        authButton.disabled = true;
        try {
            log(`Requesting authentication options for ${username}...`);
            const options = await postJson("/webauthn/authenticate/options", { username });

            options.challenge = base64UrlToBuffer(options.challenge);
            if (options.allowCredentials) {
                options.allowCredentials = options.allowCredentials.map((cred) => ({
                    ...cred,
                    id: base64UrlToBuffer(cred.id),
                }));
            }

            const assertion = await navigator.credentials.get({ publicKey: options });
            if (!assertion) {
                throw new Error("Authentication was cancelled.");
            }

            const payload = {
                username,
                id: assertion.id,
                rawId: bufferToBase64Url(assertion.rawId),
                type: assertion.type,
                response: {
                    authenticatorData: bufferToBase64Url(assertion.response.authenticatorData),
                    clientDataJSON: bufferToBase64Url(assertion.response.clientDataJSON),
                    signature: bufferToBase64Url(assertion.response.signature),
                    userHandle: assertion.response.userHandle
                        ? bufferToBase64Url(assertion.response.userHandle)
                        : null,
                },
            };

            await postJson("/webauthn/authenticate/verify", payload);
            log("Authentication completed successfully.");
        } catch (error) {
            log(error.message, "error");
        } finally {
            authButton.disabled = false;
        }
    }

    authButton.addEventListener("click", authenticate);
})();
