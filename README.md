# Mental-Health-Tracker

# Mental Health Tracker

![Java](https://img.shields.io/badge/Java-23-blue.svg?style=for-the-badge&logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg?style=for-the-badge&logo=mysql)
![GitHub stars](https://img.shields.io/github/stars/YOUR_USERNAME/YOUR_REPO?style=for-the-badge)
![GitHub license](https://img.shields.io/github/license/YOUR_USERNAME/YOUR_REPO?style=for-the-badge)

A comprehensive Java-based desktop application designed to help users monitor, understand, and improve their mental well-being through daily tracking and journaling.

## Table of Contents
- [About The Project](#about-the-project)
- [Key Features](#key-features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [How It Works](#how-it-works)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## About The Project

Mental Health Tracker is a secure, offline-first desktop application built with Java. It provides a private and safe space for users to record their thoughts, track their moods, and perform daily self-assessments. The project leverages Natural Language Processing (NLP) to provide insightful sentiment analysis on journal entries, helping users gain a deeper understanding of their emotional patterns.

The user interface is custom-built with a modern, clean aesthetic inspired by Material Design, ensuring a smooth and intuitive user experience.

## Key Features

- **Dashboard:** A central hub providing a quick overview of your recent activity and mood trends.
- **Daily Journaling:** Write and save daily journal entries in a secure environment.
- **Sentiment Analysis:** Utilizes the **Stanford CoreNLP library** to analyze the sentiment of your journal entries, providing you with feedback on your emotional tone.
- **Mood Tracking:** Log your mood throughout the day to identify patterns and triggers.
- **Daily Assessments:** Answer a set of curated questions to assess your mental state regularly.
- **Personal Profile:** Manage your user information.
- **Reminders:** Set up custom reminders to journal or complete an assessment.
- **Resource Center:** Access a curated list of helpful articles, contacts, and resources for mental wellness.

## Screenshots

*(Add screenshots of your application here to showcase the UI)*

| Dashboard | Journal View |
| :---: | :---: |
| ![Dashboard Screenshot](URL_TO_YOUR_SCREENSHOT) | ![Journal Screenshot](URL_TO_YOUR_SCREENSHOT) |

## Tech Stack

This project is built using the following technologies:

- **Core:** Java (JDK 23)
- **Database:** MySQL
- **UI Framework:** Java Swing with custom Material Design components.
- **Key Libraries:**
  - `mysql-connector-j`: For database connectivity.
  - `Stanford CoreNLP`: For Natural Language Processing and sentiment analysis.
  - `javax.mail`: For the email notification/reminder feature.
  - `joda-time`: For robust date and time handling.

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- [Java Development Kit (JDK) 23](https://www.oracle.com/java/technologies/downloads/) or later.
- [MySQL Server](https://dev.mysql.com/downloads/mysql/) installed and running.
- An IDE like [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [Eclipse](https://www.eclipse.org/downloads/).

### Installation

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/YOUR_USERNAME/YOUR_REPO.git](https://github.com/YOUR_USERNAME/YOUR_REPO.git)
    ```

2.  **Set up the Database:**
    - Open your MySQL client (e.g., MySQL Workbench, DBeaver).
    - Create a new database for the project.
      ```sql
      CREATE DATABASE mental_health_tracker;
      ```
    - *(Optional)* If you have a `.sql` schema file, import it into your new database.

3.  **Configure Application:**
    - Open the project in your IDE.
    - Locate the database configuration file (e.g., `src/app/utilities/Database.java`).
    - Update the database URL, username, and password with your MySQL credentials.
    - Locate the email configuration file (e.g., `src/app/utilities/EmailSender.java`).
    - Update the SMTP server, port, and sender credentials if you plan to use the email feature.

4.  **Add Libraries:**
    - Make sure all the `.jar` files in the `lib` directory are added to the project's build path.
      - In IntelliJ: `File` -> `Project Structure` -> `Modules` -> `Dependencies` -> `+` -> `JARs or directories...`
      - In Eclipse: Right-click project -> `Build Path` -> `Configure Build Path` -> `Libraries` -> `Add JARs...`

5.  **Run the Application:**
    - Find the main entry point of the application (e.g., `src/app/App.java`).
    - Run the `main` method.

## How It Works

The application follows a standard desktop application architecture. The user interacts with the GUI frames built using Java Swing. All user data, including journal entries, mood logs, and assessment results, is securely stored in a local or remote MySQL database.

The standout feature is the integration of Stanford CoreNLP. When a user saves a journal entry, the text is processed by the NLP pipeline to determine its overall sentiment (e.g., positive, neutral, negative), which is then stored and can be visualized to show trends over time.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Your Name - [@YourTwitterHandle](https://twitter.com/YourTwitterHandle) - your.email@example.com

Project Link: [https://github.com/YOUR_USERNAME/YOUR_REPO](https://github.com/YOUR_USERNAME/YOUR_REPO)
