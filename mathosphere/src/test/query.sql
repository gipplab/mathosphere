CREATE OR REPLACE ALGORITHM = UNDEFINED
  DEFINER =`root`@`localhost`
  SQL SECURITY DEFINER VIEW `coverages` AS
  SELECT
    `Cats`.`cat`                                            AS `c`,
    `results`.`patternName`                                 AS `patternName`,
    min(`results`.`vote`)                                   AS `v`,
    if((max(`results`.`covCat`) >= `Cats`.`cat`) > 0, 1, 0) AS `F`,
    (min(`results`.`vote`) > 0)                             AS `R`
  FROM (`results`
    JOIN `Cats`)
  GROUP BY `Cats`.`cat`, `results`.`pageID`, `results`.`patternName`
  ORDER BY
    `results`.`queryNum`, `results`.`queryFormulaId`, `Cats`.`cat`;