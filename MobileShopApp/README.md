# Mobile Zone — Mobile Accessories Shop App (Android, Java)

A Flipkart-style shopping app for a mobile-accessories store, with:
- **Customer side**: browse products, search, view details/photos/video, "Buy Now",
  see order history, and a floating **WhatsApp icon** to message the admin directly
  (also shown when a product is out of stock).
- **Admin side** (same app, one special login): add/edit/delete products with price,
  photo and video, change the shop logo, update the WhatsApp contact number, and
  view/confirm all customer orders.

Backend: **Firebase** (Authentication + Realtime Database + Storage) — free tier is enough
to get started, no custom server needed.

---

## 1. What's in this zip

```
MobileShopApp/
  app/
    src/main/java/com/mobileshop/app/       -> customer screens
    src/main/java/com/mobileshop/app/admin/ -> admin screens
    src/main/java/com/mobileshop/app/model/ -> Product.java, Order.java
    src/main/java/com/mobileshop/app/adapter/ -> RecyclerView adapters
    src/main/java/com/mobileshop/app/utils/   -> Constants.java, WhatsAppHelper.java
    src/main/res/                             -> all layouts, icons, colors, strings
    build.gradle
  build.gradle
  settings.gradle
  gradle.properties
```

This is a **standard Android Studio project written in Java**. There's no `google-services.json`
included (that file is unique to your own Firebase project and must never be shared publicly),
and the Gradle wrapper jar isn't included either — Android Studio regenerates both automatically,
see steps below.

---

## 2. One-time setup

### Step 1 — Install Android Studio
Download from https://developer.android.com/studio if you don't already have it.

### Step 2 — Open the project
Unzip the file, then in Android Studio: **File → Open** → select the `MobileShopApp` folder.
Let Gradle sync (it will show errors about a missing `google-services.json` — that's expected,
fix it in the next step).

### Step 3 — Create your Firebase project
1. Go to https://console.firebase.google.com → **Add project** → give it any name.
2. Inside the project, click **Add app → Android**.
3. For **Android package name**, enter exactly: `com.mobileshop.app`
   (must match `applicationId` in `app/build.gradle`).
4. Download the generated **`google-services.json`** file.
5. Copy that file into the `app/` folder of this project, i.e.
   `MobileShopApp/app/google-services.json` (same level as `app/build.gradle`).
6. Sync Gradle again in Android Studio (elephant icon / "Sync Now" banner). The errors go away.

### Step 4 — Turn on the Firebase products this app uses
In the Firebase console, still inside your project:

- **Build → Authentication → Get started → Sign-in method → Email/Password → Enable.**
- **Build → Realtime Database → Create Database** (pick any region). Start in **test mode**
  for development, then before going live replace the rules with the ones below.
- **Build → Storage → Get started** (this stores product photos and the shop logo). Start
  in test mode too, then apply the rules below before going live.

### Step 5 — Recommended security rules (apply once you're done testing)

**Realtime Database rules:**
```json
{
  "rules": {
    "products": {
      ".read": true,
      ".write": "auth != null"
    },
    "settings": {
      ".read": true,
      ".write": "auth != null"
    },
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    },
    "orders": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```
This lets anyone browse products (no login needed to look, only to buy), but only signed-in
users can write data. For a production app you'd tighten `products`/`settings` writes to only
your admin's UID — ask if you'd like that stricter version.

**Storage rules:**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

### Step 6 — Set who the admin is
Open `app/src/main/java/com/mobileshop/app/utils/Constants.java` and change:

```java
public static final String ADMIN_EMAIL = "admin@mobileshop.com";
public static final String DEFAULT_WHATSAPP_NUMBER = "910000000000"; // country code + number, digits only
```

Whichever email you put here becomes the admin — **register in the app itself using
that exact email** (Register screen) and you'll land straight on the Admin Dashboard,
today and every time you log back in. Every other email that registers is treated as
a normal customer.

You can also change the WhatsApp number later, anytime, from inside the app:
**Admin Dashboard → Change Logo screen → WhatsApp Contact Number** (that screen handles
both the logo and the number).

### Step 7 — Run it
Click the green **Run ▶** button in Android Studio with an emulator or a real device
connected (USB debugging enabled).

---

## 3. How the app works

**Customer flow**
1. Register/Login.
2. Home screen shows all products in a grid (photo, name, price, stock status), with search.
3. Tap a product → full details, description, price, and (if the admin added one) a video link.
4. **Buy Now** → creates an order in Firebase and opens WhatsApp with the order details
   pre-filled, so the customer and admin can confirm payment/delivery in chat (no payment
   gateway is wired up — this mirrors how most small accessory shops actually operate).
5. If a product is **out of stock**, Buy Now is disabled and the customer is guided to the
   WhatsApp icon instead.
6. The green **WhatsApp floating button** is on every screen — tapping it always opens a
   chat with the admin's number.
7. **My Orders** (menu, top-right) shows the customer's own order history and status.

**Admin flow** (log in with the email set in `Constants.ADMIN_EMAIL`)
1. **Add Product** — name, category, price, description, photo (picked from gallery,
   uploaded to Firebase Storage), optional video URL, and an "In Stock" checkbox.
2. **Manage Products** — list of everything you've added, with **Edit** and **Delete**.
3. **Change Logo** — replace the logo shown in the customer app's toolbar, and update the
   WhatsApp number used everywhere.
4. **View Orders** — every order placed by every customer, with a button to mark it
   "Confirmed" once you've sorted payment/delivery over WhatsApp.

---

## 4. Notes & easy customizations

- **Currency**: prices are formatted as Indian Rupees (`₹`) via `NumberFormat` with locale
  `en-IN`. To change currency, edit the `Locale("en", "IN")` lines in `ProductAdapter.java`
  and `ProductDetailActivity.java`.
- **Product video**: kept simple as a pasted URL (YouTube link or direct `.mp4` link) rather
  than an in-app upload, to avoid large video uploads through the phone's data connection.
  If you want in-app video **upload** instead (like the image), it follows the exact same
  Firebase Storage pattern already used for photos in `AddEditProductActivity.java`.
- **Multiple product images**: currently one photo per product. Extending to a photo gallery
  per product just means storing a list of URLs instead of one string on `Product.java`.
- **Categories/filters**: the search bar already matches both name and category — you can
  add a category filter chip row on `MainActivity` fairly easily if you want it.
- **App icon/name**: the launcher icon is a simple placeholder (`ic_launcher_foreground.xml`
  / `ic_launcher_background.xml`) — swap those for your real logo, and change `app_name` in
  `res/values/strings.xml`.

## 5. If Gradle sync still complains
- "File google-services.json is missing" → you skipped Step 3.5 above.
- "SDK location not found" → File → Project Structure → set your Android SDK path (or create
  a `local.properties` file with `sdk.dir=/path/to/Android/sdk`).
- Any dependency version conflict → click "Sync Project with Gradle Files" again after
  Android Studio auto-updates the Android Gradle Plugin if it prompts you to.
