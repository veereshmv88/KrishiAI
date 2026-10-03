/* ============================================================
   KRISHIAI – index.js  v3.0
   Real-Time 1-to-1 WhatsApp Chat · Server-Auth · No Demo Accounts
   ============================================================ */

/* ══════════════════════════════════════════════════════════
   1. LOCAL CACHE LAYER (products & prefs; auth is server-side)
   ══════════════════════════════════════════════════════════ */
const DB = {
  // Product listings cache
  getRawProds:  ()  => localStorage.getItem('krishi_products'),
  saveProds:    (p) => { localStorage.setItem('krishi_products', JSON.stringify(p)); },
  // Session token & user
  getSession:   ()  => localStorage.getItem('krishi_session'),
  saveSession:  (uid) => { if(uid) localStorage.setItem('krishi_session',uid); else localStorage.removeItem('krishi_session'); },
  getSessionUser: () => { try { return JSON.parse(localStorage.getItem('krishi_session_user')); } catch{return null;} },
  saveSessionUser: (u) => { if(u) localStorage.setItem('krishi_session_user',JSON.stringify(u)); else localStorage.removeItem('krishi_session_user'); },
  // Preferences
  getDark:  ()  => localStorage.getItem('krishi_dark') === 'true',
  saveDark: (v) => { localStorage.setItem('krishi_dark', v); },
  getLang:  ()  => localStorage.getItem('krishi_lang') || 'en',
  saveLang: (l) => { localStorage.setItem('krishi_lang', l); },
};

function getProds() { return JSON.parse(DB.getRawProds() || '[]'); }

/* ══════════════════════════════════════════════════════════
   2. TRANSLATIONS (English + ಕನ್ನಡ)
   ══════════════════════════════════════════════════════════ */
const TR = {
  en: {
    appName:'KrishiAI', tagline:'AI-Powered Farmer Marketplace',
    farmer:'Farmer', buyer:'Buyer',
    chooseRole:'Choose Your Role', roleSubtitle:'Select how you want to interact with the KrishiAI network',
    farmerDesc:'List crops, get AI fair price recommendations, and connect with buyers directly.',
    buyerDesc:'Browse fresh crop listings, filter by district, and contact farmers directly.',
    continueAs:'Continue as', createAccount:'Create New Account',
    welcomeBack:'Welcome Back', signIn:'Sign In', signUp:'Sign Up',
    email:'Email Address', password:'Password', forgotPass:'Forgot Password?',
    fullName:'Full Name', mobile:'Mobile Number',
    district:'Karnataka District', taluk:'Taluk', city:'City / Town',
    farmName:'Farm Name (optional)', bizName:'Business Name (optional)',
    register:'Register Account', alreadyHave:'Already have an account?',
    dontHave:"Don't have an account?",
    dashboard:'Dashboard', mycrops:'My Crops', market:'Market', profile:'Profile', settings:'Settings',
    namaskara:'Namaskara', listCrop:'List Crop Yield',
    totalCrops:'Total Crops', activeListings:'Active Listings', cropsSold:'Crops Sold', revenue:'Est. Revenue',
    aiInsights:'AI Crop Insights', myFeed:'My Crop Feed', manage:'Manage →',
    buyFresh:'Buy Fresh Crops 🛒', searchPlaceholder:'Search crop yields...',
    all:'All', vegetables:'Vegetables', fruits:'Fruits',
    aboutCrop:'About This Crop', aiBreakdown:'AI Price Breakdown', contactFarmer:'Contact Farmer',
    callFarmer:'📞 Call Farmer Directly',
    marketBase:'Market Base Rate', qualityAdj:'Quality Adjustment', freshnessAdj:'Freshness Factor',
    seasonAdj:'Seasonal Scarcity', weatherAdj:'Logistics Weather', aiTarget:'AI Target Price', confidence:'Model Confidence',
    calcAiPrice:'✨ Calculate AI Price', publishCrop:'Publish Crop →',
    cropCategory:'Crop Category', cropName:'Crop Name', quality:'Quality Grade',
    quantity:'Quantity', harvestDate:'Harvest Date', description:'Description',
    attachPhoto:'📷 Attach Crop Photo (Required)',
    updateStatus:'Update Listing Status', saveChanges:'Save Changes',
    signOut:'🚪 Sign Out', darkMode:'Dark Mode', language:'Language', notifications:'Push Notifications',
    aboutApp:'About Application', privacy:'Privacy & Data',
    resetPass:'Reset Password', sendLink:'Send Reset Link',
    premium:'Premium Grade 🏆', good:'Good Grade ⭐', average:'Average Grade',
    available:'Available', reserved:'Reserved', sold:'Sold', expired:'Expired',
    availDesc:'Listed on marketplace — buyers can see it',
    reservedDesc:'Negotiating with a buyer — temporarily held',
    soldDesc:'Deal finalized — remove from listings',
    expiredDesc:'Freshness degraded — no longer sellable',
    noListings:'No listings yet', noListingsSub:'Tap + on the dashboard to list your first crop',
    noResults:'No results found', noResultsSub:'Try different search terms or clear filters',
    errorFields:'All fields marked * are required.',
    errorEmailTaken:'This email is already registered.',
    errorInvalidCreds:'Invalid email or password.',
    errorEmailEmpty:'Please enter your email address.',
    successReset:'✓ Reset link sent! Check your inbox.',
    successPublished:'Crop published successfully!',
    sellToday:'Sell Today', waitBetter:'Wait for Better Price',
    deleteConfirm:'Remove this listing permanently?',
    callAlert:'Launching Android Dialer:\ncalling +91 ',
    // Chat
    chats:'Chats', noChats:'No conversations yet', noChatsSub:'Start a chat from a product listing',
    chatWith:'Chat with', typeMessage:'Type a message…', send:'Send',
    online:'Online', offline:'Offline', typing:'typing…',
    chatFarmer:'💬 Chat with Farmer', newMessage:'New message',
    inboxTitle:'Messages', unread:'unread',
    liveMarket:'🟢 LIVE Market', liveTip:'Listings update in real-time',
  },
  kn: {
    appName:'ಕೃಷಿAI', tagline:'AI ಚಾಲಿತ ರೈತ ಮಾರುಕಟ್ಟೆ',
    farmer:'ರೈತ', buyer:'ಖರೀದಿದಾರ',
    chooseRole:'ನಿಮ್ಮ ಪಾತ್ರ ಆರಿಸಿ', roleSubtitle:'ನೀವು ಕೃಷಿAI ನೆಟ್‌ವರ್ಕ್‌ನೊಂದಿಗೆ ಹೇಗೆ ಸಂವಹನ ಮಾಡಲು ಬಯಸುತ್ತೀರಿ ಎಂಬುದನ್ನು ಆರಿಸಿ',
    farmerDesc:'ಬೆಳೆಗಳನ್ನು ಪಟ್ಟಿ ಮಾಡಿ, AI ನ್ಯಾಯಯುತ ಬೆಲೆ ಶಿಫಾರಸುಗಳನ್ನು ಪಡೆಯಿರಿ.',
    buyerDesc:'ತಾಜಾ ಬೆಳೆ ಪಟ್ಟಿಗಳನ್ನು ನೋಡಿ, ಜಿಲ್ಲೆ ಪ್ರಕಾರ ಫಿಲ್ಟರ್ ಮಾಡಿ, ರೈತರನ್ನು ಸಂಪರ್ಕಿಸಿ.',
    continueAs:'ಮುಂದುವರಿಯಿರಿ', createAccount:'ಹೊಸ ಖಾತೆ ರಚಿಸಿ',
    welcomeBack:'ಮತ್ತೆ ಸ್ವಾಗತ', signIn:'ಸೈನ್ ಇನ್', signUp:'ನೋಂದಣಿ',
    email:'ಇಮೇಲ್ ವಿಳಾಸ', password:'ಪಾಸ್‌ವರ್ಡ್', forgotPass:'ಪಾಸ್‌ವರ್ಡ್ ಮರೆತಿದ್ದೀರಾ?',
    fullName:'ಪೂರ್ಣ ಹೆಸರು', mobile:'ಮೊಬೈಲ್ ಸಂಖ್ಯೆ',
    district:'ಕರ್ನಾಟಕ ಜಿಲ್ಲೆ', taluk:'ತಾಲ್ಲೂಕು', city:'ನಗರ / ಪಟ್ಟಣ',
    farmName:'ಫಾರ್ಮ್ ಹೆಸರು (ಐಚ್ಛಿಕ)', bizName:'ವ್ಯಾಪಾರ ಹೆಸರು (ಐಚ್ಛಿಕ)',
    register:'ಖಾತೆ ನೋಂದಾಯಿಸಿ', alreadyHave:'ಈಗಾಗಲೇ ಖಾತೆ ಇದೆಯೇ?',
    dontHave:'ಖಾತೆ ಇಲ್ಲವೇ?',
    dashboard:'ಡ್ಯಾಶ್‌ಬೋರ್ಡ್', mycrops:'ನನ್ನ ಬೆಳೆಗಳು', market:'ಮಾರುಕಟ್ಟೆ', profile:'ಪ್ರೊಫೈಲ್', settings:'ಸೆಟ್ಟಿಂಗ್ಸ್',
    namaskara:'ನಮಸ್ಕಾರ', listCrop:'ಬೆಳೆ ಪ್ರಮಾಣ ಪಟ್ಟಿ ಮಾಡಿ',
    totalCrops:'ಒಟ್ಟು ಬೆಳೆಗಳು', activeListings:'ಸಕ್ರಿಯ ಪಟ್ಟಿಗಳು', cropsSold:'ಮಾರಾಟವಾದ ಬೆಳೆಗಳು', revenue:'ಅಂದಾಜು ಆದಾಯ',
    aiInsights:'AI ಬೆಳೆ ಒಳನೋಟಗಳು', myFeed:'ನನ್ನ ಬೆಳೆ ಫೀಡ್', manage:'ನಿರ್ವಹಿಸಿ →',
    buyFresh:'ತಾಜಾ ಬೆಳೆಗಳನ್ನು ಖರೀದಿಸಿ 🛒', searchPlaceholder:'ಬೆಳೆ ಹುಡುಕಿ...',
    all:'ಎಲ್ಲ', vegetables:'ತರಕಾರಿ', fruits:'ಹಣ್ಣು',
    aboutCrop:'ಈ ಬೆಳೆಯ ಬಗ್ಗೆ', aiBreakdown:'AI ಬೆಲೆ ವಿವರ', contactFarmer:'ರೈತರನ್ನು ಸಂಪರ್ಕಿಸಿ',
    callFarmer:'📞 ರೈತರನ್ನು ನೇರವಾಗಿ ಕರೆ ಮಾಡಿ',
    marketBase:'ಮಾರುಕಟ್ಟೆ ಮೂಲ ದರ', qualityAdj:'ಗುಣಮಟ್ಟ ಹೊಂದಾಣಿಕೆ', freshnessAdj:'ತಾಜಾತನ ಅಂಶ',
    seasonAdj:'ಋತುಮಾನ ಕೊರತೆ', weatherAdj:'ಸಾರಿಗೆ ಹವಾಮಾನ', aiTarget:'AI ಶಿಫಾರಸು ಬೆಲೆ', confidence:'ಮಾದರಿ ವಿಶ್ವಾಸ',
    calcAiPrice:'✨ AI ಬೆಲೆ ಲೆಕ್ಕ ಹಾಕಿ', publishCrop:'ಬೆಳೆ ಪ್ರಕಟಿಸಿ →',
    cropCategory:'ಬೆಳೆ ವರ್ಗ', cropName:'ಬೆಳೆ ಹೆಸರು', quality:'ಗುಣಮಟ್ಟ ದರ್ಜೆ',
    quantity:'ಪ್ರಮಾಣ', harvestDate:'ಕಟಾವಿನ ದಿನಾಂಕ', description:'ವಿವರಣೆ',
    attachPhoto:'📷 ಬೆಳೆ ಫೋಟೋ ಲಗತ್ತಿಸಿ (ಅಗತ್ಯ)',
    updateStatus:'ಪಟ್ಟಿ ಸ್ಥಿತಿ ನವೀಕರಿಸಿ', saveChanges:'ಬದಲಾವಣೆಗಳನ್ನು ಉಳಿಸಿ',
    signOut:'🚪 ಸೈನ್ ಔಟ್', darkMode:'ಡಾರ್ಕ್ ಮೋಡ್', language:'ಭಾಷೆ', notifications:'ಪುಶ್ ಅಧಿಸೂಚನೆಗಳು',
    aboutApp:'ಅಪ್ಲಿಕೇಶನ್ ಬಗ್ಗೆ', privacy:'ಗೌಪ್ಯತೆ ಮತ್ತು ಡೇಟಾ',
    resetPass:'ಪಾಸ್‌ವರ್ಡ್ ಮರುಹೊಂದಿಸಿ', sendLink:'ಮರುಹೊಂದಿಸುವ ಲಿಂಕ್ ಕಳುಹಿಸಿ',
    premium:'ಪ್ರೀಮಿಯಂ ದರ್ಜೆ 🏆', good:'ಉತ್ತಮ ದರ್ಜೆ ⭐', average:'ಸಾಧಾರಣ ದರ್ಜೆ',
    available:'ಲಭ್ಯ', reserved:'ಮೀಸಲು', sold:'ಮಾರಾಟ', expired:'ಅಮಾನ್ಯ',
    availDesc:'ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಪ್ರದರ್ಶಿಸಲಾಗಿದೆ', reservedDesc:'ಖರೀದಿದಾರರೊಂದಿಗೆ ಮಾತುಕತೆ ನಡೆಯುತ್ತಿದೆ',
    soldDesc:'ಒಪ್ಪಂದ ಅಂತಿಮಗೊಳಿಸಲಾಗಿದೆ', expiredDesc:'ತಾಜಾತನ ಕಡಿಮೆಯಾಗಿದೆ',
    noListings:'ಇನ್ನೂ ಪಟ್ಟಿಗಳಿಲ್ಲ', noListingsSub:'ಮೊದಲ ಬೆಳೆ ಪಟ್ಟಿ ಮಾಡಲು + ಒತ್ತಿ',
    noResults:'ಯಾವ ಫಲಿತಾಂಶಗಳೂ ಇಲ್ಲ', noResultsSub:'ವಿಭಿನ್ನ ಹುಡುಕಾಟ ಪದಗಳನ್ನು ಪ್ರಯತ್ನಿಸಿ',
    errorFields:'* ಗುರುತಿಸಿದ ಎಲ್ಲ ಕ್ಷೇತ್ರಗಳು ಅಗತ್ಯ.',
    errorEmailTaken:'ಈ ಇಮೇಲ್ ಈಗಾಗಲೇ ನೋಂದಾಯಿಸಲಾಗಿದೆ.',
    errorInvalidCreds:'ತಪ್ಪಾದ ಇಮೇಲ್ ಅಥವಾ ಪಾಸ್‌ವರ್ಡ್.',
    errorEmailEmpty:'ದಯವಿಟ್ಟು ನಿಮ್ಮ ಇಮೇಲ್ ವಿಳಾಸ ನಮೂದಿಸಿ.',
    successReset:'✓ ಮರುಹೊಂದಿಸುವ ಲಿಂಕ್ ಕಳುಹಿಸಲಾಗಿದೆ!',
    successPublished:'ಬೆಳೆ ಯಶಸ್ವಿಯಾಗಿ ಪ್ರಕಟಿಸಲಾಗಿದೆ!',
    sellToday:'ಇಂದು ಮಾರಿ', waitBetter:'ಉತ್ತಮ ಬೆಲೆಗಾಗಿ ನಿರೀಕ್ಷಿಸಿ',
    deleteConfirm:'ಈ ಪಟ್ಟಿಯನ್ನು ಶಾಶ್ವತವಾಗಿ ತೆಗೆದುಹಾಕಬೇಕೇ?',
    callAlert:'ಆಂಡ್ರಾಯ್ಡ್ ಡಯಲರ್ ತೆರೆಯಲಾಗುತ್ತಿದೆ:\n+91 ',
    // Chat
    chats:'ಚಾಟ್‌ಗಳು', noChats:'ಇನ್ನೂ ಯಾವ ಸಂಭಾಷಣೆಗಳೂ ಇಲ್ಲ', noChatsSub:'ಉತ್ಪನ್ನ ಪಟ್ಟಿಯಿಂದ ಚಾಟ್ ಪ್ರಾರಂಭಿಸಿ',
    chatWith:'ಜೊತೆ ಚಾಟ್', typeMessage:'ಸಂದೇಶ ಟೈಪ್ ಮಾಡಿ…', send:'ಕಳುಹಿಸಿ',
    online:'ಆನ್‌ಲೈನ್', offline:'ಆಫ್‌ಲೈನ್', typing:'ಟೈಪ್ ಮಾಡುತ್ತಿದ್ದಾರೆ…',
    chatFarmer:'💬 ರೈತರೊಂದಿಗೆ ಚಾಟ್ ಮಾಡಿ', newMessage:'ಹೊಸ ಸಂದೇಶ',
    inboxTitle:'ಸಂದೇಶಗಳು', unread:'ಓದಿಲ್ಲ',
    liveMarket:'🟢 ಲೈವ್ ಮಾರುಕಟ್ಟೆ', liveTip:'ಪಟ್ಟಿಗಳು ನೈಜ-ಸಮಯದಲ್ಲಿ ನವೀಕರಣಗೊಳ್ಳುತ್ತವೆ',
  }
};
const T = (k) => TR[DB.getLang()][k] || TR['en'][k] || k;

