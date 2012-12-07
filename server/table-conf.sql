


CREATE TABLE `conversationparticipant` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `conversationparticipant` ADD COLUMN `conversation` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `conversationparticipant` ADD INDEX `index_conversation` (`conversation`);
ALTER TABLE `conversationparticipant` ADD COLUMN `participant` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `conversationparticipant` ADD INDEX `index_participant` (`participant`);
ALTER TABLE `conversationparticipant` ADD COLUMN `joinTime` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `conversationparticipant` ADD COLUMN `lastSync` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `conversationparticipant` ADD UNIQUE KEY `unique_conversationParticipantContstraint` (`conversation`,`participant`);




CREATE TABLE `userevent` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `userevent` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `userevent` ADD INDEX `index_user` (`user`);
ALTER TABLE `userevent` ADD COLUMN `time` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `userevent` ADD COLUMN `filtered` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `userevent` ADD COLUMN `cleared` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `userevent` ADD COLUMN `conversation` INT(11)  ;
ALTER TABLE `userevent` ADD COLUMN `conversationAction` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userevent` ADD COLUMN `participant` INT(11)  ;
ALTER TABLE `userevent` ADD COLUMN `participantAction` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userevent` ADD COLUMN `contact` INT(11)  ;
ALTER TABLE `userevent` ADD COLUMN `contactAction` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userevent` ADD COLUMN `message` INT(11)  ;
ALTER TABLE `userevent` ADD COLUMN `avatar` INT(11)  ;
ALTER TABLE `userevent` ADD INDEX `index_messageUserTimeFilteredClearedIndex` (`user`,`time`,`filtered`,`cleared`);

ALTER TABLE `userevent` ADD UNIQUE KEY `unique_uniqueConversation` (`user`,`time`,`conversation`);

ALTER TABLE `userevent` ADD UNIQUE KEY `unique_uniqueParticipant` (`user`,`time`,`participant`);

ALTER TABLE `userevent` ADD UNIQUE KEY `unique_uniqueContact` (`user`,`time`,`contact`);

ALTER TABLE `userevent` ADD UNIQUE KEY `unique_uniqueAvatar` (`user`,`time`,`avatar`);

ALTER TABLE `userevent` ADD UNIQUE KEY `unique_uniqueMessage` (`user`,`message`);




CREATE TABLE `contact` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `contact` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `contact` ADD INDEX `index_user` (`user`);
ALTER TABLE `contact` ADD COLUMN `contact` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `contact` ADD INDEX `index_contact` (`contact`);
ALTER TABLE `contact` ADD COLUMN `status` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `contact` ADD COLUMN `statusTime` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `contact` ADD UNIQUE KEY `unique_userContactContstraint` (`user`,`contact`);




CREATE TABLE `user` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `user` ADD COLUMN `token` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD UNIQUE KEY `unique_token` (`token`);
ALTER TABLE `user` ADD COLUMN `name` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `status` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `email` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD UNIQUE KEY `unique_email` (`email`);
ALTER TABLE `user` ADD COLUMN `pendingEmail` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `password` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `user` ADD COLUMN `isAdmin` TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE `user` ADD COLUMN `lastLogin` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `user` ADD COLUMN `lastSync` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';



CREATE TABLE `message` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `message` ADD COLUMN `conversation` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `message` ADD INDEX `index_conversation` (`conversation`);
ALTER TABLE `message` ADD COLUMN `sender` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `message` ADD INDEX `index_sender` (`sender`);
ALTER TABLE `message` ADD COLUMN `body` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `message` ADD COLUMN `time` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';



CREATE TABLE `conversation` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `conversation` ADD COLUMN `originator` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `conversation` ADD INDEX `index_originator` (`originator`);
ALTER TABLE `conversation` ADD COLUMN `type` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `conversation` ADD COLUMN `startTime` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';
ALTER TABLE `conversation` ADD COLUMN `lastMessage` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `conversation` ADD COLUMN `lastMessageTime` TIMESTAMP NOT NULL DEFAULT '1970-01-01 00:00:01';



CREATE TABLE `usertoken` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `usertoken` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `usertoken` ADD INDEX `index_user` (`user`);
ALTER TABLE `usertoken` ADD COLUMN `key` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `usertoken` ADD INDEX `index_key` (`key`);
ALTER TABLE `usertoken` ADD COLUMN `hash` VARCHAR(255) NOT NULL DEFAULT '';



CREATE TABLE `userdevice` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

ALTER TABLE `userdevice` ADD COLUMN `user` INT(11) NOT NULL DEFAULT 0;
ALTER TABLE `userdevice` ADD INDEX `index_user` (`user`);
ALTER TABLE `userdevice` ADD COLUMN `deviceType` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD COLUMN `deviceIdentifier` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD COLUMN `deviceVerifier` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD COLUMN `authToken` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD COLUMN `authSecret` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD COLUMN `phone` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD COLUMN `pendingPhone` VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE `userdevice` ADD UNIQUE KEY `unique_deviceIdentifierLookup` (`deviceType`,`deviceIdentifier`);




CREATE TABLE `undefineduser` (
`id` INT(11) NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

