CREATE DATABASE college_id_cards;

USE college_id_cards;

CREATE TABLE students (
	srn VARCHAR(20) PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	department VARCHAR(50) NOT NULL,
	semester INT NOT NULL,
	gender VARCHAR(20) NOT NULL,
	card_color VARCHAR(20) DEFAULT '#ADD8E6';
);