/* ══════════════════════════════════════════════════════════
   3. KARNATAKA LOCATION DATA
   ══════════════════════════════════════════════════════════ */
const KA = {
  "Mandya":          ["Mandya","Maddur","Malavalli","Pandavapura","Srirangapatna","Nagamangala"],
  "Mysuru":          ["Mysuru","Hunsur","Nanjangud","T. Narasipura","Krishnarajanagara","Periyapatna"],
  "Belagavi":        ["Belagavi","Athani","Chikkodi","Gokak","Hukkeri","Ramdurg"],
  "Hassan":          ["Hassan","Arsikere","Belur","Channarayapatna","Holenarasipur","Sakleshpur"],
  "Tumakuru":        ["Tumakuru","Tiptur","Turuvekere","Gubbi","Pavagada","Madhugiri"],
  "Shivamogga":      ["Shivamogga","Bhadravati","Sagar","Sorab","Hosanagara","Thirthahalli"],
  "Bengaluru Rural": ["Doddaballapura","Devanahalli","Hosakote","Nelamangala","Hoskote"],
  "Dharwad":         ["Dharwad","Hubli","Kalghatgi","Kundgol","Navalgund"],
  "Raichur":         ["Raichur","Lingasugur","Manvi","Sindhanur","Devadurga"],
};

/* ══════════════════════════════════════════════════════════
   4. BASE PRICE CATALOGUE
   ══════════════════════════════════════════════════════════ */
const BASE_PRICES = [
  {name:'Tomato',   base:30, cat:'Vegetables', peak:'Monsoon'},
  {name:'Potato',   base:22, cat:'Vegetables', peak:'Winter'},
  {name:'Onion',    base:25, cat:'Vegetables', peak:'Summer'},
  {name:'Carrot',   base:45, cat:'Vegetables', peak:'Winter'},
  {name:'Brinjal',  base:28, cat:'Vegetables', peak:'Monsoon'},
  {name:'Cabbage',  base:18, cat:'Vegetables', peak:'Winter'},
  {name:'Cauliflower',base:35,cat:'Vegetables',peak:'Winter'},
  {name:'Mango',    base:80, cat:'Fruits',     peak:'Summer'},
  {name:'Banana',   base:35, cat:'Fruits',     peak:'Summer'},
  {name:'Papaya',   base:28, cat:'Fruits',     peak:'Monsoon'},
  {name:'Apple',    base:120,cat:'Fruits',     peak:'Winter'},
  {name:'Grapes',   base:90, cat:'Fruits',     peak:'Summer'},
  {name:'Pomegranate',base:70,cat:'Fruits',    peak:'Summer'},
];

/* ══════════════════════════════════════════════════════════
   5. APP STATE
   ══════════════════════════════════════════════════════════ */
const S = {
  screen: 'splash',
  role: 'Farmer',
  user: null,
  weather: { temp:28, cond:'Clear', hum:62 },
  obSlide: 0,
  tmpPhoto: null,
  tmpCalc: null,
  selProduct: null,
  selStatusId: null, _pendingStatus: null,
  catFilter: '', distFilter: '', q: '',
};

/* Restore session from saved user object */
(function restoreSession(){
  const cached = DB.getSessionUser();
  if (cached?.uid) { S.user = cached; S.role = cached.role; }
})();

/* ══════════════════════════════════════════════════════════
   SOCKET.IO CLIENT – Live Products Broadcast
   ══════════════════════════════════════════════════════════ */
let socket = null;

function initSocket() {
  try {
    socket = io();

    socket.on('connect', () => {
      updateRealtimeStatus(true);
      if (S.user) socket.emit('user_online', { uid: S.user.uid, name: S.user.name, role: S.user.role });
    });
    socket.on('disconnect',    () => updateRealtimeStatus(false));
    socket.on('connect_error', () => updateRealtimeStatus(false));

    /* Live product list — re-render whatever grid is visible */
    socket.on('products_updated', (products) => {
      DB.saveProds(products);
      const grid = $('prod-grid-inner');
      if (grid) { grid.innerHTML = buildProductGrid(); }
      else if (['buyer-dashboard','farmer-dashboard','my-products'].includes(S.screen)) navigate(S.screen);
      const liveEl = $('live-badge');
      if (liveEl) { liveEl.classList.add('pulse-once'); setTimeout(()=>liveEl.classList.remove('pulse-once'),600); }
    });

  } catch(e) {
    console.warn('Socket.IO not available:', e.message);
  }
}

