DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_catalog.pg_roles
        WHERE rolname = 'studyassistant_app'
    ) THEN
        REVOKE ALL PRIVILEGES
            ON TABLE public.flyway_schema_history
            FROM studyassistant_app;
    END IF;
END
$$;
