ALTER TABLE lost_items
    ADD COLUMN assigned_station_id UUID REFERENCES police_stations(station_id);

ALTER TABLE found_reports
    ADD COLUMN assigned_station_id UUID REFERENCES police_stations(station_id);

CREATE INDEX idx_lost_items_assigned_station ON lost_items(assigned_station_id);
CREATE INDEX idx_found_reports_assigned_station ON found_reports(assigned_station_id);

UPDATE lost_items report
SET assigned_station_id = (
    SELECT station.station_id
    FROM police_stations station
    JOIN locations station_location ON station_location.location_id = station.location_id
    JOIN locations report_location ON report_location.location_id = report.location_id
    WHERE station.is_active = TRUE
    ORDER BY 2 * 6371 * asin(sqrt(
        sin(radians((station_location.latitude - report_location.latitude)::double precision) / 2) ^ 2
        + cos(radians(report_location.latitude::double precision))
        * cos(radians(station_location.latitude::double precision))
        * sin(radians((station_location.longitude - report_location.longitude)::double precision) / 2) ^ 2
    ))
    LIMIT 1
);

UPDATE found_reports report
SET assigned_station_id = (
    SELECT station.station_id
    FROM police_stations station
    JOIN locations station_location ON station_location.location_id = station.location_id
    JOIN locations report_location ON report_location.location_id = report.location_id
    WHERE station.is_active = TRUE
    ORDER BY 2 * 6371 * asin(sqrt(
        sin(radians((station_location.latitude - report_location.latitude)::double precision) / 2) ^ 2
        + cos(radians(report_location.latitude::double precision))
        * cos(radians(station_location.latitude::double precision))
        * sin(radians((station_location.longitude - report_location.longitude)::double precision) / 2) ^ 2
    ))
    LIMIT 1
);