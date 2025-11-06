USE tracker;

//users
CREATE TABLE `users` (
  `username` varchar(45) NOT NULL,
  `height` int unsigned NOT NULL,
  `weight` int unsigned NOT NULL,
  `birthYear` year NOT NULL,
  `gender` enum('Male','Female') NOT NULL,
  `password` varchar(45) NOT NULL,
  `email` varchar(100) NOT NULL DEFAULT '',
  `verified` tinyint(1) DEFAULT '0',
  `verification_code` varchar(6) DEFAULT NULL,
  `device_token` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


//resources
CREATE TABLE `resources` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(300) NOT NULL,
  `keywords` varchar(300) NOT NULL,
  `content` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

//mood_logs
CREATE TABLE `mood_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `mood` varchar(45) NOT NULL,
  `stress_level` int NOT NULL,
  `log_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `username_idx` (`username`),
  CONSTRAINT `fk_moodlogs_users` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


//user_daily_mood_stress
CREATE TABLE `user_daily_mood_stress` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `date` date NOT NULL,
  `average_score` decimal(5,2) NOT NULL,
  `dominant_type` varchar(20) NOT NULL,
  `mood_cont` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


//journal_entries
CREATE TABLE `journal_entries` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(45) NOT NULL,
  `entry` text NOT NULL,
  `entry_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sentiment_score` int NOT NULL,
  `sentiment_type` varchar(45) NOT NULL,
  `very_positive` double NOT NULL,
  `positive` double NOT NULL,
  `neutral` double NOT NULL,
  `negative` double NOT NULL,
  `very_negative` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `username_idx` (`username`),
  CONSTRAINT `fk_journal_users` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

//user_daily_avg_journal_sentiment
CREATE TABLE `user_daily_avg_journal_sentiment` (
    `id` int NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `date` date NOT NULL,
    `avg_very_positive` decimal(5,2) NOT NULL,
    `avg_positive` decimal(5,2) NOT NULL,
    `avg_neutral` decimal(5,2) NOT NULL,
    `avg_negative` decimal(5,2) NOT NULL,
    `avg_very_negative` decimal(5,2) NOT NULL,
    `entry_count` int DEFAULT '1',
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`,`date`)
  ) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

//assessment_questions
  CREATE TABLE `assessment_questions` (
    `id` int NOT NULL AUTO_INCREMENT,
    `question_text` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=395 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

//user_asked_questions
CREATE TABLE `user_asked_questions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `question_id` int NOT NULL,
  `asked_date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `question_id` (`question_id`),
  KEY `username` (`username`,`asked_date`),
  CONSTRAINT `user_asked_questions_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `assessment_questions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

//user_assessment_responses
CREATE TABLE `user_assessment_responses` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `question_id` int NOT NULL,
  `response_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `selected_sentiment` varchar(20) NOT NULL,
  `sentiment_score` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `question_id` (`question_id`),
  KEY `username` (`username`,`response_time`),
  CONSTRAINT `user_assessment_responses_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `assessment_questions` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

//user_daily_sentiment
CREATE TABLE `user_daily_sentiment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `date` date NOT NULL,
  `average_score` decimal(5,2) NOT NULL,
  `dominant_type` varchar(20) NOT NULL,
  `assessment_count` int DEFAULT '1',
  `average_type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;




