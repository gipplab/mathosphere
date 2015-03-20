use mathosphere;
CREATE TABLE depthMatch
(
  dMID INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  fID INT NOT NULL,
  qId INT NOT NULL,
  depth INT NOT NULL,
  updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE formulae_count
(
  pageId VARCHAR(50) PRIMARY KEY NOT NULL,
  count INT NOT NULL,
  ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE formulae_fulltext
(
  formula_name VARCHAR(50),
  value LONGTEXT,
  sectionname VARCHAR(50)
);
CREATE TABLE formulae_name
(
  fId INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  pageId VARCHAR(50) NOT NULL,
  formula_name VARCHAR(50) NOT NULL,
  isEquation TINYINT
);
CREATE TABLE formulae_name2
(
  fId INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  pageId VARCHAR(50) NOT NULL,
  formula_name VARCHAR(50) NOT NULL,
  isEquation TINYINT
);
CREATE TABLE formulae_strict
(
  stricCmml LONGTEXT,
  abstractCD LONGTEXT,
  fId INT PRIMARY KEY NOT NULL
);
CREATE TABLE ftbkf
(
  formula_name VARCHAR(50),
  value LONGTEXT,
  sectionname VARCHAR(50)
);
CREATE TABLE referee_votes
(
  qId INT NOT NULL,
  pageId VARCHAR(50) NOT NULL,
  vote INT NOT NULL
);
CREATE TABLE results
(
  queryNum INT,
  queryFormulaId CHAR(8),
  formula_name VARCHAR(50),
  cdMatch TINYINT,
  dataMatch TINYINT,
  matchDepth INT,
  queryCoverage DOUBLE,
  isFormulae TINYINT,
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  vote INT,
  pageID VARCHAR(50) NOT NULL,
  covCat INT
);
ALTER TABLE depthMatch ADD FOREIGN KEY (fID) REFERENCES formulae_name (fId);
CREATE UNIQUE INDEX results ON depthMatch (qId, fID);
CREATE INDEX fId_idx ON depthMatch (fID);
ALTER TABLE formulae_name ADD FOREIGN KEY (pageId) REFERENCES formulae_count (pageId);
CREATE UNIQUE INDEX pageId_formula_name_index ON formulae_name (pageId, formula_name);
ALTER TABLE formulae_name2 ADD FOREIGN KEY (pageId) REFERENCES formulae_count (pageId);
CREATE UNIQUE INDEX pageId_formula_name_index2 ON formulae_name2 (pageId, formula_name);
ALTER TABLE formulae_strict ADD FOREIGN KEY (fId) REFERENCES formulae_name (fId);
ALTER TABLE referee_votes ADD FOREIGN KEY (pageId) REFERENCES formulae_count (pageId);