function showToast(msg) {
  let toast = $('krishi-toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'krishi-toast';
    toast.className = 'krishi-toast';
    document.body.appendChild(toast);
  }
  toast.textContent = msg;
  toast.classList.add('show');
  clearTimeout(toast._t);
  toast._t = setTimeout(() => toast.classList.remove('show'), 3500);
}

/* ══════════════════════════════════════════════════════════
   6. UTILS & NAVIGATION
   ══════════════════════════════════════════════════════════ */
const $ = (id) => document.getElementById(id);
const vp = () => $('viewport');

const statusChip = (s) => {
  const map = { AVAILABLE:'chip-g', RESERVED:'chip-a', SOLD:'chip-b', EXPIRED:'chip-r' };
  const lbl = { AVAILABLE:T('available'), RESERVED:T('reserved'), SOLD:T('sold'), EXPIRED:T('expired') };
  return `<span class="chip ${map[s]||'chip-gray'}">${lbl[s]||s}</span>`;
};

const sbar = (light=false) =>
  `<div class="sbar ${light?'light':'dark'}">
    <span>${new Date().toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'})}</span>
    <div style="display:flex;gap:4px;">📶 🔋</div>
  </div>`;

const backBtn = (target, label='') =>
  `<div style="display:flex;align-items:center;gap:10px;padding:8px 14px 4px;flex-shrink:0;">
    <button class="back-btn" onclick="navigate('${target}')">‹</button>
    ${label ? `<span style="font-family:'Outfit',sans-serif;font-size:16px;font-weight:900;color:var(--t1);">${label}</span>` : ''}
  </div>`;

function applyDarkMode() {
  const isDark = DB.getDark();
  const vEl = $('viewport');
  if (vEl) vEl.dataset.theme = isDark ? 'dark' : 'light';
  // Sync console dark toggle checkbox
  const tgl = $('dark-toggle-con');
  if (tgl) tgl.checked = isDark;
}

function navigate(id) {
  S.screen = id;
  const vEl = vp(); if (!vEl) return;
  vEl.innerHTML = '';
  const wrap = document.createElement('div');
  wrap.innerHTML = renderScreen(id);
  const el = wrap.firstElementChild;
  if (el) vEl.appendChild(el);
  applyDarkMode();
}

function renderScreen(id) {
  switch(id) {
    case 'splash':           return renderSplash();
    case 'onboarding':       return renderOnboarding();
    case 'welcome':          return renderWelcome();
    case 'login':            return renderLogin();
    case 'signup':           return renderSignup();
    case 'forgot-password':  return renderForgot();
    case 'farmer-dashboard': return renderFarmerDB();
    case 'upload-crop':      return renderUpload();
    case 'my-products':      return renderMyProducts();
    case 'buyer-dashboard':  return renderBuyerDB();
    case 'product-details':  return renderDetails();
    case 'chat':             return renderChat();
    case 'chats':            return renderChats();
    case 'profile':          return renderProfile();
    case 'settings':         return renderSettings();
    case 'about':            return renderAbout();
    default: return `<div class="ms" style="padding:24px;color:var(--t3);">Screen not found.</div>`;
  }
}

/* ══════════════════════════════════════════════════════════
   7. CONSOLE CONTROLS
   ══════════════════════════════════════════════════════════ */
function setWeather(type) {
  if (type === 'Rain') {
    S.weather = { temp:21, cond:'Rainy', hum:90 };
    $('btn-clear')?.classList.remove('active');
    $('btn-rain')?.classList.add('active');
    $('w-info').textContent = '21°C · Rainy / Storm · 90% Humidity';
  } else {
    S.weather = { temp:28, cond:'Clear', hum:62 };
    $('btn-rain')?.classList.remove('active');
    $('btn-clear')?.classList.add('active');
    $('w-info').textContent = '28°C · Clear · 62% Humidity';
  }
  if (['upload-crop','farmer-dashboard','buyer-dashboard','product-details'].includes(S.screen)) navigate(S.screen);
}

function setLang(lang) {
  DB.saveLang(lang);
  document.querySelectorAll('.lang-btn').forEach(b => b.classList.remove('active'));
  $('lang-'+lang)?.classList.add('active');
  navigate(S.screen); // Re-render in new language
}

function toggleDark() {
  const isDark = !DB.getDark();
  DB.saveDark(isDark);
  applyDarkMode();
  // Re-render current screen so in-screen dark toggles also reflect the change
  navigate(S.screen);
}

/* ══════════════════════════════════════════════════════════
   8. AI PRICE CALCULATOR
   ══════════════════════════════════════════════════════════ */
function calcAiPrice(cropName, quality, harvestDateStr) {
  const entry = BASE_PRICES.find(p => p.name === cropName) || { base:25, peak:'Summer' };
  const base = entry.base;
  let qAdj = quality==='Premium' ? base*0.15 : quality==='Good' ? base*0.05 : base*-0.10;
  const daysSince = Math.max(0, Math.floor((Date.now()-new Date(harvestDateStr).getTime())/864e5));
  let fAdj = daysSince<=2 ? base*0.05 : daysSince>5 ? base*-0.15 : 0;
  const m = new Date().getMonth();
  const cur = m>=2&&m<=5?'Summer': m>=6&&m<=9?'Monsoon':'Winter';
  let sAdj = entry.peak===cur ? base*-0.05 : base*0.15;
  let wAdj = S.weather.cond==='Rainy' ? base*0.10 : 0;
  const final = base + qAdj + fAdj + sAdj + wAdj;
  const conf = daysSince<=2 ? 95 : daysSince<=5 ? 90 : 82;
  const rec = daysSince>5
    ? `⚠️ ${T('sellToday')} — Freshness declining, price discounted`
    : `✅ Promote Listing — High demand conditions active`;
  return { base, qAdj, fAdj, sAdj, wAdj, final, conf, rec };
}

/* ══════════════════════════════════════════════════════════
   9. AUTH FUNCTIONS
   ══════════════════════════════════════════════════════════ */
function doLogin() {
  const email = $('l-email')?.value?.trim().toLowerCase();
  const pass  = $('l-pass')?.value?.trim();
  const err   = $('l-err');
  const btn   = $('l-btn');
  if (!email || !pass) { err.textContent = T('errorFields'); return; }
  err.textContent = '';
  if (btn) { btn.disabled = true; btn.textContent = '...'; }
  fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password: pass, role: S.role }),
  })
  .then(r => r.json())
  .then(data => {
    if (btn) { btn.disabled = false; btn.textContent = T('signIn'); }
    if (data.error) { err.textContent = data.error; return; }
    S.user = data.user;
    DB.saveSession(data.user.uid);
    DB.saveSessionUser(data.user);
    // Register presence on socket
    if (socket?.connected) socket.emit('user_online', { uid: data.user.uid, name: data.user.name, role: data.user.role });
    navigate(data.user.role === 'Farmer' ? 'farmer-dashboard' : 'buyer-dashboard');
  })
  .catch(() => {
    if (btn) { btn.disabled = false; btn.textContent = T('signIn'); }
    err.textContent = 'Server unreachable. Please try again.';
  });
}

function doSignup() {
  const name  = $('s-name')?.value?.trim();
  const phone = $('s-phone')?.value?.trim();
  const email = $('s-email')?.value?.trim().toLowerCase();
  const pass  = $('s-pass')?.value?.trim();
  const dist  = $('s-dist')?.value;
  const err   = $('s-err');
  const btn   = $('s-btn');
  if (!name||!phone||!email||!pass||!dist) { err.textContent = T('errorFields'); return; }
  if (pass.length < 6) { err.textContent = 'Password must be at least 6 characters.'; return; }
  err.textContent = '';
  if (btn) { btn.disabled = true; btn.textContent = '...'; }
  fetch('/api/auth/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name, email, password: pass, phone, role: S.role,
      district: dist,
      taluk:  S.role==='Farmer' ? ($('s-taluk')?.value||'') : '',
      city:   S.role==='Buyer'  ? ($('s-city')?.value||'')  : '',
      biz:    $('s-biz')?.value?.trim() || '',
    }),
  })
  .then(r => r.json())
  .then(data => {
    if (btn) { btn.disabled = false; btn.textContent = T('signUp'); }
    if (data.error) { err.textContent = data.error; return; }
    S.user = data.user;
    DB.saveSession(data.user.uid);
    DB.saveSessionUser(data.user);
    if (socket?.connected) socket.emit('user_online', { uid: data.user.uid, name: data.user.name, role: data.user.role });
    navigate(data.user.role === 'Farmer' ? 'farmer-dashboard' : 'buyer-dashboard');
  })
  .catch(() => {
    if (btn) { btn.disabled = false; btn.textContent = T('signUp'); }
    err.textContent = 'Server unreachable. Please try again.';
  });
}

function doForgot() {
  const email = $('f-email')?.value?.trim();
  const msgEl = $('f-msg');
  if (!email) { msgEl.className='msg-err'; msgEl.textContent=T('errorEmailEmpty'); return; }
  msgEl.className = 'msg-ok';
  msgEl.textContent = 'If this email is registered, you will receive a password reset link.';
}

function doLogout() {
  S.user = null;
  S.chatMessages = {};
  S.chatUnread   = {};
  S.totalUnread  = 0;
  S.inboxConversations = [];
  S.activeChatProductId = null;
  S.activeChatBuyerUid  = null;
  DB.saveSession(null);
  DB.saveSessionUser(null);
  navigate('welcome');
}

/* ══════════════════════════════════════════════════════════
   10. PRODUCT FUNCTIONS
   ══════════════════════════════════════════════════════════ */
function fillTaluks(dist, selId) {
  const el = $(selId); if (!el) return;
  el.innerHTML = (KA[dist]||[]).map(t=>`<option value="${t}">${t}</option>`).join('');
}

function pickPhoto() {
  S.tmpPhoto = 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&auto=format&fit=crop';
  const el = $('photo-pick');
  if (el) el.innerHTML = `<img src="${S.tmpPhoto}" alt="crop"/>`;
}

function updateCropNames(cat) {
  const sel = $('u-name'); if (!sel) return;
  sel.innerHTML = BASE_PRICES.filter(p=>p.cat===cat).map(p=>`<option>${p.name}</option>`).join('');
}

