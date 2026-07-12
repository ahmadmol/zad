# Ihsan Part 4 Future Integration Request

## Overview
This document outlines the steps required to integrate the Part 4 Charity Trust Layer into the main Ihsan application in a future phase.

## Required Edits (Future Phase)

### 1. Navigation
- Add `IhsanPlusCharityTrust` route to `Screen.kt`.
- Add composable destination to `AppNavHost.kt`.
- Link "View Verified Cases" from the existing Ehsan dashboard.

### 2. Dependency Injection
- Register `ihsanPlusCharityTrustModule` in the `AppModule` or `FeatureModule`.

### 3. Database (Room)
- Extend `DonationEntity` with verification fields (status, verifiedBy, verificationDate).
- Update `UserDao` or create a new `CharityTrustDao` to handle trust metrics.

### 4. Data Layer
- Transition `IhsanPlusCharityTrustRepositoryImpl` from `FakeDataSource` to a real `CharityTrustRemoteDataSource`.
- Map backend verification status to `IhsanPlusVerificationStatus`.

### 5. UI Integration
- Replace static strings with string resources in `strings.xml`.
- Integrate `IhsanPlusHighlightedCaseCard` into the existing `DonationListScreen`.
- Add trust badges to existing `DonationDetailScreen`.

### 6. External Intents
- Safely implement WhatsApp and Phone intents for contact safety features, ensuring they follow the privacy guidelines defined in Part 4.
