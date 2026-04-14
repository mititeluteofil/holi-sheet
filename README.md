# HoliSheet

An offline Android inventory app. Create named lists, take photos of items, extract text and identify objects using on-device ML Kit OCR — no internet connection required. All data stays on your device.

---

## Prerequisites — Development Machine

| Requirement | Version | Notes |
|---|---|---|
| Android Studio | Hedgehog 2023.1.1+ or Iguana 2023.2.1+ | [Download](https://developer.android.com/studio) |
| JDK | 17 | Bundled with Android Studio — verify with `java -version` |
| Android SDK | API 34 (compile), API 24 (min) | Install via Android Studio SDK Manager |
| Kotlin | 1.9.22 | Managed by Gradle — no separate install needed |
| Git | Any recent version | For cloning the repo |

> Kotlin and all library dependencies are downloaded automatically by Gradle on first build.

---

## Prerequisites — Physical Device (Recommended)

> A **physical Android device is strongly recommended** for OCR and object detection testing. The Android emulator's simulated camera produces artificial scenes that don't reflect real-world scan results.

| Requirement | How to enable |
|---|---|
| Android 7.0+ (API 24+) | Check: Settings → About Phone → Android version |
| Developer Options enabled | Settings → About Phone → tap **Build Number** 7 times |
| USB Debugging enabled | Settings → Developer Options → **USB Debugging ON** |
| Authorize your computer | When you connect via USB, tap **Allow** on the device prompt |
| Working rear camera | Required for CameraX capture and OCR processing |

---

## Get the Code

```bash
git clone https://github.com/mititeluteofil/holi-sheet.git
cd holi-sheet
```

---

## Build

### Option A — Android Studio (recommended)

1. Open Android Studio → **Open** → select the `holi-sheet` folder
2. Wait for Gradle sync to complete (first run downloads ~300 MB of dependencies)
3. Confirm no red errors in the **Build** output panel
4. **Build → Make Project**

### Option B — Command line

```bash
./gradlew assembleDebug
# Output APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## Run on Device

### Via Android Studio
1. Connect your device via USB
2. Select your device in the toolbar device dropdown
3. Press **Run ▶** (or Shift+F10)

### Via command line
```bash
./gradlew installDebug
# Then launch HoliSheet manually from the app drawer
```

---

## Manual Test Checklist

Work through these in order. Each step builds on the previous.

### Inventories
- [ ] Tap **+** on the home screen → create an inventory with a name, description, color, and emoji
- [ ] Confirm it appears on the home screen grid
- [ ] Long-press the card → tap **Edit** → change the name → save → confirm the change
- [ ] Use the search bar to filter inventories by name

### Scan & Add (Text OCR — food / packaged goods)
- [ ] Open an inventory → tap **+** → **Scan & Add**
- [ ] Grant camera permission when prompted
- [ ] Point camera at a food item with printed text (e.g. a yogurt container, cereal box)
- [ ] Tap the capture button
- [ ] Wait for the result sheet — confirm a **"Text found"** section appears with recognized text lines
- [ ] Tap the **✏️** pencil icon on a line → the text becomes an editable field pre-filled with the recognized text → edit it → tap ✓ to confirm
- [ ] Tap **"Item name"** on one suggestion
- [ ] Tap **"Label"** on another suggestion (if available)
- [ ] Tap **Continue** — confirm the Create Item screen opens with the name and labels pre-filled
- [ ] Save the item → confirm it appears in the inventory grid with the photo thumbnail

### Scan & Add (Object Detection — toys / items without text)
- [ ] Scan a physical object that has no readable text (e.g. a toy car, clothing item)
- [ ] Confirm an **"Objects detected"** section appears with category labels (e.g. "Toy", "Vehicle")
- [ ] Tap ✏️ → edit the generic label to something specific (e.g. `Red Hot Wheels Car '66 Chevy`) → confirm
- [ ] Set it as the item name → Continue → Save
- [ ] Confirm the item is saved with the edited name (not the raw ML Kit output)

### Add Item Manually
- [ ] Open an inventory → tap **+** → **Add manually**
- [ ] Fill in name, quantity (use the +/− stepper), description, notes
- [ ] Tap **+ Add label** → type a new label name → tap **Add** → confirm the chip appears
- [ ] Tap **+ Add label** again → pick an existing label from the list
- [ ] Save → confirm the item appears with label chips visible

### Item Detail & Edit
- [ ] Tap an item card → confirm the detail screen shows photo, name, quantity, labels, notes
- [ ] If OCR text was captured, tap **Show** under "Recognized text" → confirm raw text is visible
- [ ] Tap the **edit FAB** → change the item name → save → confirm the update

### Delete
- [ ] Swipe an item card left (inside an inventory) → confirm the delete prompt → delete
- [ ] Long-press an inventory card on the home screen → **Delete** → confirm

### Search
- [ ] Add several items with different names across different inventories
- [ ] Use the search bar inside an inventory to filter by item name
- [ ] Use the search bar on the home screen to filter inventories by name

### Sort
- [ ] Inside an inventory with multiple items, tap the sort icon (top-right)
- [ ] Switch between **Newest first**, **Name A→Z**, **Quantity** — confirm the list reorders

### Persistence
- [ ] Kill the app completely (swipe away from recents)
- [ ] Reopen — confirm all inventories and items are still present

### Offline
- [ ] Enable **Airplane Mode** on the device
- [ ] Open the app → create an inventory, scan an item, add labels
- [ ] Confirm everything works with no network connection

---

## ML Kit / OCR Notes

| Engine | What it does | Works offline? |
|---|---|---|
| ML Kit Text Recognition | Reads printed text from labels, packaging, signs | Yes — model is bundled in APK |
| ML Kit Image Labeling | Identifies objects visually (toy, vehicle, food, etc.) | Yes — model is bundled in APK |

- No internet permission is declared in the app — all recognition is fully on-device
- Image Labeling only shows results with ≥ 70% confidence
- Image Labeling returns generic categories — users are expected to refine the label via the inline edit before saving
- Text Recognition works best with good lighting and clear, high-contrast text
- Both engines run in parallel on each captured photo

---

## Emulator Limitations

| Feature | Emulator | Physical device |
|---|---|---|
| Camera preview | Simulated scene | Real camera |
| Text OCR | Only reads simulated scene text | Real packaging/labels |
| Object detection | Detects simulated objects | Real objects |
| Performance | Slower | Native speed |

Use a physical device for all OCR and scan-related testing.

---

## Troubleshooting

**Gradle sync fails**
- File → Invalidate Caches → Restart
- Ensure Android SDK API 34 is installed (SDK Manager → SDK Platforms)

**Device not detected**
- Check USB cable (use a data cable, not a charge-only cable)
- Revoke USB debugging authorizations on device and re-authorize

**Camera permission denied and can't re-prompt**
- Go to device Settings → Apps → HoliSheet → Permissions → Camera → Allow

**No text/objects recognized**
- Improve lighting — avoid glare and shadows
- Hold the camera steady and close enough to fill the frame with the item
- For packaged goods, ensure the text side faces the camera
