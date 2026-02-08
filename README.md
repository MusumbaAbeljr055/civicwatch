# CivicWatch - Community Issue Reporting Platform 🏘️

CivicWatch is an Android application that empowers citizens to report, track, and resolve community issues in real-time. From potholes to illegal dumping, CivicWatch makes civic engagement simple and effective.

<div align="center">

![CivicWatch Banner](docs/images/banner.jpg)

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://java.com)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Google Maps](https://img.shields.io/badge/Google%20Maps-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)](https://developers.google.com/maps)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)
[![GitHub stars](https://img.shields.io/github/stars/MusumbaAbeljr055/civicwatch?style=for-the-badge)](https://github.com/MusumbaAbeljr055/civicwatch/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/MusumbaAbeljr055/civicwatch?style=for-the-badge)](https://github.com/MusumbaAbeljr055/civicwatch/network)

</div>

## 📸 App Screenshots

<div align="center">

| Issues Map | Report New Issue | Reported Issues | About Us |
|:---:|:---:|:---:|:---:|
| <img src="docs/images/IssuesMap.jpg" width="200" alt="Issues Map"> | <img src="docs/images/ReportNewIssue.jpg" width="200" alt="Report New Issue"> | <img src="docs/images/ReportedIssues.jpg" width="200" alt="Reported Issues"> | <img src="docs/images/AboutUs.jpg" width="200" alt="About Us"> |
| **Interactive Map View** | **Issue Reporting Screen** | **Community Issues Dashboard** | **About CivicWatch** |

</div>

## 🎯 Why CivicWatch?

CivicWatch bridges the gap between citizens and local authorities by providing a modern, user-friendly platform for reporting civic issues. No more lengthy phone calls or complicated forms – report issues with just a few taps!

### 🏆 Key Benefits
- **Real-time issue tracking** for the entire community
- **Transparent process** with status updates
- **Community voting** to prioritize important issues
- **Photo evidence** for better issue documentation
- **Location-based reporting** for accurate problem identification

## 🚀 Features

### 🗺️ **Interactive Mapping**
- Google Maps integration with custom markers
- Real-time issue visualization
- Tap-to-select location for precise reporting
- Different marker icons for each issue category
- Cluster markers for better visualization in dense areas

### 📝 **Smart Reporting System**
- **Multiple Issue Categories**: Potholes, Graffiti, Litter, Illegal Parking, Street Lighting, and more
- **Photo Evidence**: Capture or upload photos directly from the app
- **Severity Levels**: Classify issues as Minor, Moderate, Major, or Critical
- **Detailed Descriptions**: Provide comprehensive information about the issue
- **Automatic Location Detection**: Uses GPS to pinpoint exact locations

### 📊 **Community Dashboard**
- **Browse All Issues**: View all community-reported problems
- **Filter & Sort**: Filter by status, sort by date or upvotes
- **Upvote System**: Vote on issues that need immediate attention
- **Status Tracking**: Monitor issue progress (Pending → In Progress → Resolved)
- **Visual Indicators**: Color-coded status badges for quick identification

### 🔄 **Real-time Synchronization**
- **Firebase Integration**: All data syncs instantly across devices
- **Live Updates**: See new issues appear in real-time
- **Upvote Counting**: Watch upvote numbers change as community votes
- **Status Changes**: Get notified when authorities update issue status

### 🎨 **Modern UI/UX**
- **Material Design**: Clean, intuitive interface following Google's design guidelines
- **Responsive Layout**: Works on all screen sizes from phones to tablets
- **Smooth Animations**: Pleasing transitions and feedback
- **Dark Mode Support**: Easy on the eyes with dark theme option

## 🏗️ Technology Stack

### Frontend
- **Language**: Java
- **Framework**: Android SDK
- **UI Components**: Material Design Components
- **Maps**: Google Maps Android API
- **Image Processing**: Bitmap manipulation and Base64 encoding
- **Permissions**: Runtime permissions for camera, location, and storage

### Backend & Services
- **Database**: Firebase Realtime Database
- **Authentication**: Firebase Authentication (ready for future implementation)
- **Storage**: Firebase Storage (for image uploads)
- **Maps API**: Google Maps Platform
- **Location Services**: FusedLocationProviderClient

### Architecture & Patterns
- **MVP Pattern**: Model-View-Presenter architecture
- **Repository Pattern**: Clean data layer abstraction
- **Adapter Pattern**: RecyclerView adapters for efficient list rendering
- **Observer Pattern**: Firebase listeners for real-time updates
- **Singleton Pattern**: Utility classes and helpers

## 📁 Project Structure
