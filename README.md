# Recipe Book - Android Application

A modern, social culinary platform built with **Kotlin** and **Clean Architecture**, allowing users to share, discover, and organize recipes with ease.

## Features

### Authentication & Profile
*   **Secure Login/Register**: Powered by Firebase Authentication.
*   **Remember Me**: Persistent session handling using Android DataStore.
*   **Modern Registration**: Includes a Country selection dropdown (Spinner) and real-time profile picture selection.
*   **Dynamic Profile**: Users can update their profile picture anytime. Images are compressed and hosted on Cloudinary for optimal performance.

### Recipe Dashboard
*   **Real-time Feed**: Live updates from Firestore using Kotlin Flows.
*   **Category Filtering**: Organized tabs (All, Breakfast, Dinner, etc.) that filter recipes instantly.
*   **Smart Search**: Search bar with "Clear" functionality. Filtering is category-aware (searches within the active tab).
*   **Visual Cards**: High-quality thumbnails with modern, compact styling (112dp left-aligned thumbnails).

### Recipe Details
*   **Clean UI**: Separated header design where the recipe image sits in a premium rounded card above the content.
*   **Interaction**: Direct links to YouTube cooking tutorials.
*   **Ownership Management**: Dedicated "Edit" and "Delete" actions visible only to the recipe creator.

### UI/UX Excellence
*   **Modern Styling**: "Soft-styled" text inputs with 16dp rounded corners and subtle 0.8dp borders.
*   **Interactive Feedback**: The app provides real-time Toasts and Snackbars for every action (e.g., "Saving recipe...", "Profile updated").
*   **Consistent Theme**: Forced Light Mode to maintain brand vibrant orange/terracotta aesthetics.
*   **Premium Brand Identity**: High-resolution splash screen and large logos for better brand recognition.

---

## 🛠 Tech Stack

*   **Language**: Kotlin
*   **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture (Domain/Data/Presentation layers).
*   **Dependency Injection**: Dagger Hilt.
*   **Database**: Google Firebase Firestore (Real-time).
*   **Storage**: Cloudinary (Image Hosting & Optimization).
*   **Networking**: Coroutines & Flow for reactive programming.
*   **UI Components**: Material3, ViewBinding, Navigation Component (SafeArgs).
*   **Image Loading**: Glide with Cache-busting strategies.

---

##  Setup & Installation

### 1. Prerequisites
*   Android Studio Ladybug (or newer).
*   Minimum SDK: 24 (Android 7.0).
*   Target SDK: 35 (Android 15).

### 2. Firebase Configuration
1.  Create a project in the [Firebase Console](https://console.firebase.google.com/).
2.  Add an Android App and download `google-services.json`.
3.  Place the file in the `app/` directory.
4.  Enable **Email/Password Authentication** and **Firestore Database**.

### 3. Firestore Indexing
To support category filtering and sorting, you must create a **Composite Index** in Firestore:
*   **Collection**: `recipes`
*   **Field 1**: `category` (Ascending)
*   **Field 2**: `createdAt` (Descending)

*   **Collection**: `recipes`
*   **Field 1**: `autherId` (Ascending)
*   **Field 2**: `createdAt` (Descending)

### 4. Cloudinary Configuration
Update the following values in `com.example.recipe_book.util.Constants.kt`:
*   `CLOUDINARY_CLOUD_NAME`
*   `CLOUDINARY_UPLOAD_PRESET`

---

## 📂 Project Structure

`com.example.recipe_book`
├── **data**            # Repository Implementations, DTOs, & Remote Data Sources  
├── **di**              # Hilt Modules (Firebase, Cloudinary, Repositories)  
├── **domain**          # Business Logic (UseCases, Models, Repository Interfaces)  
├── **presentation**    # UI (Fragments, ViewModels, Adapters)  
│   ├── auth        # Login & Registration  
│   ├── home        # Dashboard & Search  
│   ├── profile     # User Profile & My Recipes  
│   └── recipedetails  
└── **util**            # Extensions, Constants, & Validators  

---

## 👨‍💻 Notes
*   **Package Visibility**: The app includes the `<queries>` block for `com.google.android.gms` to ensure compatibility with Android 11+.
*   **Image Security**: Uses unsigned Cloudinary uploads with unique identifiers to bypass "overwrite" security restrictions on the client side.

