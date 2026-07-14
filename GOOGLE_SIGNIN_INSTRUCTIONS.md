# Google Sign-In Setup Instructions

To enable Google Sign-In in the app, you need to configure an OAuth client ID in the Google Cloud Console.

## Steps to create the OAuth Client ID:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Click on **Create Credentials** > **OAuth client ID**.
3. Select **Android** as the Application type.
4. Enter a Name (e.g., "Android Client 1").
5. In the **Package name** field, enter:
   `com.aistudio.craftmarket.qvxwrx`
6. In the **SHA-1 certificate fingerprint** field, enter:
   `BF:38:B9:53:60:8B:00:D2:5C:D1:5C:05:34:D9:3D:61:76:A0:8E:AB`
7. Click **Create**.
8. After creating the OAuth Client ID, you will also need to create a **Web application** type OAuth Client ID (for the server-side token verification/Firebase). Copy its **Client ID** string.
9. Open the **Secrets panel** in AI Studio and add a new secret named `GOOGLE_WEB_CLIENT_ID` with the value of your Web Application Client ID.
10. The app will then use this Client ID to securely handle Google Sign-In.
