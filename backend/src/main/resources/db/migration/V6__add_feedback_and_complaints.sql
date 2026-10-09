CREATE TABLE feedback_submissions (
    feedback_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(user_id),
    feedback_type VARCHAR(20) NOT NULL,
    subject VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    admin_response TEXT,
    responded_by UUID REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_feedback_type CHECK (feedback_type IN ('FEEDBACK', 'COMPLAINT')),
    CONSTRAINT chk_feedback_status CHECK (status IN ('OPEN', 'IN_REVIEW', 'RESOLVED', 'CLOSED'))
);

CREATE INDEX idx_feedback_user_created ON feedback_submissions(user_id, created_at DESC);
CREATE INDEX idx_feedback_status_created ON feedback_submissions(status, created_at DESC);