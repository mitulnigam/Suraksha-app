# Quick SOS Test Script

## 1. Install & Setup (1 minute)
```powershell
# Install updated APK
adb install -r "C:\Users\rogue\OneDrive\Documents\MATLAB\Suraksha new - 2\Suraksha\app\build\outputs\apk\debug\app-debug.apk"

# Grant permissions
adb shell "pm grant com.suraksha.app android.permission.SEND_SMS"
adb shell "pm grant com.suraksha.app android.permission.ACCESS_FINE_LOCATION"
```

## 2. Add Contact (30 seconds)
- Open app
- Go to **Contacts** tab
- Tap **+ button**
- Add your own phone number for testing
- Name: Test, Phone: your number

## 3. Manual SOS Test (30 seconds)
```powershell
# Start logging
adb logcat -c
adb logcat | Select-String "SosReceiver|AlertManager|SMS SENT"

# In another terminal, trigger SOS
adb shell "am broadcast -a com.suraksha.app.ACTION_TRIGGER_SOS"
```

### ✅ Expected Result:
Within 10 seconds you should see:
```
SosReceiver: ⚠️ SOS TRIGGER RECEIVED!
AlertManager: Found 1 contacts in database
AlertManager: Contact 1: Test - [your number]
AlertManager: ✅ SMS SENT to Test at [your number]
AlertManager: === SOS ALERT COMPLETE: 1 sent, 0 failed ===
```

**AND receive SMS on your phone!**

---

## 4. Full Fall Detection Test (2 minutes)
```powershell
# Start logging
adb logcat -c
adb logcat | Select-String "FALL|Confirmation|SOS"
```

**On Phone:**
1. Open app → Settings
2. Enable "Fall Detection"
3. Hold phone and simulate fall:
   - Quick downward motion
   - Sudden stop
   - Keep still 2-3 seconds

**Expected:**
- Confirmation dialog appears
- 15-second countdown
- After countdown: SMS sent automatically

---

## Troubleshooting

### No SMS Received?
```powershell
# Check contacts
adb shell "run-as com.suraksha.app sqlite3 /data/data/com.suraksha.app/databases/suraksha_database 'SELECT name, phoneNumber FROM contacts;'"
```

If empty: **Add contacts in app first!**

### Check Logs for Errors:
```powershell
adb logcat -d | Select-String "AlertManager.*❌|FAILED|error"
```

Common errors:
- `NO TRUSTED CONTACTS FOUND` → Add contacts in app
- `Permission denied` → Grant SMS permission
- `Failed to get SmsManager` → Test on real phone with SIM

---

## Success Criteria

✅ Logs show: `SOS TRIGGER RECEIVED`  
✅ Logs show: `Found X contacts` (where X > 0)  
✅ Logs show: `SMS SENT to [Name]`  
✅ Logs show: `SOS ALERT COMPLETE: X sent, 0 failed`  
✅ **SMS received on contact's phone**  

**If all ✅ → Working perfectly!**

---

## Quick Commands

### One-Line Test:
```powershell
adb shell "am broadcast -a com.suraksha.app.ACTION_TRIGGER_SOS" ; Start-Sleep 5 ; adb logcat -d | Select-String "sent, \d+ failed"
```

### Check Last SOS Status:
```powershell
adb logcat -d | Select-String "SOS ALERT COMPLETE" | Select-Object -Last 1
```

### Verify Receiver Works:
```powershell
adb shell "am broadcast -a com.suraksha.app.ACTION_TRIGGER_SOS" ; Start-Sleep 1 ; adb logcat -d | Select-String "SOS TRIGGER RECEIVED" | Select-Object -Last 1
```

If you see `SOS TRIGGER RECEIVED` → Broadcast working!  
If you see `SMS SENT` → SMS working!  
If you receive SMS → **Everything working!** 🎉

