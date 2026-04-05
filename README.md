# KAPrivateMessage

Plugin Private Message lintas platform untuk **Velocity + Paper**.

## 1) Arsitektur Proyek

Project ini memakai Maven multi-module:

- `common` → shared constants + packet serializer
- `velocity` → logic PM utama (command, privacy, ignore, social spy, anti-spam)
- `paper` → backend bridge (sound playback, GUI selector, hooks PlaceholderAPI/MiniPlaceholders/Essentials)

Komunikasi data proxy-backend menggunakan plugin messaging channel:

- Channel: `kaprivatemessage:main`

## 2) Fitur Utama

- PM lintas server: `/msg`, `/reply`
- Smart targeting: nama asli → nick exact → nick partial (min 3 karakter)
- Privacy mode: `None`, `Low`, `Medium`, `High`
- Ignore system: `/ignore add|remove|list|clear`
- Social Spy: `/socialspy`
- Anti-spam cooldown
- PM sound send/receive + GUI selector

## 3) Placeholder Internal di Format Pesan

Template pesan di `velocity/config.yml` sekarang mendukung placeholder internal berikut:

### Sender
- `{sender-name}`
- `{sender-realname}`
- `{sender-prefix}`
- `{sender-server-id}`
- `{sender-server-name}`
- `{sender-server-formatted}`

### Receiver (juga support typo `reciever-*` untuk kompatibilitas)
- `{receiver-name}` / `{reciever-name}`
- `{receiver-realname}` / `{reciever-realname}`
- `{receiver-prefix}` / `{reciever-prefix}`
- `{receiver-server-id}` / `{reciever-server-id}`
- `{receiver-server-name}` / `{reciever-server-name}`
- `{receiver-server-formatted}` / `{reciever-server-formatted}`

### Umum
- `{message}`
- `{sender}` (alias display sender)
- `{target}` (alias display receiver)

Contoh default format:

- `format-send`: menampilkan receiver prefix + name + server formatted
- `format-receive`: menampilkan sender prefix + name + server formatted
- `format-socialspy`: menampilkan server id sender/receiver

## 4) Instalasi

### Prasyarat
- Proxy: **Velocity**
- Backend: **Paper/Spigot-compatible** (plugin backend ada di module `paper`)
- Disarankan EssentialsX jika ingin fitur nickname hook

### Build JAR
1. Build project dari root:
   - `mvn clean package`
2. Ambil hasil build:
   - Proxy plugin: `velocity/target/*.jar`
   - Backend plugin: `paper/target/*.jar`

### Pasang ke server
1. Copy JAR Velocity ke folder `plugins/` pada server proxy Velocity.
2. Copy JAR Paper ke folder `plugins/` pada **setiap** backend server Paper yang terhubung ke Velocity.
3. Restart proxy dan semua backend server.

### Verifikasi instalasi
- Pastikan plugin load tanpa error di console Velocity dan Paper.
- Cek command tersedia:
  - `/msg`
  - `/reply`
  - `/pmsound`
  - `/pmprivacy`

## 5) Konfigurasi Paper (`paper/config.yml`)

Ditambahkan file config backend Paper:

```yaml
server:
  id: "lobby-1"
  name: "Lobby"
  formatted: "<gray>[Lobby]</gray>"

bridge:
  send-prefix: true
  send-nickname: true

database:
  type: "none" # none|mariadb|redis
  host: "127.0.0.1"
  port: 3306
  name: "kaprivatemessage"
  username: "root"
  password: "change_me"
```

### Keterangan

- `server.id/name/formatted`  
  Dipakai untuk metadata server pada placeholder internal sender/receiver.
- `bridge.send-prefix`  
  Menentukan apakah prefix dikirim ke Velocity.
- `bridge.send-nickname`  
  Menentukan apakah nickname Essentials dikirim ke Velocity.
- `database.*`  
  Disediakan untuk integrasi backend database (saat ini sebagai konfigurasi fondasi).

## 6) Placeholder API Hook (Paper)

Prefix PlaceholderAPI: `%pm_*%`

Placeholder aktif:

- `%pm_status%`
- `%pm_plugin_name%`
- `%pm_plugin_version%`
- `%pm_server_id%`
- `%pm_server_name%`
- `%pm_server_formatted%`

MiniPlaceholders namespace: `pm`

- `<pm:status>`
- `<pm:plugin>`
- `<pm:server_id>`
- `<pm:server_name>`
- `<pm:server_formatted>`

## 7) Alur Data Proxy ↔ Backend

### Paper → Velocity (sync metadata pemain)
Saat player join backend Paper:
1. Paper baca nickname (Essentials), realname, prefix (jika diaktifkan), dan data server dari config.
2. Paper kirim packet `NICKNAME_UPDATE` ke Velocity.
3. Velocity simpan metadata player di memory cache.
4. Saat `/msg` dieksekusi, formatter akan resolve placeholder sender/receiver dari cache ini.

### Velocity → Paper (audio/GUI)
- `PLAY_SOUND` untuk memainkan sound ke player tujuan.
- `REQUEST_SOUND_GUI` untuk membuka sound selector GUI di backend tempat player berada.

## 8) Cara Menggunakan

### Penggunaan pemain
- Kirim PM:
  - `/msg <player> <message>`
- Balas PM terakhir:
  - `/reply <message>`
- Atur privasi PM:
  - `/pmprivacy <High|Medium|Low|None>`
- Atur suara PM:
  - `/pmsound toggle <on|off>`
  - `/pmsound gui`
- Ignore player:
  - `/ignore add <player>`
  - `/ignore remove <player>`
  - `/ignore list`
  - `/ignore clear`

### Penggunaan admin/staff
- Lihat PM lewat social spy:
  - `/socialspy [on|off]`
- Kelola ignore player (admin):
  - `/adminignore list <player>`
  - `/adminignore clear <player>`

## 9) Command Ringkas

- `/msg <player> <message>` (alias: w, tell, pm, m, t, whisper)
- `/reply <message>` (alias: r)
- `/pmprivacy <High|Medium|Low|None>`
- `/pmsound toggle <on|off>`
- `/pmsound gui`
- `/ignore add|remove|list|clear [player]`
- `/adminignore list|clear <player>`
- `/socialspy [on|off]`

## 10) Detail Rules Nickname EssentialsX (Targeting vs Display)

### A. Saat sender dan target di server backend yang sama
- Target bisa dicari pakai:
  - realname
  - nickname EssentialsX (exact / partial, min 3 char)
- Di format PM, nama yang tampil mengikuti nickname aktif pemain (jika ada), kalau tidak ada fallback ke realname.

### B. Saat sender dan target beda backend server (cross-server)
- Target **harus** dicari dengan **realname**.
- Pencarian dengan nickname tidak digunakan untuk target lintas server.
- Di format PM, nama tampilan tetap:
  - nickname jika pemain sedang nicked
  - realname jika tidak nicked

## 11) Color Code pada Isi Private Message

Isi pesan `/msg` dan `/reply` sekarang mendukung color code berikut:

- Legacy: `&a`, `&b`, `&c`, ..., `&l`, `&n`, `&o`, `&m`, `&k`, `&r`
- Legacy Hex:
  - format panjang: `&x&R&R&G&G&B&B`
  - format pendek: `&#RRGGBB`

Semua format di atas bisa dikombinasikan dalam satu pesan.

### Permission

- Permission untuk pakai color code:
  - `kaprivatemessage.pm.color`

Jika player **tidak** punya permission ini, isi PM dikirim sebagai teks biasa (tanpa parsing color code), sementara format default template PM plugin tetap berjalan normal.
