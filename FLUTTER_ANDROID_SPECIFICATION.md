# HOSTEL HUB — MASTER FLUTTER ANDROID APPLICATION SPECIFICATION & AI ENGINEERING BLUEPRINT

> **AI SYSTEM INSTRUCTION & AUTONOMOUS AGENT ROLE DEFINITION:**
> You are an Expert Flutter/Dart Mobile Architect, Full-Stack MERN Engineer, and Autonomous AI Agent. Your objective is to build, expand, refactor, and maintain the **Hostel Hub Android Application** using **Flutter (Dart)**.
> This mobile application is the direct Android counterpart to the fully completed, production-ready **Hostel Hub MERN Web Application (Express + MongoDB + Socket.io + Google Gemini AI)**.
> 
> Just as **Airbnb** seamlessly unifies its web platform and native mobile applications—sharing identical REST APIs, database models, real-time WebSocket events, user roles, and visual aesthetics—the **Hostel Hub Flutter Android App** must perfectly mirror the web app across all **12 official Final Year Project (FYP) modules**.
>
> Whenever you have access to this project workspace (`/Users/macbookair/VS CODE PROJECTS/hostel-hub`), you **MUST** reference both this master specification and the underlying backend files (`server/index.js`, `server/models/*.js`, `server/routes/*.js`) to ensure 100% contract fidelity. Never guess or invent imaginary endpoints or models.

---

## 🏗️ 1. ARCHITECTURAL & SYSTEM SYNERGY (WEB + ANDROID)

