# CivicWatch - Community Issue Reporting Platform 🏘️

CivicWatch is an Android application that empowers citizens to report, track, and resolve community issues in real-time. From potholes to illegal dumping, CivicWatch makes civic engagement simple and effective.

<div align="center">

![CivicWatch Logo](https://via.placeholder.com/800x200/0088cc/ffffff?text=CivicWatch+Community+Monitoring)

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://java.com)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Google Maps](https://img.shields.io/badge/Google%20Maps-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)](https://developers.google.com/maps)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

</div>

## 📸 App Screenshots

| Issues Map | Report New Issue | Reported Issues | About Us |
|:---:|:---:|:---:|:---:|
| ![IssuesMap](https://via.placeholder.com/250x500/4CAF50/ffffff?text=Issues+Map) | ![ReportNewIssue](https://via.placeholder.com/250x500/2196F3/ffffff?text=Report+Issue) | ![ReportedIssues](https://via.placeholder.com/250x500/FF9800/ffffff?text=Issues+List) | ![AboutUs](https://via.placeholder.com/250x500/9C27B0/ffffff?text=About+Us) |
| **Interactive Map** - View all reported issues on an interactive Google Map with custom markers | **Report Issues** - Submit new issues with photos, location, and detailed descriptions | **Issues Dashboard** - Browse, filter, and upvote community-reported issues | **About CivicWatch** - Learn about the app and its mission |

## 🚀 Key Features

### 🗺️ **Real-time Issue Mapping**
- Interactive Google Maps integration
- Custom markers for different issue types (Pothole, Graffiti, Litter, etc.)
- Tap-to-select location for reporting
- Real-time marker updates via Firebase

### 📝 **Smart Issue Reporting**
- Photo capture with camera or gallery integration
- 10+ categorized issue types
- Severity level selection (Minor, Moderate, Major, Critical)
- Detailed description field
- Automatic GPS location detection

### 📊 **Community Dashboard**
- Browse all reported issues with cards
- Filter by status (Pending, In Progress, Resolved)
- Sort by date (newest first)
- Upvote system for community prioritization
- Visual status indicators with color coding

### 🔄 **Real-time Synchronization**
- Firebase Realtime Database integration
- Instant issue updates across all users
- Live upvote counting
- No refresh needed - updates appear automatically

## 🏗️ Technology Stack

**Frontend:**
- Android SDK with Java
- Material Design Components
- Google Maps Android API
- View Binding
- RecyclerView with custom adapters

**Backend Services:**
- Firebase Realtime Database
- Google Maps Platform
- Location Services API

**Architecture:**
- Model-View-Presenter pattern
- Repository pattern for data management
- Firebase listeners for real-time updates

## 📋 Supported Issue Categories

| Category | Color | Description |
|----------|-------|-------------|
| **Pothole** | 🚧 | Road damage, potholes, road surface issues |
| **Graffiti** | 🎨 | Vandalism, unauthorized graffiti |
| **Litter** | 🗑️ | Trash accumulation, waste management issues |
| **Illegal parking** | 🚗 | Vehicles parked in prohibited areas |
| **Roadworks** | 🚧 | Ongoing or needed road repairs |
| **Street lighting** | 💡 | Broken street lights, lighting issues |
| **Illegal dumping** | 🚛 | Illegal waste disposal sites |
| **Abandoned vehicle** | 🚘 | Abandoned cars, vehicles left unattended |
| **Tree issues** | 🌳 | Damaged trees, fallen branches, greenery |
| **Street signs** | 🪧 | Broken or missing street signs |

## 🛠️ Installation & Setup

### Prerequisites
- Android Studio (Latest Version)
- Android SDK 21+ (Android 5.0 Lollipop)
- Google Maps API Key
- Firebase Account

### Step 1: Clone the Repository
```bash
git clone https://github.com/MusumbaAbeljr055/civicwatch.git
cd civicwatch
