# CivicWatch - Community Issue Reporting Platform

<div align="center">

<img src="docs/images/ic_civicwatch.png" width="200" alt="CivicWatch Logo">

<h3>Empowering Communities Through Technology</h3>

[![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://java.com)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Google Maps](https://img.shields.io/badge/Google%20Maps-4285F4?style=flat-square&logo=googlemaps&logoColor=white)](https://developers.google.com/maps)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

</div>

## Overview

CivicWatch is an Android application that enables citizens to report, track, and resolve community issues in real-time. Transform civic engagement with a modern, intuitive platform for reporting potholes, graffiti, litter, and other community concerns.

---

## 📱 Screenshots

<div align="center">

| | | |
|:---:|:---:|:---:|
| **Issues Map** | **Report Issue** | **Issues Dashboard** |
| <img src="docs/images/IssuesMap.jpeg" width="200"> | <img src="docs/images/ReportNewIssue.jpeg" width="200"> | <img src="docs/images/ReportedIssues.jpeg" width="200"> |
| Interactive map view | Issue reporting interface | Community issues list |

</div>

## ✨ Features

### 🗺️ **Interactive Mapping**
- Real-time issue visualization on Google Maps
- Custom markers for different issue categories
- Tap-to-select location for precise reporting
- Cluster markers for dense urban areas

### 📝 **Smart Reporting**
- Photo capture with camera or gallery
- 10+ categorized issue types
- Severity classification (Minor to Critical)
- Detailed description fields
- Automatic GPS location detection

### 📊 **Community Dashboard**
- Browse all reported issues with filtering
- Upvote system for issue prioritization
- Status tracking (Pending → In Progress → Resolved)
- Color-coded status indicators

### 🔄 **Real-time Updates**
- Firebase-powered instant synchronization
- Live upvote counting
- Push notification support
- Multi-user collaboration

### Key Components

1. **MainActivity.java** - Navigation controller with bottom navigation
2. **MapFragment.java** - Google Maps integration with issue markers
3. **IssuesFragment.java** - Issues list with filtering and upvoting
4. **AboutFragment.java** - App information and details
5. **IssuesAdapter.java** - RecyclerView adapter for issues list
6. **Issue.java** - Data model for issue entities
7. **MapMarkerUtils.java** - Custom marker icon generation
8. **ImageUtils.java** - Image processing utilities

## 🚀 Quick Start

### Prerequisites
- Android Studio 2022.3+
- Android SDK 21+
- Java JDK 8+
- Google Maps API Key
- Firebase Account

### Installation
1. **Clone the repository**
   ```bash
   git clone https://github.com/MusumbaAbeljr055/civicwatch.git
   cd civicwatch