### 1.1 Shared MERN Backend & Real-Time Server Configuration
- **REST API Base URL:**
  - **Production/Cloud:** `http://<CLOUD_IP_OR_DOMAIN>:5003/api`
  - **Local Android Emulator Testing:** `http://10.0.2.2:5003/api` (The Android emulator loopback alias mapping to the host computer's `localhost:5003`).
  - **Physical Android Device on Local Wi-Fi:** `http://<YOUR_LAN_IPv4_ADDRESS>:5003/api` (e.g., `http://192.168.1.100:5003/api`).
- **WebSocket (Socket.io) Server URL:**
  - **Local Android Emulator:** `ws://10.0.2.2:5003` (or `http://10.0.2.2:5003` for polling fallback).
- **Authentication Header Enforcement:**
  Every protected API request must inject the stored JWT token into the HTTP request header:
  `Authorization: Bearer <ACCESS_TOKEN>`

### 1.2 Recommended Flutter Clean Architecture Directory Structure
```text
lib/
├── main.dart
├── config/
│   ├── app_theme.dart          # Light/Dark mode tokens, glassmorphism overlays & typography
│   ├── constants.dart          # API URLs, timeout limits, error messages, regex rules
│   └── routes.dart             # Named routes / GoRouter configuration
├── core/
│   ├── network/
│   │   ├── api_client.dart     # Dio client with JWT interceptor & 401 auto-logout handling
│   │   └── socket_service.dart # Singleton Socket.io connection manager
│   └── storage/
│       └── secure_storage.dart # flutter_secure_storage wrapper for JWT & user role
├── models/                     # Dart models mirroring exact MongoDB schemas
│   ├── user_model.dart
│   ├── hostel_model.dart
│   ├── booking_model.dart
│   ├── review_model.dart
│   ├── chat_model.dart
│   ├── notification_model.dart
│   ├── agreement_model.dart
│   ├── bank_details_model.dart
│   ├── fraud_report_model.dart
│   └── analytics_model.dart
├── providers/                  # State management controllers (Riverpod / Provider)
│   ├── auth_provider.dart
│   ├── hostel_provider.dart
│   ├── booking_provider.dart
│   ├── chat_provider.dart
│   └── notification_provider.dart
└── screens/                    # UI Screens grouped strictly by the 12 official modules
    ├── auth/                   # Module 1: Login, Register, Forgot Password, ID Verification
    ├── admin/                  # Module 2 & 11: Admin Dashboard, Moderation Queue, Fraud Alerts
    ├── hostels/                # Module 3, 7 & 10: Property Listing, Explore, Maps, Fair Rent
    ├── bookings/               # Module 4 & 8: Booking Form, History, Receipt Upload, Bank Config
    ├── reviews/                # Module 5: Star Ratings, Write Review Modal, Trust Score
    ├── chat/                   # Module 6: Conversation List, Live Chat Room
    ├── notifications/          # Module 9: Notification Center, Bell Badge
    └── agreements/             # Module 12: Digital Agreement Dashboard, Touchscreen Signature Canvas
```

### 1.3 Mandatory Dependencies (`pubspec.yaml`)
```yaml
dependencies:
  flutter:
    sdk: flutter
  dio: ^5.4.0                   # Advanced HTTP client with interceptors and error normalization
  flutter_secure_storage: ^9.0.0 # Encrypted keychain/keystore storage for JWT access/refresh tokens
  shared_preferences: ^2.2.2    # Non-sensitive app settings (Dark Mode choice, onboarding complete)
  provider: ^6.1.1              # Or flutter_riverpod: ^2.4.9 for reactive state management
  socket_io_client: ^2.0.3      # Low-latency real-time WebSocket connectivity
  flutter_map: ^6.1.0           # OpenStreetMap interactive map rendering (or google_maps_flutter)
  latlong2: ^0.9.0              # Geolocation coordinates handling
  image_picker: ^1.0.7          # Selecting hostel pictures and CNIC/Passport ID documents
  file_picker: ^8.0.0           # Picking PDF agreements and bank payment receipts
  signature: ^5.4.0             # Touchscreen digital drawing pad for rental contracts
  fl_chart: ^0.66.2             # Owner revenue charts & admin platform growth graphs
  lucide_icons: ^1.1.4          # Mirroring exact vector iconography from web app
  intl: ^0.19.0                 # Date formatting and Pakistani Rupee (PKR) currency formatting
  cached_network_image: ^3.3.1  # High-performance image caching with loading indicators
```

---

## 🎨 2. DESIGN SYSTEM & UI/UX SPECIFICATIONS

The mobile app must deliver an ultra-premium, modern, dynamic visual impression:
1. **Harmonious Color Tokens:**
   - **Primary Brand (Indigo/Violet):** `#6366F1` (Light Mode) to `#4F46E5` (Dark Mode)
   - **Success / Verified / Fair Price (Emerald):** `#10B981`
   - **Warning / Pending / Above Average (Amber):** `#F59E0B`
   - **Danger / Rejected / Fraud / Ban (Rose):** `#F43F5E`
   - **Surface Backgrounds:** `#F8FAFC` (Light Canvas) / `#0F172A` (Dark Canvas)
   - **Card Elements:** `#FFFFFF` (Light Card) / `#1E293B` (Dark Card)
2. **Typography:** Use `GoogleFonts.inter()` or `GoogleFonts.outfit()` across all screens.
3. **Glassmorphism & Micro-Animations:**
   - Use `BackdropFilter(filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12))` for bottom navigation bars, modals, and floating action trays.
   - All interactive cards must feature subtle scale effects on tap (`InkWell` with `BorderRadius.circular(20)`).
4. **Animated Bottom Navigation Bar:**
   - **Customer/Student Role:** `Explore/Home` | `Wishlists` | `Chat` | `Bookings/Agreements` | `Profile`
   - **Hostel Owner Role:** `My Properties` | `Bookings/Verify` | `Chat` | `Analytics/Rent` | `Profile`
   - **Admin Role:** `Platform Stats` | `Moderation Queue` | `Fraud Alerts` | `User Management` | `Profile`

---

## 📋 3. MASTER DART DATA MODELS (EXACT MERN CONTRACTS)

### 3.1 `UserModel` (`lib/models/user_model.dart`)
```dart
class UserModel {
  final String id;
  final String username;
  final String email;
  final String role; // 'customer', 'owner', or 'admin'
  final String firstName;
  final String lastName;
  final String phone;
  final String verificationStatus; // 'unverified', 'pending', 'verified', 'rejected'
  final String? documentUrl;
  final int trustScore;
  final bool emailVerified;

  UserModel({
    required this.id, required this.username, required this.email,
    required this.role, required this.firstName, required this.lastName,
    required this.phone, required this.verificationStatus,
    this.documentUrl, required this.trustScore, required this.emailVerified,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) => UserModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    username: json['username'] ?? '',
    email: json['email'] ?? '',
    role: json['role'] ?? 'customer',
    firstName: json['firstName'] ?? '',
    lastName: json['lastName'] ?? '',
    phone: json['phone'] ?? '',
    verificationStatus: json['verificationStatus'] ?? 'unverified',
    documentUrl: json['documentUrl'],
    trustScore: json['trustScore'] ?? 80,
    emailVerified: json['emailVerified'] ?? false,
  );

  Map<String, dynamic> toJson() => {
    '_id': id, 'username': username, 'email': email, 'role': role,
    'firstName': firstName, 'lastName': lastName, 'phone': phone,
    'verificationStatus': verificationStatus, 'documentUrl': documentUrl,
    'trustScore': trustScore, 'emailVerified': emailVerified,
  };
}
```

### 3.2 `HostelModel`, `RoomModel`, `BedModel` (`lib/models/hostel_model.dart`)
```dart
class BedModel {
  final String bedId;
  final String bedNumber;
  final String? bookingId;
  final bool isOccupied;

  BedModel({required this.bedId, required this.bedNumber, this.bookingId, required this.isOccupied});

  factory BedModel.fromJson(Map<String, dynamic> json) => BedModel(
    bedId: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    bedNumber: json['bedNumber'] ?? 'Bed 1',
    bookingId: json['bookingId']?.toString(),
    isOccupied: json['isOccupied'] ?? (json['bookingId'] != null),
  );
}

class RoomModel {
  final String id;
  final String name;
  final String type; // 'Single', 'Double', 'Triple', 'Shared'
  final int price;
  final List<BedModel> beds;

  RoomModel({required this.id, required this.name, required this.type, required this.price, required this.beds});

  factory RoomModel.fromJson(Map<String, dynamic> json) => RoomModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    name: json['name'] ?? 'Room',
    type: json['type'] ?? 'Shared',
    price: json['price'] ?? 0,
    beds: (json['beds'] as List<dynamic>? ?? []).map((x) => BedModel.fromJson(x)).toList(),
  );
}

class HostelModel {
  final String id;
  final String name;
  final String description;
  final String location;
  final String address;
  final double price;
  final double rating;
  final String ownerId;
  final List<String> images;
  final List<String> amenities;
  final String status; // 'pending', 'active', 'inactive', 'flagged'
  final double latitude;
  final double longitude;
  final String fairnessLabel; // 'Fair Price', 'Below Market', 'Above Average'
  final int riskScore;
  final int capacity;
  final List<RoomModel> rooms;

  HostelModel({
    required this.id, required this.name, required this.description,
    required this.location, required this.address, required this.price,
    required this.rating, required this.ownerId, required this.images,
    required this.amenities, required this.status, required this.latitude,
    required this.longitude, required this.fairnessLabel, required this.riskScore,
    required this.capacity, required this.rooms,
  });

  factory HostelModel.fromJson(Map<String, dynamic> json) => HostelModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    name: json['name'] ?? '',
    description: json['description'] ?? '',
    location: json['location'] ?? '',
    address: json['address'] ?? '',
    price: (json['price'] ?? 0).toDouble(),
    rating: (json['rating'] ?? 0.0).toDouble(),
    ownerId: json['ownerId']?.toString() ?? json['owner']?.toString() ?? '',
    images: List<String>.from(json['images'] ?? []),
    amenities: List<String>.from(json['amenities'] ?? []),
    status: json['status'] ?? 'pending',
    latitude: (json['latitude'] ?? 33.6844).toDouble(),
    longitude: (json['longitude'] ?? 73.0479).toDouble(),
    fairnessLabel: json['fairnessLabel'] ?? 'Fair Price',
    riskScore: json['riskScore'] ?? 0,
    capacity: json['capacity'] ?? 10,
    rooms: (json['rooms'] as List<dynamic>? ?? []).map((x) => RoomModel.fromJson(x)).toList(),
  );
}
```

### 3.3 `BookingModel` (`lib/models/booking_model.dart`)
```dart
class BookingModel {
  final String id;
  final String hostelId;
  final String hostelName;
  final String studentId;
  final String studentName;
  final String roomId;
  final String roomName;
  final String bedId;
  final String checkInDate;
  final int durationMonths;
  final double totalPrice;
  final String status; // 'pending', 'confirmed', 'rejected', 'cancelled'
  final String paymentStatus; // 'pending', 'submitted', 'verified', 'rejected'
  final String? receiptUrl;

  BookingModel({
    required this.id, required this.hostelId, required this.hostelName,
    required this.studentId, required this.studentName, required this.roomId,
    required this.roomName, required this.bedId, required this.checkInDate,
    required this.durationMonths, required this.totalPrice, required this.status,
    required this.paymentStatus, this.receiptUrl,
  });

  factory BookingModel.fromJson(Map<String, dynamic> json) => BookingModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    hostelId: json['hostelId']?.toString() ?? '',
    hostelName: json['hostelName'] ?? 'Hostel',
    studentId: json['studentId']?.toString() ?? '',
    studentName: json['studentName'] ?? 'Student',
    roomId: json['roomId']?.toString() ?? '',
    roomName: json['roomName'] ?? 'Room',
    bedId: json['bedId']?.toString() ?? '',
    checkInDate: json['checkInDate'] ?? '',
    durationMonths: json['durationMonths'] ?? 1,
    totalPrice: (json['totalPrice'] ?? 0).toDouble(),
    status: json['status'] ?? 'pending',
    paymentStatus: json['paymentStatus'] ?? 'pending',
    receiptUrl: json['receiptUrl'],
  );
}
```

### 3.4 `ChatMessageModel` & `ConversationModel` (`lib/models/chat_model.dart`)
```dart
class ChatMessageModel {
  final String id;
  final String conversationId;
  final String senderId;
  final String senderName;
  final String content;
  final DateTime createdAt;
  final bool isRead;

  ChatMessageModel({
    required this.id, required this.conversationId, required this.senderId,
    required this.senderName, required this.content, required this.createdAt,
    required this.isRead,
  });

  factory ChatMessageModel.fromJson(Map<String, dynamic> json) => ChatMessageModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    conversationId: json['conversationId']?.toString() ?? '',
    senderId: json['senderId']?.toString() ?? '',
    senderName: json['senderName'] ?? 'User',
    content: json['content'] ?? '',
    createdAt: DateTime.tryParse(json['createdAt'] ?? '') ?? DateTime.now(),
    isRead: json['isRead'] ?? false,
  );
}

class ConversationModel {
  final String id;
  final String participantName;
  final String participantRole;
  final String lastMessage;
  final DateTime updatedAt;
  final int unreadCount;

  ConversationModel({
    required this.id, required this.participantName, required this.participantRole,
    required this.lastMessage, required this.updatedAt, required this.unreadCount,
  });

  factory ConversationModel.fromJson(Map<String, dynamic> json) => ConversationModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    participantName: json['participantName'] ?? 'User',
    participantRole: json['participantRole'] ?? 'customer',
    lastMessage: json['lastMessage'] ?? '',
    updatedAt: DateTime.tryParse(json['updatedAt'] ?? '') ?? DateTime.now(),
    unreadCount: json['unreadCount'] ?? 0,
  );
}
```

### 3.5 `AgreementModel` (`lib/models/agreement_model.dart`)
```dart
class AgreementModel {
  final String id;
  final String bookingId;
  final String hostelId;
  final String hostelName;
  final String studentId;
  final String studentName;
  final String ownerId;
  final String termsAndConditions;
  final double monthlyRent;
  final String status; // 'pending', 'signed', 'terminated'
  final String? studentSignature; // Base64 PNG string
  final DateTime? signedAt;

  AgreementModel({
    required this.id, required this.bookingId, required this.hostelId,
    required this.hostelName, required this.studentId, required this.studentName,
    required this.ownerId, required this.termsAndConditions, required this.monthlyRent,
    required this.status, this.studentSignature, this.signedAt,
  });

  factory AgreementModel.fromJson(Map<String, dynamic> json) => AgreementModel(
    id: json['_id']?.toString() ?? json['id']?.toString() ?? '',
    bookingId: json['bookingId']?.toString() ?? '',
    hostelId: json['hostelId']?.toString() ?? '',
    hostelName: json['hostelName'] ?? 'Hostel',
    studentId: json['studentId']?.toString() ?? '',
    studentName: json['studentName'] ?? 'Student',
    ownerId: json['ownerId']?.toString() ?? '',
    termsAndConditions: json['termsAndConditions'] ?? '',
    monthlyRent: (json['monthlyRent'] ?? 0).toDouble(),
    status: json['status'] ?? 'pending',
    studentSignature: json['studentSignature'],
    signedAt: json['signedAt'] != null ? DateTime.tryParse(json['signedAt']) : null,
  );
}
```

---

## 🛠️ 4. DETAILED SPECIFICATION & API MAP FOR ALL 12 FYP MODULES

---

### MODULE 1: AUTHENTICATION & IDENTITY VERIFICATION
- **Overview:** Manages user onboarding, secure JWT authentication (`accessToken` + `refreshToken`), social login callbacks, password recovery, and identity verification (`CNIC`/`Passport` upload).
- **Flutter UI Screens:**
  1. `LoginScreen`: Email/Password text fields, social OAuth buttons (`Google`, `Facebook`), and error snackbars.
  2. `RegisterScreen`: Full name, username, email, phone, password, and Role Picker (`customer` vs `owner`).
  3. `IdentityVerificationScreen`: Document type selector (`CNIC` / `Passport`) + `image_picker` / `file_picker` upload button.
- **Backend API Routes & Request/Response Contracts:**
  - `POST /api/auth/register`
    - Request Body: `{ "email": "ali@gmail.com", "password": "Password123!", "username": "ali123", "firstName": "Ali", "lastName": "Khan", "phone": "03001234567", "role": "customer" }`
    - Response (201): `{ "user": { ... }, "accessToken": "eyJ...", "refreshToken": "eyJ..." }`
  - `POST /api/auth/login`
    - Request Body: `{ "email": "ali@gmail.com", "password": "Password123!" }`
    - Response (200): `{ "user": { ... }, "accessToken": "eyJ...", "refreshToken": "eyJ..." }`
  - `POST /api/verification/upload` (Headers: `Authorization: Bearer <token>`, `Content-Type: multipart/form-data`)
    - Form Data: File key `document` (image/pdf <= 5MB).
    - Response (200): `{ "message": "Document uploaded successfully", "documentUrl": "/uploads/identity_docs/id-xxx.jpg" }`
- **State Management & Operational Flow:**
  - `AuthProvider` stores `accessToken` and `refreshToken` in `flutter_secure_storage`.
  - Checks `user.role` on app launch: routes `customer` to Student Home, `owner` to Owner Dashboard, and `admin` to Admin Panel.

---

### MODULE 2: ADMIN & MODERATION PLATFORM
- **Overview:** System dashboard restricted strictly to `admin` role. Controls platform health stats, user lifecycle management, content moderation queue for submitted listings, and report generation.
- **Flutter UI Screens:**
  1. `AdminDashboardScreen`: Recharts/`fl_chart` cards showing Total Users, Active Hostels, Monthly Revenue, and Pending Verifications.
  2. `UserManagementScreen`: Searchable user list with role filters, suspension toggle, and instant ID document verification buttons.
  3. `ModerationQueueScreen`: List of newly created hostels (`status: 'pending'`). Features quick `Approve Listing` (`status: 'active'`) and `Reject Listing` (`status: 'inactive'`) buttons.
- **Backend API Routes & Contracts:**
  - `GET /api/admin/stats` -> Response: `{ "totalUsers": 120, "activeHostels": 45, "totalRevenue": 2500000, "pendingVerifications": 12 }`
  - `GET /api/admin/users` -> Response: `[ { "_id": "...", "username": "...", "role": "customer", "verificationStatus": "verified" } ]`
  - `PUT /api/admin/users/:id/status` -> Body: `{ "verificationStatus": "verified" }`
  - `GET /api/hostels/admin/pending` -> Response: `[ { "_id": "...", "name": "...", "status": "pending", "riskScore": 15 } ]`
  - `PUT /api/hostels/admin/moderate/:id` -> Body: `{ "status": "active" }`

---

### MODULE 3: PROPERTY LISTING & ROOM MANAGEMENT
- **Overview:** Allows Hostel Owners to create, update, activate, and manage properties, including uploading photo galleries, setting GPS map markers, specifying amenities, and building room/bed inventories.
- **Flutter UI Screens:**
  1. `OwnerHostelListScreen`: Overview of owner's properties categorized by `active`, `pending`, and `inactive`.
  2. `PropertyListingFormScreen`: Multi-step form:
     - *Step 1 (Basic Info):* Name, description, address, sector, monthly rent.
     - *Step 2 (Map Coordinates):* Interactive `flutter_map` picker where owner taps to set exact `latitude` and `longitude`.
     - *Step 3 (Amenities Grid):* Checkboxes for Wi-Fi, AC, Laundry, CCTV, Parking, Generator, Mess.
     - *Step 4 (Room & Bed Config):* Add Single/Double/Shared rooms and assign bed counts (`capacity`).
     - *Step 5 (Photo Upload):* Multi-image picker (`image_picker`) uploading up to 10 photos (`multer` limits).
  3. `HostelDetailScreen`: Rich public detail screen displaying image slider, interactive room grid, map location, and booking CTAs.
- **Backend API Routes & Contracts:**
  - `GET /api/hostels/owner/my-hostels` (Headers: `Bearer <token>`) -> Returns list of owner's `HostelModel`.
  - `POST /api/hostels` (Multipart form or JSON) -> Creates new hostel with `status: 'pending'` for admin review.
  - `PUT /api/hostels/:id` & `DELETE /api/hostels/:id`

---

### MODULE 4: SCHEDULING & ONLINE BOOKING
- **Overview:** Enables students to select check-in dates, pick a specific room and bed slot, verify real-time availability, submit booking requests, and manage booking lifecycles.
- **Flutter UI Screens:**
  1. `BookingFormBottomSheet`: Modal launched from `HostelDetailScreen`. Student picks Check-in date (`showDatePicker`), Duration (1 to 12 months), Room, and specific vacant Bed slot (`isOccupied: false`).
  2. `StudentBookingHistoryScreen`: Tabs for `Pending`, `Confirmed`, `Rejected`, and `Cancelled` bookings.
  3. `OwnerBookingDashboardScreen`: Owner interface listing incoming booking requests with `Accept Booking` and `Decline` action buttons.
- **Backend API Routes & Contracts:**
  - `POST /api/bookings` -> Body: `{ "hostelId": "...", "roomId": "...", "bedId": "...", "checkInDate": "2026-08-01", "durationMonths": 6, "totalPrice": 210000 }`
  - `GET /api/bookings/my-bookings` (Student) & `GET /api/bookings/owner-bookings` (Owner).
  - `PUT /api/bookings/:id/status` -> Body: `{ "status": "confirmed" }` (Marking confirmed locks the bed slot).

---

### MODULE 5: REVIEWS & TRUST SYSTEM
- **Overview:** Transparent rating system where verified students leave star feedback and comments for stayed hostels. Aggregates trust scores and category ratings (`Cleanliness`, `Security`, `Food`, `Value`).
- **Flutter UI Screens:**
  1. `ReviewsSectionWidget`: Displayed on `HostelDetailScreen`. Shows overall star badge, category progress bars, and review cards.
  2. `WriteReviewModal`: Interactive star slider (`RatingBar`) + text comment box.
  3. `OwnerReviewManagementScreen`: Allows owners to read reviews and post official host replies.
- **Backend API Routes & Contracts:**
  - `GET /api/reviews/hostel/:hostelId` -> Response: `[ { "_id": "...", "rating": 5, "comment": "Great clean hostel!", "studentName": "Ali" } ]`
  - `POST /api/reviews` -> Body: `{ "hostelId": "...", "rating": 5, "comment": "Excellent experience and security!" }`
  - `DELETE /api/reviews/:id`

---

### MODULE 6: CHAT & REAL-TIME COMMUNICATION HUB
- **Overview:** Low-latency WebSocket messaging (`Socket.io`) enabling live chat between Students and Owners without page refreshes. Features unread badges, typing indicators, and message history persistence.
- **Flutter UI Screens:**
  1. `ChatDashboardScreen`: Conversation list displaying partner avatar, last message text, timestamp, and unread dot.
  2. `ChatRoomScreen`: Live messaging window with distinct chat bubbles (Indigo for outgoing, Slate for incoming), typing indicator animation, and instant send bar.
- **Socket.io WebSocket Protocol & API Routes:**
  - **Connection:** `io('ws://10.0.2.2:5003', OptionBuilder().setTransports(['websocket']).setAuth({'token': accessToken}).build())`
  - **Socket Events emitted by Flutter:**
    - `socket.emit('joinRoom', conversationId);`
    - `socket.emit('sendMessage', { 'conversationId': id, 'senderId': myId, 'receiverId': partnerId, 'content': text });`
    - `socket.emit('typing', conversationId);` / `socket.emit('stopTyping', conversationId);`
  - **Socket Events listened by Flutter:**
    - `socket.on('newMessage', (data) => _appendMessageToUI(ChatMessageModel.fromJson(data)));`
    - `socket.on('typing', (_) => setState(() => isPartnerTyping = true));`
  - **REST Backup:** `GET /api/chat/conversations` & `GET /api/chat/messages/:conversationId`

---

### MODULE 7: SEARCH, FILTER SYSTEM & RECOMMENDATIONS
- **Overview:** Exploration engine powered by **NLP Smart Search** (`Google Gemini AI` matching queries like *"quiet air-conditioned room near NUST under 35k"*), multi-parameter sliders, and map-based radius discovery.
- **Flutter UI Screens:**
  1. `ExploreHomeScreen`: Dual-mode search bar (`Standard` vs `AI Smart Search` with `Sparkles` icon), horizontal sector chips, and recommended hostel cards.
  2. `FilterBottomSheet`: Sliders for Price Range (PKR 10k to PKR 150k+), radius distance picker (in km), and amenity checkboxes.
  3. `HostelMapScreen`: Interactive `flutter_map` showing price pills (`PKR 35k`) for all filtered properties. Tapping a marker pops up a bottom preview card.
- **Backend API Routes & Contracts:**
  - `GET /api/hostels?search=...&minPrice=10000&maxPrice=50000&amenities=WiFi,AC&sort=price_asc`
  - `POST /api/ai/smart-search` -> Body: `{ "query": "quiet room near NUST under 35k with AC and high security" }`
    - Response: `{ "suggestions": [ { "hostel": { ...HostelModel... }, "matchReason": "Located 1.2km from NUST with AC and CCTV" } ] }`

---

### MODULE 8: PAYMENT VERIFICATION MODULE
- **Overview:** Manages bank transfer instructions and rent receipts. Owners configure bank account details, students upload screenshot proof/receipts, and owners verify payment authenticity.
- **Flutter UI Screens:**
  1. `BankDetailsConfigScreen`: Owner inputs Bank Name, Account Title, IBAN/Account Number, and JazzCash/Easypaisa details.
  2. `PaymentReceiptUploadModal`: Student views owner's bank instructions, picks receipt image/PDF (`file_picker`), and submits.
  3. `BookingVerificationDashboardScreen`: Owner interface displaying submitted payment receipts with zoomable preview, `Verify Payment`, and `Reject Receipt` buttons.
- **Backend API Routes & Contracts:**
  - `GET /api/users/bank-details/:ownerId` & `PUT /api/users/bank-details` -> Body: `{ "bankName": "Meezan Bank", "accountTitle": "Hostel Hub Ltd", "accountNumber": "PK00MEZN000123456789" }`
  - `POST /api/bookings/:id/receipt` (Multipart upload with file key `receipt`) -> Sets `paymentStatus: 'submitted'`.
  - `PUT /api/bookings/:id/verify-payment` -> Sets `paymentStatus: 'verified'` and marks booking paid.

---

### MODULE 9: NOTIFICATIONS PANEL & REAL-TIME ALERTS
- **Overview:** Real-time push alerts (`Socket.io` broadcast + REST queue) notifying users of critical events (Booking confirmation, new chat message, security alert, payment verified, agreement ready).
- **Flutter UI Screens:**
  1. `NotificationCenterScreen`: Scrollable list of notification cards featuring type-specific Lucide icons (Bell, CheckCircle, AlertTriangle, MessageSquare), timestamps, and unread dot indicators.
  2. `NotificationBadgeWidget`: Bell icon in top AppBar showing live numeric unread count badge.
- **Backend API Routes & Socket Events:**
  - `GET /api/notifications` -> Returns list of `NotificationModel`.
  - `PUT /api/notifications/:id/read` & `PUT /api/notifications/read-all`
  - **Socket Event:** `socket.on('notification', (payload) => ...)` -> Instantly plays alert sound/vibration, increments top badge, and inserts notification card into list.

---

### MODULE 10: PRICE GUIDANCE (FAIR RENT ESTIMATOR)
- **Overview:** AI valuation tool using Gemini ML benchmarks against Pakistani city averages (Islamabad, Lahore, Karachi, Rawalpindi) based on amenities, capacity, and sector location.
- **Flutter UI Screens:**
  1. `FairRentEstimatorScreen` (Owner Tool): Owner picks City/Sector, Room Type, Capacity, and checks amenities. Tapping *"Calculate Fair Rent"* calls AI engine and displays recommended price band (`PKR 34,000 – PKR 38,000`) with actionable pricing tips.
  2. `FairnessBadgeWidget`: Color-coded pill displayed on hostel cards: **`Fair Price` (Emerald)**, **`Below Market` (Indigo)**, or **`Above Average` (Amber)**.
- **Backend API Routes & Contracts:**
  - `POST /api/ai/estimate-rent` -> Body: `{ "location": "Islamabad H-12", "roomType": "Single", "capacity": 1, "amenities": ["WiFi", "AC", "Generator"] }`
    - Response: `{ "estimatedPrice": 36000, "fairnessLabel": "Fair Price", "confidenceScore": 92, "marketBenchmarks": { ... } }`

---

### MODULE 11: FRAUD DETECTION & TRUST SCORE SYSTEM
- **Overview:** Automated 3-Layer Security Platform monitoring platform integrity. Tracks owner **Trust Scores (0 to 100)**, detects duplicate/stolen listing photos using perceptual image hashing (`sharp`), flags suspicious pricing/descriptions, and provides public reporting channels.
- **Flutter UI Screens:**
  1. `TrustScoreBadgeWidget`: Shield icon displayed on owner profiles and hostel cards (`Trust Score: 92/100`).
  2. `ReportFraudModal`: Modal launched from hostel detail. Student picks reason (`Fake Photos`, `Unresponsive Owner`, `Scam Attempt`), inputs description, attaches proof photo, and submits.
  3. `AdminFraudDashboardScreen` (Admin Only): High-priority security queue showing flagged hostels, AI risk score breakdowns (`riskScore: 85`), duplicate image matches, and user fraud reports with quick `Ban Listing` or `Dismiss Alert` actions.
- **Backend API Routes & Contracts:**
  - `POST /api/fraud/report` -> Body: `{ "hostelId": "...", "reason": "Fake Photos", "description": "Photos belong to another hotel in Blue Area" }`
  - `GET /api/fraud/admin/reports` & `GET /api/fraud/admin/flagged-hostels`
  - `PUT /api/fraud/admin/resolve/:id` -> Body: `{ "action": "ban_hostel" }`

---

### MODULE 12: DIGITAL AGREEMENTS & E-SIGNATURES
- **Overview:** Generates legally binding digital rental agreements between students and hostel owners. Features an interactive touchscreen e-signature canvas allowing both parties to draw their physical signatures and download finalized PDF contracts.
- **Flutter UI Screens:**
  1. `AgreementDashboardScreen`: List of active and pending rental contracts.
  2. `AgreementViewerScreen`: Full scrollable contract text displaying terms and conditions, monthly rent, deposit amount, and check-in dates.
  3. `ESignatureCanvasModal`: Interactive touchscreen drawing pad using `signature: ^5.4.0`. Allows user to draw signature with finger/stylus, clear canvas, and tap *"Confirm Digital Signature"*.
  4. `AgreementHistoryScreen`: Archive of signed contracts with `Download PDF` action buttons.
- **Backend API Routes & Contracts:**
  - `POST /api/agreements/generate` -> Body: `{ "bookingId": "...", "hostelId": "...", "studentId": "...", "ownerId": "...", "termsAndConditions": "Standard tenancy terms...", "monthlyRent": 35000 }`
  - `GET /api/agreements/my-agreements`
  - `POST /api/agreements/:id/sign` -> Body: `{ "signatureData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg..." }` -> Sets status to `signed`.

---

## 🔒 5. MANDATORY SECURITY ENFORCEMENT (5 PILLARS IN FLUTTER)

When implementing or expanding the mobile application, the AI must enforce the exact same **5 Security Pillars** as the backend:

1. **Input Validation & Sanitization:**
   - Always validate text fields (`TextFormField`) using `validator:` callbacks before form submission (enforce character limits and email/phone regex).
   - Sanitize all string inputs to strip HTML tags or leading MongoDB operators (`$`, `.`) before transmitting API bodies.
2. **Secrets & Token Management:**
   - **Never** store `accessToken` or `refreshToken` in unencrypted `SharedPreferences`. Always use `flutter_secure_storage`.
   - Never print raw JWT tokens, user passwords, or full authorization headers in `debugPrint()` or `print()` console logs.
3. **Network Error Normalization:**
   - Create a centralized Dio interceptor (`api_client.dart`). When the backend returns `500 Internal Server Error` or a network exception occurs, **never** show raw Node/Mongoose stack traces to the user. Display normalized, user-friendly alerts (`"An unexpected server error occurred. Please try again later."`).
   - If a `401 Unauthorized` response is received, automatically clear `flutter_secure_storage` and navigate the user cleanly to `LoginScreen`.
4. **File Upload Safety:**
   - When picking images (`image_picker`) or PDF documents (`file_picker`), check the file size in Dart (`file.lengthSync() <= 5 * 1024 * 1024`) before initiating multipart uploads.
   - Verify the file extension (`.jpg`, `.jpeg`, `.png`, `.pdf`) on the client side before sending to prevent unnecessary bandwidth consumption and server rejection.
