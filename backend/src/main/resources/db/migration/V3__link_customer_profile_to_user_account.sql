-- Ensure only linked account/customer pairs remain before adding strong FK.
DELETE cp
FROM customer_profile cp
LEFT JOIN user_account ua ON ua.id = cp.customer_id
WHERE ua.id IS NULL;

-- Enforce one-to-one account/customer linkage with cascade delete from account -> customer.
SET @fk_exists := (
	SELECT COUNT(*)
	FROM information_schema.REFERENTIAL_CONSTRAINTS
	WHERE CONSTRAINT_SCHEMA = DATABASE()
	  AND CONSTRAINT_NAME = 'fk_customer_profile_user_account'
);

SET @fk_sql := IF(
	@fk_exists = 0,
	'ALTER TABLE customer_profile ADD CONSTRAINT fk_customer_profile_user_account FOREIGN KEY (customer_id) REFERENCES user_account(id) ON DELETE CASCADE',
	'SELECT 1'
);

PREPARE stmt FROM @fk_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;