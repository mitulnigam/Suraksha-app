# Places API Setup Instructions

Your app is not showing hospitals and police stations because the **Places API (New)** might not be enabled for your Google Maps API key.

## Steps to Enable Places API:

### 1. Go to Google Cloud Console
- Visit: https://console.cloud.google.com/
- Sign in with your Google account

### 2. Select Your Project
- In the top navigation bar, click on the project dropdown
- Select the project associated with your API key: **AIzaSyB_zzihs-ogi-GK5WMUeILm7D3r1TmyQRU**
- If you don't know which project, look for "suraksha" or similar name

### 3. Enable Required APIs
- In the left sidebar, click on **"APIs & Services"** → **"Library"**
- Search for and enable the following APIs (click each, then click "Enable"):
  - **Places API (New)** ⚠️ IMPORTANT - This is the main API needed
  - **Maps SDK for Android** (should already be enabled)
  - **Geocoding API** (optional, but recommended)

### 4. Verify API Key Restrictions
- Go to **"APIs & Services"** → **"Credentials"**
- Find your API key: **AIzaSyB_zzihs-ogi-GK5WMUeILm7D3r1TmyQRU**
- Click on it to edit
- Under **"API restrictions"**, make sure the following are selected:
  - Maps SDK for Android
  - Places API (New)
  - Geocoding API (optional)

### 5. Check Billing
⚠️ **IMPORTANT**: Places API (New) requires billing to be enabled on your Google Cloud project.
- Go to **"Billing"** in the left sidebar
- Make sure a valid billing account is linked
- Don't worry - Google provides $200 free credit per month, which is more than enough for development

### 6. Wait for Propagation
- After enabling the APIs, wait 2-5 minutes for changes to propagate
- Then rebuild and run your app

## Troubleshooting:

### If places still don't show:
1. Check Android Studio Logcat for error messages (filter by "MapViewModel" or "Places")
2. Look for errors like:
   - "This API key is not authorized to use this service"
   - "PLACES_API_ACCESS_NOT_CONFIGURED"
   - "REQUEST_DENIED"

### Check your API key in the code:
- File: `local.properties`
- Should contain: `MAPS_API_KEY=AIzaSyB_zzihs-ogi-GK5WMUeILm7D3r1TmyQRU`

### Verify Places SDK initialization:
- Check Logcat for: "Places SDK initialized" message
- If you see "Places SDK is not initialized", the API key might be wrong

## Testing:
After completing the setup:
1. Rebuild the app: **Build → Rebuild Project**
2. Uninstall the old app from your device
3. Install and run the new version
4. Grant location permissions when prompted
5. Navigate to the Map screen
6. Wait a few seconds for markers to appear

## What the Code Does:
- Searches for "police station near me" within 5km radius
- Searches for "hospital near me" within 5km radius
- Shows results as red markers (police) and green markers (hospitals)
- Your location is shown as a cyan marker

## Expected Results:
- You should see multiple markers on the map
- Counter at the top shows: "Found: X Police Stations, Y Hospitals (within 5km)"
- If counter shows "Found: 0 Police Stations, 0 Hospitals", the API is not working

## Additional Notes:
- The app uses the New Places API (places:3.5.0 library)
- Search radius is 5km (5000 meters)
- Maximum 20 results per search query
- Results are ranked by distance from your location

