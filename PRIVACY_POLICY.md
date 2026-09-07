# Privacy Policy for Internet Speed Meter

**Effective Date:** September 8, 2026  
**Developer:** memamun ([github.com/memamun](https://github.com/memamun))  
**Contact:** a.a.mamun595@gmail.com  

Internet Speed Meter ("we", "our", or "the app") is committed to protecting your privacy. This policy outlines how your data is handled.

---

### 1. Zero Data Collection
Internet Speed Meter is an on-device utility application:
- **No personal data is collected, stored, or transmitted.**
- **No analytics, ads, or tracking SDKs** are integrated.
- All network telemetry (speeds, daily totals) is computed locally and stored in your device's private sandbox.

---

### 2. Permissions & On-Device Usage

| Permission | Purpose | Data Handling |
| :--- | :--- | :--- |
| `FOREGROUND_SERVICE_DATA_SYNC` | Keeps the speed monitoring service active with a persistent status bar notification. | On-device calculation only. |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Required by Android OS to read the connected Wi-Fi router SSID name. | Never locates, records, or uploads your physical coordinates. |
| `POST_NOTIFICATIONS` | Displays real-time speed and data usage in the notification shade and status bar. | Local system notification only. |
| `RECEIVE_BOOT_COMPLETED` | Automatically restarts the meter service after device reboot (if enabled). | Local system broadcast receiver. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Optional user toggle to prevent the OS from terminating the meter. | Standard Android power management intent. |

---

### 3. Third-Party Sharing
We do not share, sell, or transmit any data to third parties.

---

### 4. Contact
If you have any questions, please reach out to **a.a.mamun595@gmail.com**.
