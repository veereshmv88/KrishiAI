/**
 * KrishiAI – Marketplace Server  v4.0
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *  ✅ REST API – Auth (signup / login)
 *  ✅ REST API – Products (CRUD)
 *  ✅ Socket.IO – Live product broadcast to all buyers
 *  ✅ Persistent JSON DB (lowdb v1)
 *  ✅ Works locally AND online (Railway / Render / any Node host)
 *  ❌ Chat / Conversations removed
 */

const express    = require('express');
const cors       = require('cors');
const path       = require('path');
const crypto     = require('crypto');
const os         = require('os');
const http       = require('http');
const { Server } = require('socket.io');
const low        = require('lowdb');
const FileSync   = require('lowdb/adapters/FileSync');

/* ── Database ────────────────────────────────────────────── */
const adapter = new FileSync(path.join(__dirname, 'krishi.json'));
const db      = low(adapter);

db.defaults({ users: [], products: [], holds: [] }).write();

/* ── Express + Socket.IO ─────────────────────────────────── */
const app    = express();
const server = http.createServer(app);
const io     = new Server(server, {
  cors: { origin: '*', methods: ['GET', 'POST', 'PATCH', 'DELETE'] },
});

const PORT = process.env.PORT || 3001;

/* ── Middleware ───────────────────────────────────────────── */
app.use(cors({ origin: '*' }));
app.use(express.json({ limit: '8mb' }));
app.use(express.static(path.join(__dirname)));

/* ── Helpers ─────────────────────────────────────────────── */
const hashPass = (p)   => crypto.createHash('sha256').update(p).digest('hex');
const genToken = (uid) => crypto.createHash('sha256').update(uid + Date.now() + Math.random()).digest('hex');
const newUid   = ()    => 'u_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
const newPid   = ()    => 'p_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4);
const safeUser = (u)   => { const { password: _, ...rest } = u; return rest; };

/* ── lowdb safe helpers ──────────────────────────────────── */
function dbGetUsers()    { return db.get('users').value()    || []; }
function dbGetProducts() { return db.get('products').value() || []; }
function dbGetHolds()    { return db.get('holds').value()    || []; }
function dbSaveUsers(arr)    { db.set('users',    arr).write(); }
function dbSaveProducts(arr) { db.set('products', arr).write(); }
function dbSaveHolds(arr)    { db.set('holds',    arr).write(); }

/* ── Broadcast products to all connected clients ─────────── */
function broadcastProducts() {
  io.emit('products_updated', dbGetProducts());
}

/* ── Socket.IO – presence + live listings only ───────────── */
const online = new Map(); // uid → Set<socketId>

io.on('connection', (socket) => {

  socket.on('user_online', ({ uid: userUid, name, role }) => {
    socket.data = { uid: userUid, name, role };
    if (!online.has(userUid)) online.set(userUid, new Set());
    online.get(userUid).add(socket.id);
    io.emit('presence_update', { uid: userUid, online: true });
    socket.emit('online_list', [...online.keys()]);
  });

  socket.on('disconnect', () => {
    if (socket.data?.uid) {
      const uid = socket.data.uid;
      if (online.has(uid)) {
        online.get(uid).delete(socket.id);
        if (!online.get(uid).size) online.delete(uid);
      }
      io.emit('presence_update', { uid, online: online.has(uid) });
    }
  });
});

/* ══════════════════════════════════════════════════════════
   REST API
══════════════════════════════════════════════════════════ */

/* ── Health ── */
app.get('/api/ping', (_req, res) =>
  res.json({ ok: true, server: 'KrishiAI', version: '4.0' }));

/* ══ AUTH ══════════════════════════════════════════════════ */

app.post('/api/auth/signup', (req, res) => {
  const { name, email, password, phone, role, district, taluk, city, biz } = req.body;
  if (!name?.trim() || !email?.trim() || !password || !role)
    return res.status(400).json({ error: 'Name, email, password and role are required' });
  if (password.length < 6)
    return res.status(400).json({ error: 'Password must be at least 6 characters' });

  const emailLc = email.trim().toLowerCase();
  const users   = dbGetUsers();
  if (users.find(u => u.email === emailLc))
    return res.status(409).json({ error: 'Email already registered' });

  const user = {
    uid: newUid(), name: name.trim(), email: emailLc,
    password: hashPass(password),
    phone: (phone || '').trim(), role,
    district: district || '', taluk: taluk || '',
    city: city || '', biz: (biz || '').trim(),
    createdAt: Date.now(),
  };
  users.push(user);
  dbSaveUsers(users);
  res.json({ user: safeUser(user), token: genToken(user.uid) });
});

app.post('/api/auth/login', (req, res) => {
  const { email, password, role } = req.body;
  if (!email || !password)
    return res.status(400).json({ error: 'Email and password required' });

  const users = dbGetUsers();
  const user  = users.find(u =>
    u.email === email.trim().toLowerCase() && u.password === hashPass(password)
  );
  if (!user)
    return res.status(401).json({ error: 'Invalid email or password' });
  if (role && user.role !== role)
    return res.status(403).json({ error: `This account is registered as a ${user.role}. Please select the correct role.` });

  res.json({ user: safeUser(user), token: genToken(user.uid) });
});