function doCalcPrice() {
  const name=$('u-name')?.value, cat=$('u-cat')?.value, quality=$('u-quality')?.value,
        qty=$('u-qty')?.value?.trim(), date=$('u-date')?.value,
        dist=$('u-dist')?.value, taluk=$('u-taluk')?.value, desc=$('u-desc')?.value?.trim();
  const err = $('u-err');
  if (!name||!qty||!date||!dist||!taluk||!desc||!S.tmpPhoto) {
    err.textContent = T('errorFields') + ' Also attach a crop photo.'; return;
  }
  err.textContent = '';
  const c = calcAiPrice(name, quality, date);
  S.tmpCalc = { cat, name, quality, qty, harvestMs:new Date(date).getTime(), desc, district:dist, taluk, ...c };
  const wIcon = S.weather.cond==='Rainy' ? '☁️' : '☀️';
  $('ai-content').innerHTML = `
    <div class="w-banner">${wIcon} ${S.weather.temp}°C · ${S.weather.cond} · ${S.weather.hum}% · ${dist}</div>
    <div class="breakdown">
      <div class="brow"><span>${T('marketBase')}:</span><strong>₹${c.base}/kg</strong></div>
      <div class="brow"><span>${T('qualityAdj')} (${quality}):</span><strong style="color:${c.qAdj>=0?'#2e7d32':'#ef4444'}">₹${c.qAdj>=0?'+':''}${c.qAdj.toFixed(1)}</strong></div>
      <div class="brow"><span>${T('freshnessAdj')}:</span><strong style="color:${c.fAdj>=0?'#2e7d32':'#ef4444'}">₹${c.fAdj>=0?'+':''}${c.fAdj.toFixed(1)}</strong></div>
      <div class="brow"><span>${T('seasonAdj')}:</span><strong style="color:${c.sAdj>=0?'#2e7d32':'#ef4444'}">₹${c.sAdj>=0?'+':''}${c.sAdj.toFixed(1)}</strong></div>
      <div class="brow"><span>${T('weatherAdj')}:</span><strong style="color:${c.wAdj>0?'#b45309':'var(--t2)'}">₹+${c.wAdj.toFixed(1)}</strong></div>
      <div class="brow total"><span>${T('aiTarget')}</span><strong>₹${c.final.toFixed(1)}/kg</strong></div>
    </div>
    <div class="conf-wrap">
      <div class="conf-lbl"><span>${T('confidence')}</span><span>${c.conf}%</span></div>
      <div class="conf-bar"><div class="conf-fill" style="width:${c.conf}%;"></div></div>
    </div>
    <div class="rec-box">${c.rec}</div>
    <div class="inp-group">
      <label class="inp-label">Set Your Selling Price (₹/kg)</label>
      <input id="u-final-price" class="inp-field" type="number" value="${Math.round(c.final)}" min="1">
    </div>`;
  $('ai-modal').style.display = 'flex';
}

function closeAiModal() { $('ai-modal').style.display = 'none'; }

function publishListing() {
  const fp = parseFloat($('u-final-price')?.value) || S.tmpCalc.final;
  const c = S.tmpCalc;
  const newProd = {
    farmId: S.user?.uid || 'unknown',
    farmerName: S.user?.name || 'Farmer',
    farmerPhone: S.user?.phone || '0000000000',
    cat:c.cat, name:c.name, qty:c.qty, quality:c.quality, harvestMs:c.harvestMs,
    desc:c.desc, district:c.district, taluk:c.taluk,
    img: S.tmpPhoto,
    base:c.base, aiPrice:c.final, price:fp, status:'AVAILABLE',
    ai:{ base:c.base,qAdj:c.qAdj,fAdj:c.fAdj,sAdj:c.sAdj,wAdj:c.wAdj,final:c.final,conf:c.conf,rec:c.rec }
  };
  fetch('/api/products', {
    method: 'POST',
    headers: {'Content-Type':'application/json'},
    body: JSON.stringify(newProd)
  })
  .then(r => r.json())
  .then(saved => {
    // Server will broadcast via socket — update local cache too
    const prods = getProds();
    prods.unshift(saved);
    DB.saveProds(prods);
  })
  .catch(() => {
    // Offline fallback
    const prods = getProds();
    newProd.id = 'p_' + Date.now();
    prods.unshift(newProd);
    DB.saveProds(prods);
  })
  .finally(() => {
    S.tmpPhoto = null; S.tmpCalc = null;
    navigate('farmer-dashboard');
  });
}

const STATUS_META = {
  AVAILABLE:{ dot:'#4caf50', title:'Available',  desc:T('availDesc') },
  RESERVED: { dot:'#f59e0b', title:'Reserved',   desc:T('reservedDesc') },
  SOLD:     { dot:'#3b82f6', title:'Sold',        desc:T('soldDesc') },
  EXPIRED:  { dot:'#ef4444', title:'Expired',    desc:T('expiredDesc') },
};

function openStatusModal(id) {
  S.selStatusId = id;
  S._pendingStatus = null;
  const prods = getProds();
  const prod = prods.find(p => p.id === id);
  const optsHtml = Object.entries(STATUS_META).map(([s,m])=>
    `<div class="status-opt ${prod?.status===s?'sel':''}" onclick="pickStatus('${s}',this)">
      <div class="status-dot" style="background:${m.dot};"></div>
      <div class="status-opt-txt"><h5>${m.title}</h5><p>${m.desc}</p></div>
    </div>`).join('');
  $('status-opts').innerHTML = optsHtml;
  $('status-modal').style.display = 'flex';
}

