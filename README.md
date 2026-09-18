<h1 align="center">
  <img src="https://raw.githubusercontent.com/google/material-design-icons/master/png/action/account_balance_wallet/materialicons/48dp/2x/baseline_account_balance_wallet_black_48dp.png" alt="Bluff Logo" width="80" height="80">
  <br>
  Bluff - Personal Finance Tracker
</h1>

<p align="center">
  <a href="https://github.com/yourusername/bluff/actions"><img src="https://github.com/yourusername/bluff/workflows/Build%20and%20Release%20APK/badge.svg" alt="Build Status"></a>
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
  <a href="https://developer.android.com/about/versions/13"><img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Kotlin"></a>
</p>

<p align="center">
  <strong>Bluff</strong> is an open-source, local-first Android application designed to help you take control of your personal finances. It offers automated expense tracking through secure, on-device SMS parsing, comprehensive budgeting, and financial goal tracking—all wrapped in a sleek, professional Jetpack Compose UI.
</p>

<hr>

## ✨ Features

- 📱 **Sleek & Professional UI:** Built entirely with modern Jetpack Compose. Beautiful animations, deep monochrome themes with sophisticated accent colors.
- 🤖 **Automated SMS Parsing:** Intelligently reads incoming bank SMS messages and automatically categorizes and adds transactions to your ledger. Currently supports **SBI** and **Kotak** bank formats out of the box!
- 🔒 **Local-First & Privacy-Focused:** No cloud sync by default. Your financial data stays securely on your device using an offline Room Database.
- 💰 **Budgeting & Goals:** Set monthly budgets across various categories and track your long-term savings goals.
- 🔁 **Recurring Transactions:** Automate your fixed monthly subscriptions and bills with Android's WorkManager.
- 📊 **Insightful Analytics:** Visualize your spending patterns with beautiful line charts, bar charts, and pie charts.
- ⚙️ **Customizable:** Change accent colors, app density, start of financial month, and notifications in a robust settings panel.

## 📸 Screenshots

*(Replace these placeholder paths with actual screenshots from your app)*

<p align="center">
  <img src="https://via.placeholder.com/250x500.png?text=Home+Screen" width="24%" />
  <img src="https://via.placeholder.com/250x500.png?text=Transactions" width="24%" />
  <img src="https://via.placeholder.com/250x500.png?text=Analytics" width="24%" />
  <img src="https://via.placeholder.com/250x500.png?text=Settings" width="24%" />
</p>

## 🛠 Tech Stack

Bluff is built using the recommended Modern Android Development (MAD) guidelines:

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material 3
- **Architecture:** Clean Architecture with MVVM (Model-View-ViewModel) + Repository Pattern
- **Asynchrony:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html) for reactive data streaming
- **Local Database:** [Room Database](https://developer.android.com/training/data-storage/room)
- **Background Work:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Dependency Injection:** Manual App Container (Simulated DI)

## 🚀 Getting Started

### Prerequisites

- Android Studio Giraffe | 2022.3.1 or newer
- JDK 17
- Android SDK API level 34

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/bluff.git
   ```

2. **Open the project in Android Studio**
   - Launch Android Studio and select `Open an Existing Project`.
   - Navigate to the cloned `bluff/android` folder and open it.

3. **Build the Project**
   - Wait for Gradle to finish syncing.
   - Click the green `Run` button or use `./gradlew assembleDebug` in the terminal.

4. **Testing Automated SMS Parsing**
   - Make sure you grant the SMS permissions on the first launch.
   - You can test the receiver locally via ADB. Example for SBI:
     ```bash
     adb shell 'am broadcast -n com.example.bluff/.receiver.SmsReceiver -a com.example.bluff.TEST_SMS --es TEST_SENDER "JD-SBIUPI-S" --es TEST_BODY "Dear UPI user A/C X5347 debited by 3000.00 on date 08Aug26 trf to ALEX  JIJU"'
     ```

## 🤝 Contributing

We welcome contributions! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) for details on how you can help make Bluff even better.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