/* ══ PRODUCTS ══════════════════════════════════════════════ */

app.get('/api/products', (_req, res) => res.json(dbGetProducts()));

app.post('/api/products', (req, res) => {
  const p = { ...req.body };
  if (!p.name || !p.farmId)
    return res.status(400).json({ error: 'name and farmId are required' });
  p.id        = newPid();
  p.createdAt = Date.now();

  const products = dbGetProducts();
  products.unshift(p);
  dbSaveProducts(products);
  broadcastProducts();
  res.json(p);
});

app.patch('/api/products/:id', (req, res) => {
  const products = dbGetProducts();
  const idx = products.findIndex(p => p.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Product not found' });
  Object.assign(products[idx], req.body);
  dbSaveProducts(products);
  broadcastProducts();
  res.json(products[idx]);
});

app.delete('/api/products/:id', (req, res) => {
  const products = dbGetProducts().filter(p => p.id !== req.params.id);
  dbSaveProducts(products);
  broadcastProducts();
  res.json({ ok: true });
});

/* ══ HOLDS ══════════════════════════════════════════════════ */

app.post('/api/products/:id/hold', (req, res) => {
  const { buyerUid: bUid, buyerName, buyerPhone, requestedQty, offeredPrice, message } = req.body;
  if (!bUid || !requestedQty)
    return res.status(400).json({ error: 'buyerUid and requestedQty are required' });

  const products = dbGetProducts();
  const idx = products.findIndex(p => p.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Product not found' });
  if (products[idx].status !== 'AVAILABLE')
    return res.status(409).json({ error: 'Product is not available' });

  const hold = {
    id: 'h_' + Date.now(), productId: req.params.id,
    productName: products[idx].name, farmId: products[idx].farmId,
    buyerUid: bUid, buyerName, buyerPhone,
    requestedQty, offeredPrice, message: message || '',
    status: 'PENDING', createdAt: Date.now(),
  };

  const holds = dbGetHolds();
  holds.push(hold);
  dbSaveHolds(holds);

  products[idx].status = 'RESERVED';
  products[idx].holdId = hold.id;
  dbSaveProducts(products);
  broadcastProducts();
  res.json(hold);
});

app.get('/api/holds', (req, res) => {
  const { farmId: fId, buyerUid: bUid } = req.query;
  const products = dbGetProducts();
  let holds = dbGetHolds();
  if (fId)  holds = holds.filter(h => h.farmId   === fId);
  if (bUid) holds = holds.filter(h => h.buyerUid === bUid);
  holds = holds.map(h => ({ ...h, product: products.find(p => p.id === h.productId) || null }));
  res.json(holds);
});

app.patch('/api/holds/:id', (req, res) => {
  const holds = dbGetHolds();
  const idx   = holds.findIndex(h => h.id === req.params.id);
  if (idx === -1) return res.status(404).json({ error: 'Hold not found' });

  const { status } = req.body;
  holds[idx].status = status;
  dbSaveHolds(holds);

  const products = dbGetProducts();
  const pidx = products.findIndex(p => p.id === holds[idx].productId);
  if (pidx !== -1) {
    products[pidx].status = status === 'ACCEPTED' ? 'SOLD' : 'AVAILABLE';
    if (status !== 'ACCEPTED') delete products[pidx].holdId;
    dbSaveProducts(products);
  }
  broadcastProducts();
  res.json(holds[idx]);
});

/* ── SPA fallback ── */
app.get('*', (req, res) => {
  if (req.path.startsWith('/api/') || req.path.startsWith('/socket.io/'))
    return res.status(404).end();
  res.sendFile(path.join(__dirname, 'index.html'));
});

/* ══════════════════════════════════════════════════════════
   START
══════════════════════════════════════════════════════════ */
server.listen(PORT, '0.0.0.0', () => {
  const nets = os.networkInterfaces();
  let localIP = 'localhost';
  for (const ifaces of Object.values(nets))
    for (const iface of ifaces)
      if (iface.family === 'IPv4' && !iface.internal) { localIP = iface.address; break; }

  console.log('\n');
  console.log('┌──────────────────────────────────────────────────────┐');
  console.log('│        🌿  KrishiAI Server  v4.0                     │');
  console.log('│        Live Marketplace · No Chat                    │');
  console.log('├──────────────────────────────────────────────────────┤');
  if (process.env.PORT) {
    console.log(`│  🌐 Cloud port ${PORT}                                  │`);
  } else {
    console.log(`│  Local:    http://localhost:${PORT}                    │`);
    console.log(`│  Network:  http://${localIP}:${PORT}               │`);
  }
  console.log('├──────────────────────────────────────────────────────┤');
  console.log(`│  Users:    ${dbGetUsers().length} registered                             │`);
  console.log(`│  Products: ${dbGetProducts().length} listed                                │`);
  console.log('└──────────────────────────────────────────────────────┘\n');
});
