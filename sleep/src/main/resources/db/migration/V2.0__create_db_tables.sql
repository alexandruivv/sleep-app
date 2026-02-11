CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS sleep_entry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,

    sleep_date DATE NOT NULL,

    time_in_bed_start TIMESTAMPTZ NOT NULL,
    time_in_bed_end   TIMESTAMPTZ NOT NULL,

    total_time_in_bed_minutes INTEGER NOT NULL,

    morning_feeling VARCHAR(4) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT ck_sleep_time_order
    CHECK (time_in_bed_end > time_in_bed_start),

    CONSTRAINT ck_total_time_positive
    CHECK (total_time_in_bed_minutes > 0),

    CONSTRAINT ck_morning_feeling
    CHECK (morning_feeling IN ('BAD', 'OK', 'GOOD')),

    CONSTRAINT uq_sleep_entry_user_date
    UNIQUE (user_id, sleep_date)
    );

CREATE INDEX IF NOT EXISTS idx_sleep_entry_user_date
    ON sleep_entry (user_id, sleep_date DESC);

CREATE INDEX IF NOT EXISTS idx_sleep_entry_sleep_date
    ON sleep_entry (sleep_date);
