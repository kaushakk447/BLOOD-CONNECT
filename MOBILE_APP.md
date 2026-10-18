# Building the BloodConnect Android app

The React frontend is packaged as an Android app with Capacitor. The Spring Boot
API and PostgreSQL database stay on a server; they are not bundled into the APK.

## Before building

1. Deploy the backend to a public HTTPS address, such as
   `https://api.example.com/api`. The `/api` suffix is required because Spring
   Boot uses it as its context path.
2. Configure the deployed backend with a production database, a strong
   `JWT_SECRET`, and the WebView origin:

   ```text
   APP_CORS_ALLOWED_ORIGINS=https://localhost
   ```

   Add your web domain to that comma-separated value if you also deploy the web
   frontend, for example `https://app.example.com,https://localhost`.
3. Install Android Studio and an Android SDK. This computer currently does not
   have them installed, so it cannot produce an APK yet.

## Create the Android build

From `frontend` in PowerShell:

```powershell
Copy-Item .env.android.example .env.android
```

Edit `.env.android` and replace the example URL with the public API URL. Then
run:

```powershell
npm run sync:android
npm run open:android
```

Android Studio opens the native project. Use **Build → Generate Signed Bundle /
APK** to create a release APK or Play Store app bundle. Before publishing,
replace the package ID `com.bloodconnect.app` with a unique ID you own and add
your production launcher icons.

## Everyday frontend change workflow

After changing React code, run `npm run sync:android`, then rebuild in Android
Studio. The command rebuilds the Vite site, validates the API URL, and copies the
new web files into the native Android project.
