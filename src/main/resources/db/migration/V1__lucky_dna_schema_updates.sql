ALTER TABLE saved_play
    ADD COLUMN IF NOT EXISTS play_date date;

UPDATE saved_play
SET play_date = created_at::date
WHERE play_date IS NULL;

ALTER TABLE saved_play
    ALTER COLUMN play_date SET NOT NULL;

ALTER TABLE play_result
    ADD COLUMN IF NOT EXISTS prize_amount numeric(12,2);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_saved_play_user_numbers_date'
    ) THEN
ALTER TABLE saved_play
    ADD CONSTRAINT uq_saved_play_user_numbers_date
        UNIQUE (
                user_id,
                play_date,
                white_ball1,
                white_ball2,
                white_ball3,
                white_ball4,
                white_ball5,
                power_ball
            );
END IF;
END $$;