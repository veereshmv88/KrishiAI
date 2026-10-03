# KrishiAI 🌿 — Real-Time Farmer Marketplace

A WhatsApp-like real-time platform connecting Karnataka farmers directly with buyers.  
Built with **Node.js + Socket.IO + lowdb** — works locally and deploys to the cloud in minutes.

---

## ✨ Features

- 💬 **1-to-1 Real-Time Chat** — private WhatsApp-style conversations between each buyer and farmer per product
- 🟢 **Live Market Feed** — buyer dashboard updates instantly when farmers post new crops  
- 🔔 **Push Notifications** — unread badges, toast popups, typing indicators
- 🌐 **EN / ಕನ್ನಡ** — full bilingual UI support
- 🌙 **Dark Mode** — system-aware with manual toggle
- 🤖 **AI Price Engine** — explainable fair-price recommendations
- ☁️ **Cloud Ready** — one-click Railway deployment

---

## 🚀 Run Locally

```bash
npm install
node server.js
```

Open **http://localhost:3001**

### Test Chat (two windows):
1. Open two browser windows at the same URL
2. **Window 1** → Register as **Farmer** → Post a crop
3. **Window 2** → Register as **Buyer** → Open the crop → tap **💬 Chat with Farmer**
4. Messages appear instantly in both windows ✅

---

## ☁️ Deploy Online (Railway — Free)

> Railway gives you a **public HTTPS URL** accessible from any device worldwide.

### Step 1 — Push to GitHub
```bash
git init
git add .
git commit -m "KrishiAI v3 - Real-Time Chat"
git remote add origin https://github.com/YOUR_USERNAME/krishiai.git
git push -u origin main
```

### Step 2 — Deploy on Railway
1. Go to [railway.app](https://railway.app) and sign up (free)
2. Click **"New Project"** → **"Deploy from GitHub repo"**
3. Select your repository
4. Railway auto-detects Node.js and deploys in ~2 minutes
5. Click **"Generate Domain"** to get your public URL (e.g. `krishiai.up.railway.app`)

> **Data persistence**: Railway preserves your `krishi.json` file between restarts within the same deployment. For permanent persistence across deploys, add a Railway volume pointing to `/app`.

### Alternative: Render.com
1. Go to [render.com](https://render.com) → New Web Service
2. Connect GitHub repo → Build Command: `npm install` → Start Command: `node server.js`
3. Free tier URL format: `https://krishiai.onrender.com`

---

## 🔌 API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register farmer or buyer |
| POST | `/api/auth/login` | Login |
| GET | `/api/products` | All available listings |
| POST | `/api/products` | Farmer publishes a crop |
| PATCH | `/api/products/:id` | Update product status |
| DELETE | `/api/products/:id` | Delete listing |
| GET | `/api/conversations?farmId=` | Farmer's message inbox |
| GET | `/api/conversations?buyerUid=` | Buyer's message inbox |
| GET | `/api/conversations/:id` | Full chat history |
| GET | `/api/ping` | Health check |

## 🔌 Socket.IO Events

| Event | Direction | Purpose |
|-------|-----------|---------|
| `user_online` | C→S | Register presence |
| `join_chat` | C→S | Join private conversation room |
| `send_message` | C→S | Send a chat message |
| `typing` | C→S | Typing indicator |
| `mark_read` | C→S | Mark messages as read |
| `new_message` | S→C | Deliver message to room |
| `chat_history` | S→C | Load chat history on join |
| `products_updated` | S→All | Live product list push |
| `chat_notification` | S→All | Unread badge + toast |
| `presence_update` | S→All | Online/offline status |

---

## 🗃️ Data Storage

All data is stored in `krishi.json` using [lowdb](https://github.com/typicode/lowdb):

```json
{
  "users": [...],
  "products": [...],
  "holds": [...],
  "conversations": {
    "productId::buyerUid": [messages...]
  }
}
```

The conversation key `productId::buyerUid` ensures each buyer has a **private 1-to-1 channel** with the farmer for each product.
