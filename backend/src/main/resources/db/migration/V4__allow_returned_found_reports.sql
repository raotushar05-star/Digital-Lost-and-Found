ALTER TABLE found_reports
    DROP CONSTRAINT chk_found_reports_status;

ALTER TABLE found_reports
    ADD CONSTRAINT chk_found_reports_status
    CHECK (status IN ('SUBMITTED', 'RECEIVED', 'LINKED', 'RETURNED', 'REJECTED', 'DUPLICATE'));

UPDATE found_reports report
SET status = 'RETURNED',
    updated_at = now()
FROM found_items item
JOIN handover_records handover ON handover.found_item_id = item.found_item_id
WHERE item.found_report_id = report.found_report_id
  AND report.status <> 'RETURNED';