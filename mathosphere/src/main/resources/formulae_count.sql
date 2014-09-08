CREATE TABLE `formulae_count` (
  `pageId` varchar(50) NOT NULL,
  `count` int(11) NOT NULL,
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`pageId`)
)