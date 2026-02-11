INSERT INTO app_user (id, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', now())
    ON CONFLICT (id) DO NOTHING;

-- Insert entries for last 30 days (including today UTC date stored in sleep_date)
-- Bedtime: around 22:00 - 00:30 UTC
-- Wake time: bedtime + 6h30m - 9h00m
WITH days AS (
    SELECT
        ('11111111-1111-1111-1111-111111111111')::uuid AS user_id,
        (current_date - gs.day_offset)::date AS sleep_date,
        gs.day_offset
    FROM generate_series(0, 30) AS gs(day_offset)
),
     times AS (
         SELECT
             user_id,
             sleep_date,

             -- bedtime base: 22:00, plus 0..150 minutes => up to 00:30
             (sleep_date::timestamptz + time '22:00'
            + make_interval(mins => (random() * 150)::int)
                 ) AS time_in_bed_start,

             -- duration: 390..540 minutes (6h30..9h00)
             (sleep_date::timestamptz + time '22:00'
            + make_interval(mins => (random() * 150)::int)
            + make_interval(mins => 390 + (random() * 150)::int)
                 ) AS time_in_bed_end,

             day_offset
         FROM days
     ),
     prepared AS (
         SELECT
             gen_random_uuid() AS id,
             user_id,
             sleep_date,
             time_in_bed_start,
             time_in_bed_end,
             (extract(epoch from (time_in_bed_end - time_in_bed_start)) / 60)::int AS total_time_in_bed_minutes,
             CASE
                 WHEN (day_offset % 10) IN (0, 1) THEN 'BAD'
                 WHEN (day_offset % 10) IN (2, 3, 4) THEN 'OK'
                 ELSE 'GOOD'
                 END AS morning_feeling
         FROM times
     )
INSERT INTO sleep_entry (
    id,
    user_id,
    sleep_date,
    time_in_bed_start,
    time_in_bed_end,
    total_time_in_bed_minutes,
    morning_feeling,
    created_at,
    updated_at
)
SELECT
    id,
    user_id,
    sleep_date,
    time_in_bed_start,
    time_in_bed_end,
    total_time_in_bed_minutes,
    morning_feeling,
    now(),
    now()
FROM prepared
    ON CONFLICT (user_id, sleep_date) DO NOTHING;