function pickStatus(s, el) {
  S._pendingStatus = s;
  document.querySelectorAll('.status-opt').forEach(e => e.classList.remove('sel'));
  el.classList.add('sel');
}
function closeStatusModal() { $('status-modal').style.display = 'none'; }
function saveStatus() {
  if (!S._pendingStatus) { closeStatusModal(); return; }
  const id  = S.selStatusId;
  const st  = S._pendingStatus;
  // Update server
  fetch(`/api/products/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: st }),
  }).catch(() => {});
  // Update local cache immediately
  const prods = getProds();
  const prod  = prods.find(p => p.id === id);
  if (prod) prod.status = st;
  DB.saveProds(prods);
  closeStatusModal();
  navigate('my-products');
}
function deleteProduct(id) {
  if (confirm(T('deleteConfirm'))) {
    fetch(`/api/products/${id}`, { method: 'DELETE' }).catch(() => {});
    DB.saveProds(getProds().filter(p => p.id !== id));
    navigate('my-products');
  }
}

function doSearch(v) {
  S.q = v;
  // Re-render grid only
  const grid = $('prod-grid-inner');
  if (!grid) return;
  grid.innerHTML = buildProductGrid();
}
function filterCat(c) { S.catFilter = c; navigate('buyer-dashboard'); }
function filterDist(d) { S.distFilter = d; navigate('buyer-dashboard'); }
function openDetails(id) {
  S.selProduct = getProds().find(p => p.id === id);
  navigate('product-details');
}
function callFarmer(phone) { alert(`${T('callAlert')}${phone}...`); }

/* ══════════════════════════════════════════════════════════
   11. CROSS-TAB LIVE SYNC
      Simulates Firebase Firestore real-time listeners:
      when localStorage changes in another tab, re-render
   ══════════════════════════════════════════════════════════ */
window.addEventListener('storage', (e) => {
  if (e.key === 'krishi_products') {
    // Another "device" (tab) updated product data — refresh if on a relevant screen
    if (['farmer-dashboard','buyer-dashboard','my-products','product-details'].includes(S.screen)) {
      navigate(S.screen);
      // Show a brief sync indicator
      const syncBadge = $('sync-status');
      if (syncBadge) {
        syncBadge.textContent = '🔄 Data synced from another device!';
        setTimeout(() => { if ($('sync-status')) $('sync-status').textContent = '🟢 Live Sync Active'; }, 2500);
      }
    }
  }
});

/* ══════════════════════════════════════════════════════════
   12. SCREEN RENDERERS
   ══════════════════════════════════════════════════════════ */

/* ── SPLASH ──────────────────────────────────────────────── */
function renderSplash() {
  setTimeout(() => navigate('onboarding'), 2200);
  return `<div class="ms splash-ms">
    ${sbar(true)}
    <div class="splash-logo">
      <div class="splash-icon">🌿</div>
      <h1>${T('appName')}</h1>
      <p>${T('tagline')}</p>
    </div>
    <div class="spinner"></div>
  </div>`;
}

/* ── ONBOARDING ──────────────────────────────────────────── */
const OB = [
  { icon:'🌾', bg:'rgba(46,125,50,.12)', title:'Direct Farmer Marketplace',
    desc:'Skip middlemen. List Karnataka crop yields directly and connect with verified commercial buyers for fair pricing.' },
  { icon:'📊', bg:'rgba(59,130,246,.12)', title:'Explainable AI Fair Price',
    desc:'Get pricing suggestions driven by live weather, freshness, crop quality, and Karnataka district market baselines.' },
  { icon:'📞', bg:'rgba(245,158,11,.12)', title:'Secure Farmer Contact',
    desc:'Review transparent price breakdowns, click to dial farmers directly, and finalize deals with full confidence.' },
];
function renderOnboarding() {
  const sl = OB[S.obSlide];
  const dots = OB.map((_,i)=>`<div class="dot ${i===S.obSlide?'on':''}"></div>`).join('');
  return `<div class="ms" style="background:var(--s0);">
    ${sbar()}
    <div class="ob-body">
      <div class="ob-icon" style="background:${sl.bg};">${sl.icon}</div>
      <h2 class="ob-title">${sl.title}</h2>
      <p class="ob-desc">${sl.desc}</p>
    </div>
    <div class="ob-footer">
      <div class="dots">${dots}</div>
      <button class="btn-p" onclick="nextOb()">${S.obSlide<OB.length-1?'Next →':'Get Started 🚀'}</button>
      ${S.obSlide===0?`<button class="btn-o" onclick="navigate('welcome')" style="height:42px;font-size:13px;">Skip</button>`:''}
    </div>
  </div>`;
}
function nextOb() {
  if (S.obSlide<OB.length-1) { S.obSlide++; navigate('onboarding'); }
  else { S.obSlide=0; navigate('welcome'); }
}

/* ── WELCOME / ROLE SELECT ───────────────────────────────── */
function renderWelcome() {
  const rCard = (r, icon, desc) =>
    `<div class="role-card ${S.role===r?'sel':''}" onclick="pickRole('${r}')">
      <div class="role-card-ico">${icon}</div>
      <div class="role-card-txt"><h4>${T(r.toLowerCase())}</h4><p>${desc}</p></div>
      <div class="role-radio"><div class="role-radio-dot" style="opacity:${S.role===r?1:0};"></div></div>
    </div>`;
  return `<div class="ms welcome-ms">
    ${sbar()}
    <div class="welcome-head">
      <div class="w-brand">🌾</div>
      <h2>${T('chooseRole')}</h2>
      <p>${T('roleSubtitle')}</p>
    </div>
    <div class="role-cards">
      ${rCard('Farmer','🚜',T('farmerDesc'))}
      ${rCard('Buyer','🏪',T('buyerDesc'))}
    </div>
    <button class="btn-p" onclick="navigate('login')">${T('continueAs')} ${T(S.role.toLowerCase())} →</button>
    <div style="height:12px;"></div>
    <button class="btn-o" onclick="navigate('signup')" style="height:44px;font-size:13px;">${T('createAccount')}</button>
  </div>`;
}
function pickRole(r) { S.role = r; navigate('welcome'); }

/* ── LOGIN ───────────────────────────────────────────────── */
function renderLogin() {
  return `<div class="ms auth-ms">
    ${sbar()}
    <div class="auth-inner">
      ${backBtn('welcome')}
      <div class="auth-brand"><div class="auth-brand-ico">🌿</div><span class="auth-brand-name">${T('appName')}</span></div>
      <div class="role-badge">${T(S.role.toLowerCase())}</div>
      <h2 class="auth-title">${T('welcomeBack')}</h2>
      <p class="auth-sub">Sign in to your ${T(S.role.toLowerCase())} account</p>
      <div class="auth-form">
        <div class="inp-group">
          <label class="inp-label">${T('email')}</label>
          <input id="l-email" class="inp-field" type="email" placeholder="you@example.com" autocomplete="email">
        </div>
        <div class="inp-group">
          <label class="inp-label">${T('password')}</label>
          <input id="l-pass" class="inp-field" type="password" placeholder="••••••••" autocomplete="current-password">
        </div>
        <div class="forgot" onclick="navigate('forgot-password')">${T('forgotPass')}</div>
        <div id="l-err" class="msg-err"></div>
        <button id="l-btn" class="btn-p" onclick="doLogin()">${T('signIn')}</button>
      </div>
      <p class="auth-footer-txt">${T('dontHave')} <span onclick="navigate('signup')">${T('signUp')}</span></p>
    </div>
  </div>`;
}

/* ── SIGNUP ──────────────────────────────────────────────── */
function renderSignup() {
  const distOpts = Object.keys(KA).map(d=>`<option value="${d}">${d}</option>`).join('');
  return `<div class="ms auth-ms">
    ${sbar()}
    <div class="auth-inner">
      ${backBtn('login')}
      <div class="auth-brand"><div class="auth-brand-ico">🌿</div><span class="auth-brand-name">${T('appName')}</span></div>
      <div class="role-badge">${T(S.role.toLowerCase())}</div>
      <h2 class="auth-title">${T('signUp')}</h2>
      <p class="auth-sub">Register as a ${T(S.role.toLowerCase())} in the KrishiAI network</p>
      <div class="auth-form">
        <div class="avatar-pick" onclick="this.innerHTML='<img src=&quot;https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&auto=format&fit=crop&q=80&quot;/>'" title="Tap to upload photo">
          <span style="font-size:24px;">📷</span><span>Photo</span>
        </div>
        <div class="row2">
          <div class="inp-group"><label class="inp-label">${T('fullName')} *</label><input id="s-name" class="inp-field" placeholder="Gowda Basappa"></div>
          <div class="inp-group"><label class="inp-label">${T('mobile')} *</label><input id="s-phone" class="inp-field" type="tel" placeholder="9876543210"></div>
        </div>
        <div class="inp-group"><label class="inp-label">${T('email')} *</label><input id="s-email" class="inp-field" type="email" placeholder="gowda@gmail.com"></div>
        <div class="inp-group"><label class="inp-label">${T('password')} * (min 6 chars)</label><input id="s-pass" class="inp-field" type="password" placeholder="••••••••"></div>
        <div class="inp-group">
          <label class="inp-label">${T('district')} *</label>
          <select id="s-dist" class="inp-field" onchange="fillTaluks(this.value,'s-taluk')">
            <option value="">Choose District</option>${distOpts}
          </select>
        </div>
        ${S.role==='Farmer'
          ? `<div class="inp-group"><label class="inp-label">${T('taluk')} *</label>
             <select id="s-taluk" class="inp-field"><option>Select District First</option></select></div>
             <div class="inp-group"><label class="inp-label">${T('farmName')}</label>
             <input id="s-biz" class="inp-field" placeholder="e.g. Gowda Organic Farms"></div>`
          : `<div class="inp-group"><label class="inp-label">${T('city')} *</label>
             <input id="s-city" class="inp-field" placeholder="e.g. Mysuru City"></div>
             <div class="inp-group"><label class="inp-label">${T('bizName')}</label>
             <input id="s-biz" class="inp-field" placeholder="e.g. Mandya Fresh Mart"></div>`
        }
        <div id="s-err" class="msg-err"></div>
        <button id="s-btn" class="btn-p" onclick="doSignup()">${T('register')} 🎉</button>
      </div>
      <p class="auth-footer-txt">${T('alreadyHave')} <span onclick="navigate('login')">${T('signIn')}</span></p>
    </div>
  </div>`;
}

/* ── FORGOT PASSWORD ─────────────────────────────────────── */
function renderForgot() {
  return `<div class="ms auth-ms">
    ${sbar()}
    <div class="auth-inner">
      ${backBtn('login')}
      <div class="auth-brand"><div class="auth-brand-ico">🔒</div><span class="auth-brand-name">${T('appName')}</span></div>
      <h2 class="auth-title">${T('resetPass')}</h2>
      <p class="auth-sub">We'll send a recovery link to your registered email</p>
      <div class="auth-form">
        <div class="inp-group"><label class="inp-label">${T('email')}</label><input id="f-email" class="inp-field" type="email" placeholder="you@example.com"></div>
        <div id="f-msg"></div>
        <button class="btn-p" onclick="doForgot()">${T('sendLink')}</button>
      </div>
    </div>
  </div>`;
}

/* ── FARMER DASHBOARD ────────────────────────────────────── */
function renderFarmerDB() {
  const allProds = getProds();
  const mine = allProds.filter(p => p.farmId === (S.user?.uid || 'demo_farmer'));
  const avail = mine.filter(p=>p.status==='AVAILABLE').length;
  const sold  = mine.filter(p=>p.status==='SOLD').length;
  const rev   = mine.filter(p=>p.status==='SOLD').reduce((a,p)=>a+(parseFloat(p.qty)*p.price||0),0);
  const warnTxt = S.weather.cond==='Rainy'
    ? '🌧 Rainy conditions detected — +10% logistics cost applied. Store crops in dry sheds.'
    : '🌤 Stable weather — Standard pricing active. Ideal dispatch conditions.';
  const feedHtml = mine.length
    ? mine.map(p=>`<div class="feed-item" onclick="navigate('my-products')">
        <div class="feed-img"><img src="${p.img}" alt="${p.name}" loading="lazy"/></div>
        <div class="feed-info"><h4>${p.name}</h4><p>${p.qty} · ${p.quality} · ${p.district}</p></div>
        <div class="feed-right"><span class="feed-price">₹${p.price}/kg</span>${statusChip(p.status)}</div>
      </div>`).join('')
    : `<div class="empty-state"><div class="empty-icon">🌱</div><h4>${T('noListings')}</h4><p>${T('noListingsSub')}</p></div>`;
  const unreadBadge = S.totalUnread > 0 ? `<span class="chat-nav-badge" style="display:flex">${S.totalUnread}</span>` : `<span class="chat-nav-badge" style="display:none"></span>`;
  return `<div class="ms farmer-db-ms">
    ${sbar()}
    <div class="app-bar">
      <span class="app-bar-title">${T('appName')}</span>
      <button class="icon-btn" onclick="navigate('chats')" style="position:relative;">💬${unreadBadge}</button>
      <button class="icon-btn" onclick="navigate('settings')">⚙</button>
      <button class="icon-btn" onclick="navigate('profile')">👤</button>
    </div>
    <div class="ms-body">
      <div class="welcome-banner">
        <div class="wb-avatar">👨‍🌾</div>
        <div>
          <div class="wb-name">${T('namaskara')}, ${S.user?.name||'Farmer'}!</div>
          <div class="wb-loc">📍 ${S.user?.taluk||'Maddur'}, ${S.user?.district||'Mandya'}</div>
        </div>
      </div>
      <div class="stats-grid">
        <div class="stat-card"><span class="stat-lbl">${T('totalCrops')}</span><span class="stat-val" style="color:#2e7d32">${mine.length}</span><div class="stat-sub">Active + Sold</div></div>
        <div class="stat-card"><span class="stat-lbl">${T('activeListings')}</span><span class="stat-val" style="color:#4caf50">${avail}</span><div class="stat-sub">On marketplace</div></div>
        <div class="stat-card"><span class="stat-lbl">${T('cropsSold')}</span><span class="stat-val" style="color:#f59e0b">${sold}</span><div class="stat-sub">Finalized deals</div></div>
        <div class="stat-card"><span class="stat-lbl">${T('revenue')}</span><span class="stat-val" style="color:#2e7d32">₹${rev>0?rev.toLocaleString('en-IN'):0}</span><div class="stat-sub">From sold crops</div></div>
      </div>
      <div class="insight-card">
        <div class="insight-head">✨ ${T('aiInsights')}</div>
        <div class="insight-item">${warnTxt}</div>
        <div class="insight-item">💡 <b>Freshness Bonus:</b> Listings within 48h of harvest earn a +5% premium. Keep harvest dates updated!</div>
      </div>
      ${S.totalUnread > 0 ? `<div class="chat-alert-banner" onclick="navigate('chats')">💬 <b>${S.totalUnread} new message${S.totalUnread>1?'s':''}</b> — tap to reply</div>` : ''}
      <div class="sec-head"><h4>${T('myFeed')}</h4><span onclick="navigate('my-products')">${T('manage')}</span></div>
      <div class="feed-list">${feedHtml}</div>
      <div style="height:80px;"></div>
    </div>
    <button class="fab" onclick="navigate('upload-crop')" title="${T('listCrop')}">＋</button>
    <nav class="bnav">
      <div class="bnav-item on">🏠<span>${T('dashboard')}</span></div>
      <div class="bnav-item" onclick="navigate('my-products')">📋<span>${T('mycrops')}</span></div>
      <div class="bnav-item" style="position:relative;" onclick="navigate('chats')">💬<span>${T('chats')}</span>${unreadBadge}</div>
      <div class="bnav-item" onclick="navigate('profile')">👤<span>${T('profile')}</span></div>
    </nav>
  </div>`;
}

/* ── UPLOAD CROP ─────────────────────────────────────────── */
function renderUpload() {
  const distOpts = Object.keys(KA).map(d=>`<option value="${d}" ${d===(S.user?.district||'Mandya')?'selected':''}>${d}</option>`).join('');
  const today = new Date().toISOString().substring(0,10);
  const cropOpts = BASE_PRICES.filter(p=>p.cat==='Vegetables').map(p=>`<option>${p.name}</option>`).join('');
  return `<div class="ms upload-ms">
    ${sbar()}
    ${backBtn('farmer-dashboard', T('listCrop'))}
    <div class="upload-body">
      <div class="photo-pick" id="photo-pick" onclick="pickPhoto()">
        <span style="font-size:28px;">📷</span>
        <p>${T('attachPhoto')}</p>
      </div>
      <div class="inp-group"><label class="inp-label">${T('cropCategory')} *</label>
        <select id="u-cat" class="inp-field" onchange="updateCropNames(this.value)">
          <option value="Vegetables">${T('vegetables')}</option>
          <option value="Fruits">${T('fruits')}</option>
        </select></div>
      <div class="inp-group"><label class="inp-label">${T('cropName')} *</label><select id="u-name" class="inp-field">${cropOpts}</select></div>
      <div class="inp-group"><label class="inp-label">${T('quality')} *</label>
        <select id="u-quality" class="inp-field">
          <option value="Premium">${T('premium')}</option>
          <option value="Good" selected>${T('good')}</option>
          <option value="Average">${T('average')}</option>
        </select></div>
      <div class="row2">
        <div class="inp-group"><label class="inp-label">${T('quantity')} *</label><input id="u-qty" class="inp-field" placeholder="e.g. 500 kg"></div>
        <div class="inp-group"><label class="inp-label">${T('harvestDate')} *</label><input id="u-date" class="inp-field" type="date" value="${today}"></div>
      </div>
      <div class="row2">
        <div class="inp-group"><label class="inp-label">${T('district')} *</label>
          <select id="u-dist" class="inp-field" onchange="fillTaluks(this.value,'u-taluk')">${distOpts}</select></div>
        <div class="inp-group"><label class="inp-label">${T('taluk')} *</label>
          <select id="u-taluk" class="inp-field">
            ${(KA[S.user?.district||'Mandya']||[]).map(t=>`<option ${t===(S.user?.taluk||'Maddur')?'selected':''}>${t}</option>`).join('')}
          </select></div>
      </div>
      <div class="inp-group"><label class="inp-label">${T('description')} *</label><textarea id="u-desc" class="inp-field" placeholder="Describe the crop, farming method, quality notes..."></textarea></div>
      <div id="u-err" class="msg-err"></div>
      <button class="btn-p" onclick="doCalcPrice()">${T('calcAiPrice')}</button>
    </div>
    <div id="ai-modal" class="modal-bg" style="display:none;">
      <div class="modal-sheet">
        <div class="modal-handle"></div>
        <div class="modal-title">✨ AI Price Analysis</div>
        <div class="modal-sub">Based on quality, freshness, season &amp; current weather conditions</div>
        <div id="ai-content"></div>
        <div class="modal-row">
          <button class="btn-o" onclick="closeAiModal()">Cancel</button>
          <button class="btn-p" onclick="publishListing()">${T('publishCrop')}</button>
        </div>
      </div>
    </div>
  </div>`;
}

/* ── MY PRODUCTS ─────────────────────────────────────────── */
function renderMyProducts() {
  const mine = getProds().filter(p => p.farmId === (S.user?.uid || 'demo_farmer'));
  const itemsHtml = mine.length
    ? mine.map(p=>`<div class="prod-item">
        <div class="prod-img"><img src="${p.img}" alt="${p.name}" loading="lazy"/></div>
        <div class="prod-info"><h4>${p.name}</h4><p>${p.qty} · ${p.district}</p>${statusChip(p.status)}</div>
        <div class="prod-actions">
          <button class="btn-xs btn-xs-g" onclick="openStatusModal('${p.id}')">${T('updateStatus').split(' ')[0]}</button>
          <button class="btn-xs btn-xs-r" onclick="deleteProduct('${p.id}')">🗑</button>
        </div>
      </div>`).join('')
    : `<div class="empty-state"><div class="empty-icon">📦</div><h4>${T('noListings')}</h4><p>${T('noListingsSub')}</p></div>`;
  return `<div class="ms farmer-db-ms">
    ${sbar()}
    ${backBtn('farmer-dashboard', T('mycrops'))}
    <div class="ms-body" style="padding:8px 0;">
      <div class="prod-list">${itemsHtml}</div>
      <div style="height:80px;"></div>
    </div>
    <div id="status-modal" class="modal-bg" style="display:none;">
      <div class="modal-sheet">
        <div class="modal-handle"></div>
        <div class="modal-title">${T('updateStatus')}</div>
        <div class="modal-sub">Choose the current state of this crop listing</div>
        <div id="status-opts"></div>
        <div class="modal-row">
          <button class="btn-o" onclick="closeStatusModal()">Cancel</button>
          <button class="btn-p" onclick="saveStatus()">${T('saveChanges')}</button>
        </div>
      </div>
    </div>
    <nav class="bnav">
      <div class="bnav-item" onclick="navigate('farmer-dashboard')">🏠<span>${T('dashboard')}</span></div>
      <div class="bnav-item on">📋<span>${T('mycrops')}</span></div>
      <div class="bnav-item" onclick="navigate('profile')">👤<span>${T('profile')}</span></div>
    </nav>
  </div>`;
}

/* ── BUYER DASHBOARD ─────────────────────────────────────── */
function buildProductGrid() {
  const avail = getProds().filter(p => {
    const m  = !S.q || p.name.toLowerCase().includes(S.q.toLowerCase()) || p.district.toLowerCase().includes(S.q.toLowerCase());
    const c  = !S.catFilter  || p.cat === S.catFilter;
    const d  = !S.distFilter || p.district === S.distFilter;
    return p.status==='AVAILABLE' && m && c && d;
  });
  if (!avail.length)
    return `<div class="empty-state" style="grid-column:span 2;"><div class="empty-icon">🔍</div><h4>${T('noResults')}</h4><p>${T('noResultsSub')}</p></div>`;
  return avail.map(p=>`<div class="prod-card" onclick="openDetails('${p.id}')">
    <div class="prod-card-img"><img src="${p.img}" alt="${p.name}" loading="lazy"/>
      ${p.price<=p.aiPrice?`<div class="ai-badge">AI PICK</div>`:''}
    </div>
    <div class="prod-card-body">
      <h4>${p.name}</h4>
      <div class="prod-card-loc">📍 ${p.district}</div>
      <div class="prod-card-foot">
        <span class="prod-card-price">₹${p.price}<sub>/kg</sub></span>
        ${statusChip(p.quality)}
      </div>
    </div>
  </div>`).join('');
}
function renderBuyerDB() {
  const distOpts = `<option value="">All Districts</option>`+Object.keys(KA).map(d=>`<option value="${d}" ${d===S.distFilter?'selected':''}>${d}</option>`).join('');
  const cats = ['','Vegetables','Fruits'];
  const unreadBadge = S.totalUnread > 0 ? `<span class="chat-nav-badge" style="display:flex">${S.totalUnread}</span>` : `<span class="chat-nav-badge" style="display:none"></span>`;
  return `<div class="ms buyer-db-ms">
    ${sbar()}
    <div class="app-bar">
      <span class="app-bar-title">${T('appName')} Market</span>
      <button class="icon-btn" onclick="navigate('chats')" style="position:relative;">💬${unreadBadge}</button>
      <button class="icon-btn" onclick="navigate('settings')">⚙</button>
    </div>
    <div class="ms-body">
      <div class="buyer-banner">
        <div style="display:flex;align-items:center;justify-content:space-between;">
          <p>Welcome, ${S.user?.name||'Buyer'}</p>
          <div id="live-badge" class="live-badge">
            <span class="live-dot"></span>${T('liveMarket')}
          </div>
        </div>
        <h2>${T('buyFresh')}</h2>
        <div class="live-tip">${T('liveTip')}</div>
      </div>
      <div class="search-bar">
        <span class="si">🔍</span>
        <input id="b-search" placeholder="${T('searchPlaceholder')}" value="${S.q}" oninput="doSearch(this.value)">
      </div>
      <div class="filter-row">
        ${cats.map(c=>`<div class="fpill ${S.catFilter===c?'on':''}" onclick="filterCat('${c}')">${c||T('all')}</div>`).join('')}
        <select class="fpill" onchange="filterDist(this.value)">${distOpts}</select>
      </div>
      <div class="prod-grid" id="prod-grid-inner">${buildProductGrid()}</div>
      <div style="height:80px;"></div>
    </div>
    <nav class="bnav">
      <div class="bnav-item on">🛒<span>${T('market')}</span></div>
      <div class="bnav-item" style="position:relative;" onclick="navigate('chats')">💬<span>${T('chats')}</span>${unreadBadge}</div>
      <div class="bnav-item" onclick="navigate('profile')">👤<span>${T('profile')}</span></div>
    </nav>
  </div>`;
}

/* ── PRODUCT DETAILS ─────────────────────────────────────── */
function renderDetails() {
  const p = S.selProduct;
  if (!p) return renderBuyerDB();
  const ai = p.ai;
  const hDate = new Date(p.harvestMs).toLocaleDateString('en-IN',{day:'numeric',month:'short',year:'numeric'});
  return `<div class="ms detail-ms">
    ${sbar()}
    <div class="detail-hero">
      <img src="${p.img}" alt="${p.name}" loading="lazy"/>
      <div class="detail-hero-overlay"></div>
      <div class="detail-back" onclick="navigate('buyer-dashboard')">‹</div>
      <div class="detail-price-tag">₹${p.price}<sub>/kg</sub></div>
    </div>
    <div class="detail-body">
      <div class="detail-title-row"><h2>${p.name}</h2>${statusChip(p.quality)}</div>
      <div class="detail-meta">
        <span class="detail-meta-item">📍 ${p.taluk}, ${p.district}</span>
        <span class="detail-meta-item">  📅 ${hDate}</span>
        <span class="detail-meta-item">  📦 ${p.qty}</span>
      </div>
      <div class="detail-sec">
        <div class="detail-sec-title">${T('aboutCrop')}</div>
        <p class="detail-desc">${p.desc}</p>
      </div>
      <div class="detail-sec">
        <div class="detail-sec-title">✨ ${T('aiBreakdown')}</div>
        <div class="ai-card">
          <div class="ai-card-head">🧮 Explainable Pricing Model</div>
          <div class="breakdown">
            <div class="brow"><span>${T('marketBase')}:</span><strong>₹${ai.base}/kg</strong></div>
            <div class="brow"><span>${T('qualityAdj')}:</span><strong style="color:${ai.qAdj>=0?'#2e7d32':'#ef4444'}">₹${ai.qAdj>=0?'+':''}${ai.qAdj.toFixed(1)}</strong></div>
            <div class="brow"><span>${T('freshnessAdj')}:</span><strong style="color:${ai.fAdj>=0?'#2e7d32':'#ef4444'}">₹${ai.fAdj>=0?'+':''}${ai.fAdj.toFixed(1)}</strong></div>
            <div class="brow"><span>${T('seasonAdj')}:</span><strong style="color:${ai.sAdj>=0?'#2e7d32':'#ef4444'}">₹${ai.sAdj>=0?'+':''}${ai.sAdj.toFixed(1)}</strong></div>
            <div class="brow"><span>${T('weatherAdj')}:</span><strong>₹+${ai.wAdj.toFixed(1)}</strong></div>
            <div class="brow total"><span>${T('aiTarget')}</span><strong>₹${ai.final.toFixed(1)}/kg</strong></div>
          </div>
          <div class="conf-wrap">
            <div class="conf-lbl"><span>${T('confidence')}</span><span>${ai.conf}%</span></div>
            <div class="conf-bar"><div class="conf-fill" style="width:${ai.conf}%;"></div></div>
          </div>
          <div class="rec-box">${ai.rec}</div>
        </div>
      </div>
      <div class="detail-sec">
        <div class="detail-sec-title">${T('contactFarmer')}</div>
        <div class="farmer-crd">
          <div class="farmer-ava">👨‍🌾</div>
          <div><h4>${p.farmerName}</h4><p>📞 +91 ${p.farmerPhone}</p></div>
        </div>
      </div>
      <div class="detail-btn-row">
        <button class="btn-p" style="flex:1;" onclick="callFarmer('${p.farmerPhone}')">${T('callFarmer')}</button>
        <button class="btn-chat" onclick="openChat('${p.id}')">${T('chatFarmer')}</button>
      </div>
    </div>
  </div>`;
}

/* ── PROFILE ─────────────────────────────────────────────── */
function renderProfile() {
  const u = S.user || { name:'Basavaraj Gowda', email:'farmer@krishi.com', phone:'9845012345', role:'Farmer', district:'Mandya', taluk:'Maddur', biz:'Gowda Organic Farms', uid:'demo_farmer' };
  const bt = u.role==='Farmer'?'farmer-dashboard':'buyer-dashboard';
  const myCount = getProds().filter(p=>p.farmId===u.uid).length;
  return `<div class="ms profile-ms">
    ${sbar()}
    ${backBtn(bt)}
    <div class="ms-body">
      <div class="profile-hero">
        <div class="profile-ava">👨‍🌾</div>
        <div class="profile-name">${u.name}</div>
        <span class="chip chip-g">${T(u.role.toLowerCase()).toUpperCase()}</span>
      </div>
      <div class="info-list" style="margin-top:8px;">
        <div class="info-row"><div class="info-ico">📞</div><div class="info-txt"><strong>${T('mobile')}</strong><p>+91 ${u.phone||'N/A'}</p></div></div>
        <div class="info-row"><div class="info-ico">📧</div><div class="info-txt"><strong>${T('email')}</strong><p>${u.email}</p></div></div>
        <div class="info-row"><div class="info-ico">📍</div><div class="info-txt"><strong>${T('district')}</strong><p>${u.taluk?u.taluk+', ':''} ${u.district}</p></div></div>
        <div class="info-row"><div class="info-ico">🏪</div><div class="info-txt"><strong>${u.role==='Farmer'?T('farmName'):T('bizName')}</strong><p>${u.biz||'Not set'}</p></div></div>
        ${u.role==='Farmer'?`<div class="info-row"><div class="info-ico">🌾</div><div class="info-txt"><strong>Total Listings</strong><p>${myCount} crops listed</p></div></div>`:''}
      </div>
      <div style="padding:20px 18px 0;">
        <button class="btn-danger" onclick="doLogout()">${T('signOut')}</button>
      </div>
      <div style="height:80px;"></div>
    </div>
    <nav class="bnav">
      <div class="bnav-item" onclick="navigate('${bt}')">${u.role==='Farmer'?'🏠':'🛒'}<span>${u.role==='Farmer'?T('dashboard'):T('market')}</span></div>
      <div class="bnav-item on">👤<span>${T('profile')}</span></div>
    </nav>
  </div>`;
}

/* ── SETTINGS ────────────────────────────────────────────── */
function renderSettings() {
  const bt = S.user?.role==='Buyer'?'buyer-dashboard':'farmer-dashboard';
  const isDark = DB.getDark();
  const lang = DB.getLang();
  return `<div class="ms settings-ms">
    ${sbar()}
    ${backBtn(bt, T('settings'))}
    <div class="ms-body">
      <div class="settings-list" style="margin-top:8px;">
        <div class="setting-item">
          <div class="setting-ico">🔔</div>
          <div class="setting-txt"><strong>${T('notifications')}</strong><p>Buyer interest alerts &amp; price updates</p></div>
          <label class="toggle"><input type="checkbox" checked><span class="tslider"></span></label>
        </div>
        <div class="setting-item" onclick="toggleDark()">
          <div class="setting-ico">🌙</div>
          <div class="setting-txt"><strong>${T('darkMode')}</strong><p>${isDark?'Dark theme active':'Light theme active'}</p></div>
          <label class="toggle" onclick="event.stopPropagation()">
            <input type="checkbox" id="dark-toggle" ${isDark?'checked':''} onchange="toggleDark()">
            <span class="tslider"></span>
          </label>
        </div>
        <div class="setting-item">
          <div class="setting-ico">🌐</div>
          <div class="setting-txt"><strong>${T('language')}</strong><p>Select your preferred language</p></div>
        </div>
        <!-- Language selector row -->
        <div style="padding:0 0 14px;display:flex;gap:8px;">
          <button class="fpill ${lang==='en'?'on':''}" onclick="setLang('en')">🇬🇧 English</button>
          <button class="fpill ${lang==='kn'?'on':''}" onclick="setLang('kn')">🇮🇳 ಕನ್ನಡ</button>
        </div>
        <div class="setting-item">
          <div class="setting-ico">🔒</div>
          <div class="setting-txt"><strong>${T('privacy')}</strong><p>Manage data preferences</p></div>
          <span style="color:var(--t3);">›</span>
        </div>
        <div class="setting-item" onclick="navigate('about')">
          <div class="setting-ico">ℹ️</div>
          <div class="setting-txt"><strong>${T('aboutApp')}</strong><p>Version info &amp; VTU project details</p></div>
          <span style="color:var(--t3);">›</span>
        </div>
      </div>
    </div>
  </div>`;
}

/* ── ABOUT ───────────────────────────────────────────────── */
function renderAbout() {
  return `<div class="ms about-ms">
    ${sbar()}
    ${backBtn('settings')}
    <div class="ms-body">
      <div class="about-hero">
        <div class="about-app-ico">🌿</div>
        <div class="about-app-name">${T('appName')}</div>
        <div class="about-ver">Version 1.0.0 · VTU Mini Project</div>
      </div>
      <div class="about-body">
        <div class="about-sec">
          <h4>Project Overview</h4>
          <p>An AI-powered agricultural marketplace connecting Karnataka farmers directly with verified buyers. Uses explainable pricing algorithms, live weather APIs, and role-based Firebase authentication.</p>
        </div>
        <div class="about-sec">
          <h4>Technology Stack</h4>
          <div class="tech-grid">
            <div class="tech-item">Kotlin</div><div class="tech-item">Jetpack Compose</div>
            <div class="tech-item">Material Design 3</div><div class="tech-item">Hilt DI</div>
            <div class="tech-item">Firebase Auth</div><div class="tech-item">Cloud Firestore</div>
            <div class="tech-item">Firebase Storage</div><div class="tech-item">OpenWeather API</div>
          </div>
        </div>
        <div class="about-sec">
          <h4>Architecture</h4>
          <p>Clean Architecture · MVVM · Repository Pattern · Kotlin Coroutines · Flow · Offline Caching</p>
        </div>
        <div class="about-sec">
          <h4>Project Team</h4>
          <div class="dev-row"><span>Developer 1</span><strong>USN: 1XX22CSXXX</strong></div>
          <div class="dev-row"><span>Developer 2</span><strong>USN: 1XX22CSYYY</strong></div>
          <div class="dev-row"><span>Guide</span><strong>Prof. Name, CS Dept.</strong></div>
        </div>
      </div>
    </div>
  </div>`;
}

/* ══════════════════════════════════════════════════════════
   12. REAL-TIME CHAT FUNCTIONS
   ══════════════════════════════════════════════════════════ */

/**
 * Open a 1-to-1 chat:
 *   Buyers always initiate with their own uid as buyerUid.
 *   Farmers open a chat FROM the inbox (buyerUid passed in).
 */
function openChat(productId, buyerUid, buyerName) {
  const prod = getProds().find(p => p.id === productId);
  if (!prod || !S.user) return;


  S.activeChatProductId = productId;
  S.activeChatProduct   = prod;

  if (S.user.role === 'Buyer') {
    S.activeChatBuyerUid  = S.user.uid;
    S.activeChatBuyerName = S.user.name;
  } else {
    // Farmer tapping a conversation from inbox
    S.activeChatBuyerUid  = buyerUid  || S.activeChatBuyerUid;
    S.activeChatBuyerName = buyerName || S.activeChatBuyerName || 'Buyer';
  }

  // Clear unread for this conversation
  const cid = convId(productId, S.activeChatBuyerUid);
  if (S.chatUnread[cid]) {
    S.chatUnread[cid] = 0;
    S.totalUnread = Object.values(S.chatUnread).reduce((a,b)=>a+b,0);
  }

  navigate('chat');

  // Join socket room — emit immediately if connected, otherwise wait for connect
  const doJoin = () => {
    if (socket?.connected && S.activeChatProductId && S.activeChatBuyerUid) {
      socket.emit('join_chat', { productId: S.activeChatProductId, buyerUid: S.activeChatBuyerUid });
    } else {
      // HTTP fallback — load history directly
      const cid2 = convId(S.activeChatProductId, S.activeChatBuyerUid);
      fetch(`/api/conversations/${encodeURIComponent(cid2)}`)
        .then(r => r.json())
        .then(msgs => { S.chatMessages[cid2] = msgs; renderChatMessages(cid2); })
        .catch(() => {});
    }
  };
  setTimeout(doJoin, 80);
}

function sendChatMessage() {
  const inp  = $('chat-input');
  const text = inp?.value?.trim();
  if (!text) return;

  // Ensure buyer uid is set (defensive)
  if (!S.activeChatBuyerUid && S.user?.role === 'Buyer') {
    S.activeChatBuyerUid  = S.user.uid;
    S.activeChatBuyerName = S.user.name;
  }

  const cid = activeConvId();
  if (!cid || !S.activeChatProductId || !S.activeChatBuyerUid) {
    showToast('Cannot send — please reopen the chat.');
    return;
  }

  // Clear input immediately
  inp.value = '';
  inp.style.height = '40px';
  sendTypingStatus(false);

  // Optimistic bubble so the sender sees their message right away
  const optimisticMsg = {
    id:             'm_opt_' + Date.now(),
    conversationId: cid,
    productId:      S.activeChatProductId,
    buyerUid:       S.activeChatBuyerUid,
    senderUid:      S.user?.uid,
    senderName:     S.user?.name,
    senderRole:     S.user?.role,
    text,
    lang:  DB.getLang(),
    ts:    Date.now(),
    read:  false,
    _pending: true,
  };
  if (!S.chatMessages[cid]) S.chatMessages[cid] = [];
  S.chatMessages[cid].push(optimisticMsg);
  appendChatBubble(optimisticMsg);

  if (socket?.connected) {
    // Re-join the room in case connection was re-established
    socket.emit('join_chat', { productId: S.activeChatProductId, buyerUid: S.activeChatBuyerUid });
    socket.emit('send_message', {
      productId:  S.activeChatProductId,
      buyerUid:   S.activeChatBuyerUid,
      senderUid:  S.user?.uid,
      senderName: S.user?.name,
      senderRole: S.user?.role,
      text,
      lang: DB.getLang(),
    });
  } else {
    showToast('⚠️ Offline — message saved, will send when reconnected');
  }
}

let _typingTimer = null;
function onChatInput() {
  sendTypingStatus(true);
  clearTimeout(_typingTimer);
  _typingTimer = setTimeout(() => sendTypingStatus(false), 2500);
}

function sendTypingStatus(isTyping) {
  if (socket?.connected && S.activeChatProductId && S.activeChatBuyerUid) {
    socket.emit('typing', {
      productId: S.activeChatProductId,
      buyerUid:  S.activeChatBuyerUid,
      uid:   S.user?.uid,
      name:  S.user?.name,
      isTyping,
    });
  }
}

function renderChat() {
  const prod = S.activeChatProduct;
  if (!prod) return renderChats();
  const cid      = activeConvId();
  const msgs     = cid ? (S.chatMessages[cid] || []) : [];
  const meUid    = S.user?.uid;
  const isFarmer = S.user?.role === 'Farmer';
  const otherName   = isFarmer ? (S.activeChatBuyerName || 'Buyer') : prod.farmerName;
  const otherAvatar = isFarmer ? '🏪' : '👨‍🌾';
  const otherUid    = isFarmer ? S.activeChatBuyerUid : prod.farmId;
  const isOnline    = otherUid ? S.onlineUsers.has(otherUid) : false;

  const bubblesHtml = msgs.length ? msgs.map(m => chatBubble(m, meUid)).join('') :
    `<div class="chat-empty"><div class="chat-empty-icon">💬</div><p>No messages yet.<br/>Say hello!</p></div>`;

  return `<div class="ms chat-screen">
    ${sbar(false)}
    <!-- Chat Header -->
    <div class="chat-header">
      <button class="back-btn" onclick="navigate('chats')">‹</button>
      <div class="chat-header-ava">${otherAvatar}</div>
      <div class="chat-header-info">
        <div class="chat-header-name">${escapeHtml(otherName)}</div>
        <div class="chat-header-status">
          <span class="chat-online-dot" id="chat-online-dot" style="background:${isOnline?'#4caf50':'#9e9e9e'}"></span>
          <span id="chat-online-txt">${isOnline ? T('online') : T('offline')}</span>
        </div>
      </div>
      <div class="chat-product-badge" onclick="navigate('product-details')">
        <img src="${prod.img}" alt="${prod.name}"/>
        <span>${prod.name} · ₹${prod.price}/kg</span>
      </div>
      <!-- Language toggle in chat -->
      <div class="chat-lang-toggle">
        <button class="chat-lang-btn ${DB.getLang()==='en'?'on':''}" onclick="setLang('en')">EN</button>
        <button class="chat-lang-btn ${DB.getLang()==='kn'?'on':''}" onclick="setLang('kn')">ಕನ್ನಡ</button>
      </div>
    </div>
    <!-- Messages area -->
    <div class="chat-messages" id="chat-messages">
      <div class="chat-date-divider">Today</div>
      ${bubblesHtml}
    </div>
    <!-- Typing indicator -->
    <div class="typing-indicator" id="typing-indicator" style="display:none;">
      <div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>
      <span class="typing-name"></span>
    </div>
    <!-- Input bar -->
    <div class="chat-input-bar">
      <textarea id="chat-input" class="chat-input" placeholder="${T('typeMessage')}"
        oninput="onChatInput()" onkeydown="if(event.key==='Enter'&&!event.shiftKey){event.preventDefault();sendChatMessage();}"
        rows="1"></textarea>
      <button class="chat-send-btn" onclick="sendChatMessage()" id="chat-send-btn">➤</button>
    </div>
  </div>`;
}

function chatBubble(msg, meUid) {
  const isMine  = msg.senderUid === meUid;
  const time    = new Date(msg.ts).toLocaleTimeString([], { hour:'2-digit', minute:'2-digit' });
  const avatar  = msg.senderRole === 'Farmer' ? '👨‍🌾' : '🏪';
  const pending = msg._pending;
  const tick    = isMine ? (pending ? ' <span style="opacity:0.5">⏱</span>' : ' ✓✓') : '';
  const pendingCls = pending ? ' chat-bubble-pending' : '';
  return `<div class="chat-msg-row ${isMine?'mine':'theirs'}">
    ${!isMine ? `<div class="chat-bubble-avatar">${avatar}</div>` : ''}
    <div class="chat-bubble-wrap">
      ${!isMine ? `<div class="chat-bubble-sender">${escapeHtml(msg.senderName)}</div>` : ''}
      <div class="chat-bubble${pendingCls}">
        <span class="chat-bubble-text">${escapeHtml(msg.text)}</span>
        <span class="chat-bubble-time">${time}${tick}</span>
      </div>
    </div>
  </div>`;
}

function escapeHtml(str) {
  return String(str).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function renderChatMessages(conversationId) {
  const container = $('chat-messages');
  if (!container) return;
  const msgs  = S.chatMessages[conversationId] || [];
  const meUid = S.user?.uid;
  container.innerHTML = `<div class="chat-date-divider">Today</div>` +
    (msgs.length ? msgs.map(m => chatBubble(m, meUid)).join('') :
      `<div class="chat-empty"><div class="chat-empty-icon">💬</div><p>No messages yet.<br/>Say hello!</p></div>`);
  scrollChatToBottom();
}

function appendChatBubble(msg) {
  const container = $('chat-messages');
  if (!container) return;
  // Remove empty state if present
  const empty = container.querySelector('.chat-empty');
  if (empty) empty.remove();
  const meUid = S.user?.uid;
  container.insertAdjacentHTML('beforeend', chatBubble(msg, meUid));
  scrollChatToBottom();
}

function scrollChatToBottom() {
  const c = $('chat-messages');
  if (c) c.scrollTop = c.scrollHeight;
}

/* ── CHATS (Inbox list) ──────────────────────────────────── */
function renderChats() {
  const backTarget = S.user?.role === 'Farmer' ? 'farmer-dashboard' : 'buyer-dashboard';
  const allProds   = getProds();
  const myUid      = S.user?.uid;
  // Collect all product chats this user has participated in (from local memory)
  const conversations = [];
  for (const [productId, msgs] of Object.entries(S.chatMessages)) {
    if (!msgs.length) continue;
    const involved = msgs.some(m => m.senderUid === myUid) || (S.activeChatProductId === productId);
    if (involved) {
      const prod   = allProds.find(p => p.id === productId);
      const last   = msgs[msgs.length - 1];
      const unread = S.chatUnread[productId] || 0;
      conversations.push({ productId, prod, last, unread });
    }
  }
  conversations.sort((a,b) => (b.last?.ts||0) - (a.last?.ts||0));

  const listHtml = conversations.length ? conversations.map(c => {
    const time    = c.last ? new Date(c.last.ts).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'}) : '';
    const preview = c.last ? escapeHtml(c.last.text.slice(0,50)) : 'No messages yet';
    const name    = c.prod ? (S.user?.role==='Farmer' ? c.last?.senderName || 'Buyer' : c.prod.farmerName) : 'Unknown';
    const img     = c.prod?.img || '';
    return `<div class="chat-inbox-item" onclick="openChat('${c.productId}')">
      <div class="chat-inbox-ava">${c.prod ? `<img src="${img}" alt=""/>` : '🌾'}</div>
      <div class="chat-inbox-body">
        <div class="chat-inbox-top">
          <span class="chat-inbox-name">${name} · ${c.prod?.name||'Crop'}</span>
          <span class="chat-inbox-time">${time}</span>
        </div>
        <div class="chat-inbox-preview">${preview}</div>
      </div>
      ${c.unread > 0 ? `<div class="chat-inbox-badge">${c.unread}</div>` : ''}
    </div>`;
  }).join('') : `<div class="empty-state"><div class="empty-icon">💬</div><h4>${T('noChats')}</h4><p>${T('noChatsSub')}</p></div>`;

  return `<div class="ms farmer-db-ms">
    ${sbar()}
    ${backBtn(backTarget, T('inboxTitle'))}
    <div class="ms-body" style="padding:8px 0;">
      <div class="chat-inbox-list">${listHtml}</div>
      <div style="height:80px;"></div>
    </div>
    <nav class="bnav">
      <div class="bnav-item" onclick="navigate('${backTarget}')">${S.user?.role==='Farmer'?'🏠':'🛒'}<span>${S.user?.role==='Farmer'?T('dashboard'):T('market')}</span></div>
      <div class="bnav-item on">💬<span>${T('chats')}</span></div>
      <div class="bnav-item" onclick="navigate('profile')">👤<span>${T('profile')}</span></div>
    </nav>
  </div>`;
}

/* ══════════════════════════════════════════════════════════
   13. CONSOLE INIT
   ══════════════════════════════════════════════════════════ */
function initConsole() {
  // Restore dark toggle state in console
  const dt = $('dark-toggle-con');
  if (dt) dt.checked = DB.getDark();
  // Restore language buttons
  const lang = DB.getLang();
  $('lang-en')?.classList.toggle('active', lang==='en');
  $('lang-kn')?.classList.toggle('active', lang==='kn');
}

/* ══════════════════════════════════════════════════════════
   14. BOOTSTRAP
   ══════════════════════════════════════════════════════════ */
function boot() {
  const vEl = $('viewport');
  if (!vEl) { setTimeout(boot, 40); return; }
  initConsole();
  applyDarkMode();
  initSocket();
  // Fetch fresh products from server, then navigate
  fetch('/api/products')
    .then(r => r.json())
    .then(products => { DB.saveProds(products); })
    .catch(() => {})
    .finally(() => {
      if (S.user) {
        navigate(S.user.role === 'Farmer' ? 'farmer-dashboard' : 'buyer-dashboard');
      } else {
        navigate('splash');
      }
    });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', boot);
} else {
  boot();
}
