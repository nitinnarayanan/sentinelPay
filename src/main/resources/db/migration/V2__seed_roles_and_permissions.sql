INSERT INTO permissions (id, name, description, created_at, updated_at) VALUES
                                                                            ('10000000-0000-0000-0000-000000000001', 'TRANSACTION_CREATE', 'Allows customer to create a transaction', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000002', 'TRANSACTION_VIEW_OWN', 'Allows customer to view their own transactions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000003', 'TRANSACTION_VIEW_ALL', 'Allows authorized staff to view all transactions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000004', 'FRAUD_CASE_VIEW', 'Allows fraud users to view fraud cases', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000005', 'FRAUD_CASE_ASSIGN', 'Allows fraud users to assign fraud cases', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000006', 'FRAUD_CASE_DECIDE', 'Allows fraud users to approve, reject, or close fraud cases', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000007', 'AUDIT_VIEW', 'Allows auditor/admin to view audit events', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000008', 'RULE_MANAGE', 'Allows risk managers/admins to manage fraud rules', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000009', 'USER_MANAGE', 'Allows admins to manage users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                            ('10000000-0000-0000-0000-000000000010', 'REPORT_VIEW', 'Allows authorized users to view reports', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
                                                                      ('20000000-0000-0000-0000-000000000001', 'CUSTOMER', 'Default customer role for transaction users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                      ('20000000-0000-0000-0000-000000000002', 'FRAUD_ANALYST', 'Fraud operations user who reviews suspicious transactions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                      ('20000000-0000-0000-0000-000000000003', 'RISK_MANAGER', 'Risk manager who manages fraud rules and high-risk decisions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                      ('20000000-0000-0000-0000-000000000004', 'ADMIN', 'System administrator with user and platform management access', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                                                                      ('20000000-0000-0000-0000-000000000005', 'AUDITOR', 'Read-only audit and compliance user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- CUSTOMER permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001'),
                                                          ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002');

-- FRAUD_ANALYST permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003'),
                                                          ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004'),
                                                          ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000005'),
                                                          ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000006');

-- RISK_MANAGER permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003'),
                                                          ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000004'),
                                                          ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000006'),
                                                          ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000008'),
                                                          ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000010');

-- ADMIN permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT '20000000-0000-0000-0000-000000000004', id
FROM permissions;

-- AUDITOR permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000003'),
                                                          ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000004'),
                                                          ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000007'),
                                                          ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000010');