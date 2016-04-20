
CREATE TABLE `wx_officialaccount` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(100) NOT NULL,
	`wxId` VARCHAR(100) NOT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

CREATE TABLE `wx_artical` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`id_officialaccount` INT(11) NOT NULL,
	`title` VARCHAR(200) NOT NULL,
	`url` VARCHAR(1000) NOT NULL,
	`publishDate` DATE NULL DEFAULT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;