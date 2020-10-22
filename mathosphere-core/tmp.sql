SELECT t.math_local_qid, t.math_body
FROM wiki_arqmath20.math_wbs_text_store t
LIMIT 10
INTO OUTFILE 'data.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'